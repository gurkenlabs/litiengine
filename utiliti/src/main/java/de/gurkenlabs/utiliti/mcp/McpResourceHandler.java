package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.environment.tilemap.TmxPropertyMetadataRegistry;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.view.components.UI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.net.URI;
import java.util.Map;

public class McpResourceHandler {

  public static JsonObject getResourcesList() {
    JsonArrayBuilder resources = Json.createArrayBuilder();
    resources.add(resource(
        "uti://project/info", "Project Info", "Current project path and resource summary"));
    resources.add(resource(
        "uti://project/maps", "Project Maps", "Maps in the loaded project"));
    resources.add(resource(
        "uti://project/tilesets", "Project Tilesets", "Tilesets used by the active map"));
    resources.add(resource(
        "uti://project/assets", "Project Assets", "Spritesheets, sounds, animations, emitters, blueprints, and tilesets"));
    resources.add(resource(
        "uti://map/current/layers", "Current Map Layers", "Layers on the active map"));
    resources.add(resource(
        "uti://map/current/entities", "Current Map Entities", "Entities on the active map"));
    resources.add(resource(
        "uti://editor/state", "Editor State", "Active map, camera, selection, and undo state"));
    resources.add(resource(
        "uti://editor/logs", "Editor Logs", "Recent editor log messages and error summary"));
    resources.add(resource(
        "uti://editor/property-docs", "Property Documentation", "Catalog of annotated map object properties, categories, data types, defaults, and type metadata"));
    return Json.createObjectBuilder().add("resources", resources).build();
  }

  public static JsonObject getResourceTemplatesList() {
    JsonArrayBuilder templates = Json.createArrayBuilder();
    templates.add(resourceTemplate(
        "uti://map/{name}/layers", "Map Layers", "Layers on a project map selected by name"));
    templates.add(resourceTemplate(
        "uti://map/{name}/entities", "Map Entities", "Entities on a project map selected by name"));
    return Json.createObjectBuilder().add("resourceTemplates", templates).build();
  }

  public static JsonObject handleReadResource(String uriStr) {
    if (uriStr == null) {
      return Json.createObjectBuilder().add("error", "URI cannot be null").build();
    }

    URI uri;
    try {
      uri = URI.create(uriStr);
    } catch (Exception e) {
      return Json.createObjectBuilder().add("error", "Invalid URI format: " + e.getMessage()).build();
    }

    String path = uri.getPath();
    String host = uri.getHost();
    String fullPath = (host != null ? host : "") + (path != null ? path : "");

    if (fullPath.equals("project/info")) {
      return getProjectInfo();
    } else if (fullPath.equals("project/maps")) {
      return getProjectMaps();
    } else if (fullPath.equals("project/tilesets")) {
      return getProjectTilesets();
    } else if (fullPath.equals("project/assets")) {
      return getProjectAssets();
    } else if (fullPath.equals("editor/state")) {
      return getEditorState();
    } else if (fullPath.equals("editor/logs")) {
      return getEditorLogs();
    } else if (fullPath.equals("editor/property-docs")) {
      return getPropertyDocs();
    } else if (fullPath.startsWith("map/")) {
      String rest = fullPath.substring(4);
      if (rest.endsWith("/layers")) {
        String mapName = rest.substring(0, rest.length() - "/layers".length());
        return getMapLayers(mapName);
      } else if (rest.endsWith("/entities")) {
        String mapName = rest.substring(0, rest.length() - "/entities".length());
        return getMapEntities(mapName);
      }
    }

    return Json.createObjectBuilder().add("error", "Unknown resource URI: " + uriStr).build();
  }

  public static JsonObject getProjectInfo() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    String currentFile = Editor.instance().getCurrentResourceFile() != null
        ? Editor.instance().getCurrentResourceFile().toString()
        : null;
    builder.add("gameFile", currentFile != null ? currentFile : "");
    builder.add("projectPath", Editor.instance().getProjectPath() != null ? Editor.instance().getProjectPath().toString() : "");

    JsonArrayBuilder mapsArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map != null && map.getName() != null) {
          mapsArr.add(map.getName());
        }
      }
    }
    builder.add("maps", mapsArr);

    JsonArrayBuilder spritesArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (SpritesheetResource spr : Editor.instance().getGameFile().getSpriteSheets()) {
        if (spr != null && spr.getName() != null) {
          spritesArr.add(spr.getName());
        }
      }
    }
    builder.add("spritesheets", spritesArr);

    JsonArrayBuilder soundsArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (SoundResource snd : Editor.instance().getGameFile().getSounds()) {
        if (snd != null && snd.getName() != null) {
          soundsArr.add(snd.getName());
        }
      }
    }
    builder.add("sounds", soundsArr);

    return builder.build();
  }

  public static JsonObject getProjectMaps() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonArrayBuilder mapsArr = Json.createArrayBuilder();

    if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map == null) continue;
        int widthInTiles = map.getSizeInTiles() != null ? map.getSizeInTiles().width : map.getWidth();
        int heightInTiles = map.getSizeInTiles() != null ? map.getSizeInTiles().height : map.getHeight();
        int tileW = map.getTileSize() != null ? map.getTileSize().width : 16;
        int tileH = map.getTileSize() != null ? map.getTileSize().height : 16;
        int pixelW = map.getSizeInPixels() != null ? map.getSizeInPixels().width : widthInTiles * tileW;
        int pixelH = map.getSizeInPixels() != null ? map.getSizeInPixels().height : heightInTiles * tileH;

        JsonObjectBuilder mapBuilder = Json.createObjectBuilder();
        mapBuilder.add("name", map.getName() != null ? map.getName() : "");
        mapBuilder.add("width", widthInTiles);
        mapBuilder.add("height", heightInTiles);
        mapBuilder.add("widthInTiles", widthInTiles);
        mapBuilder.add("heightInTiles", heightInTiles);
        mapBuilder.add("widthInPixels", pixelW);
        mapBuilder.add("heightInPixels", pixelH);
        mapBuilder.add("pixelWidth", pixelW);
        mapBuilder.add("pixelHeight", pixelH);
        mapBuilder.add("tileWidth", tileW);
        mapBuilder.add("tileHeight", tileH);
        mapsArr.add(mapBuilder);
      }
    }

    builder.add("maps", mapsArr);
    return builder.build();
  }

  public static JsonObject getProjectTilesets() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonArrayBuilder tilesetsArr = Json.createArrayBuilder();

    IMap map = Game.world().environment() != null ? Game.world().environment().getMap() : null;
    if (map != null && map.getTilesets() != null) {
      for (ITileset ts : map.getTilesets()) {
        if (ts == null) continue;
        JsonObjectBuilder tsBuilder = Json.createObjectBuilder();
        tsBuilder.add("name", ts.getName() != null ? ts.getName() : "");
        tsBuilder.add("firstGridId", ts.getFirstGridId());
        tsBuilder.add("tileWidth", ts.getTileWidth());
        tsBuilder.add("tileHeight", ts.getTileHeight());
        tsBuilder.add("imageSource", ts.getImage() != null && ts.getImage().getSource() != null ? ts.getImage().getSource() : "");
        tilesetsArr.add(tsBuilder);
      }
    }

    builder.add("tilesets", tilesetsArr);
    return builder.build();
  }

  public static JsonObject getProjectAssets() {
    JsonObject result = McpAssetHandler.handle("list-resources", Json.createObjectBuilder().build());
    return Json.createObjectBuilder()
        .add("assets", result.containsKey("resources")
            ? result.getJsonArray("resources")
            : Json.createArrayBuilder().build())
        .build();
  }

  public static JsonObject getEditorState() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    IMap currentMap = Game.world().environment() != null ? Game.world().environment().getMap() : null;

    builder.add("activeMap", currentMap != null && currentMap.getName() != null ? currentMap.getName() : "");
    builder.add("zoom", Game.world().camera() != null ? Game.world().camera().getZoom() : 1.0f);
    
    if (Game.world().camera() != null && Game.world().camera().getFocus() != null) {
      builder.add("cameraX", Game.world().camera().getFocus().getX());
      builder.add("cameraY", Game.world().camera().getFocus().getY());
    }

    JsonArrayBuilder selectedIds = Json.createArrayBuilder();
    if (Editor.instance().getMapComponent() != null && Editor.instance().getMapComponent().getSelectedMapObjects() != null) {
      for (IMapObject obj : Editor.instance().getMapComponent().getSelectedMapObjects()) {
        if (obj != null) {
          selectedIds.add(obj.getId());
        }
      }
    }
    builder.add("selectedEntities", selectedIds);

    boolean canUndo = false;
    boolean canRedo = false;
    if (currentMap != null) {
      UndoManager undoMgr = UndoManager.forMap(currentMap);
      if (undoMgr != null) {
        canUndo = undoMgr.canUndo();
        canRedo = undoMgr.canRedo();
      }
    }
    builder.add("canUndo", canUndo);
    builder.add("canRedo", canRedo);

    return builder.build();
  }

  public static JsonObject getEditorLogs() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    LogHandler logHandler = UI.getConsole() != null ? UI.getConsole().getLogHandler() : null;

    if (logHandler != null) {
      builder.add("warningCount", logHandler.getWarningCount());
      builder.add("errorCount", logHandler.getErrorCount());
      builder.add("latestErrorStack", logHandler.getLatestErrorStack() != null ? logHandler.getLatestErrorStack() : "");

      JsonArrayBuilder logsArr = Json.createArrayBuilder();
      for (LogHandler.LogEntry entry : logHandler.getRecentLogs()) {
        logsArr.add(Json.createObjectBuilder()
            .add("level", entry.level())
            .add("message", entry.message())
            .add("timestamp", entry.timestamp()));
      }
      builder.add("logs", logsArr);
    } else {
      builder.add("warningCount", 0);
      builder.add("errorCount", 0);
      builder.add("latestErrorStack", "");
      builder.add("logs", Json.createArrayBuilder().build());
    }
    return builder.build();
  }

  public static JsonObject getPropertyDocs() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    JsonArrayBuilder typesArr = Json.createArrayBuilder();
    for (TmxPropertyMetadataRegistry.TypeMetadata t : TmxPropertyMetadataRegistry.getAllTypes()) {
      typesArr.add(Json.createObjectBuilder()
          .add("type", t.typeName())
          .add("name", t.displayName())
          .add("description", t.description()));
    }
    builder.add("types", typesArr);

    JsonArrayBuilder propsArr = Json.createArrayBuilder();
    for (TmxPropertyMetadataRegistry.PropertyMetadata p : TmxPropertyMetadataRegistry.getAllProperties()) {
      propsArr.add(Json.createObjectBuilder()
          .add("name", p.name())
          .add("description", p.description())
          .add("category", p.category())
          .add("type", p.type())
          .add("defaultValue", p.defaultValue()));
    }
    builder.add("properties", propsArr);

    return builder.build();
  }

  public static JsonObject getMapLayers(String mapName) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonArrayBuilder layersArr = Json.createArrayBuilder();

    IMap targetMap = getTargetMap(mapName);
    if (targetMap != null && targetMap.getRenderLayers() != null) {
      for (ILayer layer : targetMap.getRenderLayers()) {
        if (layer == null) continue;
        JsonObjectBuilder lBuilder = Json.createObjectBuilder();
        lBuilder.add("name", layer.getName() != null ? layer.getName() : "");
        lBuilder.add("type", layer.getClass().getSimpleName());
        lBuilder.add("visible", layer.isVisible());
        lBuilder.add("opacity", layer.getOpacity());
        layersArr.add(lBuilder);
      }
    }

    builder.add("layers", layersArr);
    return builder.build();
  }

  public static JsonObject getMapEntities(String mapName) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonArrayBuilder entitiesArr = Json.createArrayBuilder();

    IMap targetMap = getTargetMap(mapName);

    if (targetMap != null) {
      for (IMapObject obj : targetMap.getMapObjects()) {
        if (obj == null) continue;
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        objBuilder.add("id", obj.getId());
        objBuilder.add("name", obj.getName() != null ? obj.getName() : "");
        objBuilder.add("type", obj.getType() != null ? obj.getType() : "");
        objBuilder.add("x", obj.getX());
        objBuilder.add("y", obj.getY());
        objBuilder.add("width", obj.getWidth());
        objBuilder.add("height", obj.getHeight());
        objBuilder.add("layer", obj.getLayer() != null && obj.getLayer().getName() != null ? obj.getLayer().getName() : "");

        JsonObjectBuilder propsBuilder = Json.createObjectBuilder();
        for (Map.Entry<String, ICustomProperty> entry : obj.getProperties().entrySet()) {
          if (entry.getKey() != null && entry.getValue() != null) {
            propsBuilder.add(entry.getKey(), entry.getValue().getAsString() != null ? entry.getValue().getAsString() : "");
          }
        }
        objBuilder.add("properties", propsBuilder);
        entitiesArr.add(objBuilder);
      }
    }

    builder.add("entities", entitiesArr);
    return builder.build();
  }

  private static IMap getTargetMap(String mapName) {
    if ("current".equalsIgnoreCase(mapName)) {
      return Game.world().environment() != null ? Game.world().environment().getMap() : null;
    } else if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map != null && mapName.equalsIgnoreCase(map.getName())) {
          return map;
        }
      }
    }
    return null;
  }

  private static JsonObject resource(String uri, String name, String description) {
    return Json.createObjectBuilder()
        .add("uri", uri)
        .add("name", name)
        .add("description", description)
        .add("mimeType", "application/json")
        .build();
  }

  private static JsonObject resourceTemplate(String uriTemplate, String name, String description) {
    return Json.createObjectBuilder()
        .add("uriTemplate", uriTemplate)
        .add("name", name)
        .add("description", description)
        .add("mimeType", "application/json")
        .build();
  }
}
