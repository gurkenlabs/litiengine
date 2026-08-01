package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.SpriteVariantSelector;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements Level A semantic tools for stateless, batch-capable, LLM-optimized
 * map editing. Every operation is fully self-contained: explicit mapId, entityIds,
 * and coordinate parameters — zero reliance on implicit editor selection or
 * clipboard state.
 */
public final class McpSemanticHandler {
  private McpSemanticHandler() {}

  public static JsonObject handleSemanticTool(String name, JsonObject args) {
    if (args == null) {
      args = Json.createObjectBuilder().build();
    }

    return switch (name) {
      case "get_project_context" -> getProjectContext();
      case "get_map" -> getMap(args);
      case "query_region" -> queryRegion(args);
      case "search_entities" -> searchEntities(args);
      case "search_tiles" -> searchTiles(args);
      case "search_blueprints" -> searchBlueprints(args);
      case "create_entities" -> createEntities(args);
      case "update_entities" -> updateEntities(args);
      case "duplicate_entities" -> duplicateEntities(args);
      case "delete_entities" -> deleteEntities(args);
      case "instantiate_blueprints" -> instantiateBlueprints(args);
      case "edit_tiles" -> editTiles(args);
      case "fill_region" -> fillRegion(args);
      case "fill_regions" -> fillRegions(args);
      case "paint_terrain" -> paintTerrain(args);
      case "render_map" -> renderMap(args);
      case "render_region" -> renderRegion(args);
      case "analyze_map" -> analyzeMap(args);
      case "analyze_collision" -> analyzeCollision(args);
      case "analyze_playability", "get_navigation_graph" -> analyzePlayability(args);
      case "render_playability" -> renderPlayability(args);
      case "preview_changes" -> previewChanges(args);
      case "apply_changes" -> applyChanges(args);
      case "set_ambient_light" -> setAmbientLight(args);
      case "scatter_floor_details" -> scatterFloorDetails(args);
      case "analyze_project" -> analyzeProject(args);
      case "plan_map_changes" -> planMapChanges(args);
      case "validate_map_changes", "validate_map_plan" -> validateMapPlan(args);
      case "render_tileset" -> McpTilesetHandler.handle("render-tileset", tileInspectionArgs(args));
      case "find_tile_usage" -> McpTilesetHandler.handle("find-tile-usage", tileInspectionArgs(args));
      case "render_tile_context" -> McpTilesetHandler.handle("render-tile-context", tileInspectionArgs(args));
      case "preview_tile_edits" -> McpTilesetHandler.handle("preview-tile-edits", tileInspectionArgs(args));
      default -> McpResponseFactory.createError(
          "UNKNOWN_TOOL", "Unknown semantic tool: " + name, false, null);
    };
  }

  /** Accept snake_case aliases without weakening the canonical Level B schemas. */
  private static JsonObject tileInspectionArgs(JsonObject args) {
    JsonObjectBuilder normalized = Json.createObjectBuilder();
    args.forEach(normalized::add);
    copySnakeCase(args, normalized, "map_id", "mapId");
    copySnakeCase(args, normalized, "tile_id", "tileId");
    copySnakeCase(args, normalized, "selected_layer", "layer");
    copySnakeCase(args, normalized, "tile_edits", "edits");
    return normalized.build();
  }

  private static void copySnakeCase(JsonObject source, JsonObjectBuilder target, String snake, String camel) {
    if (!source.containsKey(camel) && source.containsKey(snake)) target.add(camel, source.get(snake));
  }

  // ── Context & Inspection ──────────────────────────────────────────

  private static JsonObject getProjectContext() {
    JsonObjectBuilder builder = Json.createObjectBuilder().add("success", true);

    JsonArrayBuilder mapsArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map == null || map.getName() == null) continue;
        mapsArr.add(Json.createObjectBuilder()
            .add("name", map.getName())
            .add("widthInTiles", map.getSizeInTiles() != null ? map.getSizeInTiles().width : map.getWidth())
            .add("heightInTiles", map.getSizeInTiles() != null ? map.getSizeInTiles().height : map.getHeight())
            .add("tileWidth", map.getTileSize() != null ? map.getTileSize().width : 16)
            .add("tileHeight", map.getTileSize() != null ? map.getTileSize().height : 16)
            .add("revision", McpRevisionTracker.getRevision(map)));
      }
    }
    builder.add("maps", mapsArr);

    JsonArrayBuilder tilesetsArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (var ts : Editor.instance().getGameFile().getTilesets()) {
        if (ts != null && ts.getName() != null) {
          tilesetsArr.add(Json.createObjectBuilder()
              .add("name", ts.getName())
              .add("tileWidth", ts.getTileWidth())
              .add("tileHeight", ts.getTileHeight()));
        }
      }
    }
    builder.add("tilesets", tilesetsArr);

    JsonArrayBuilder blueprintsArr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (Blueprint bp : Editor.instance().getGameFile().getBluePrints()) {
        if (bp != null && bp.getName() != null) {
          blueprintsArr.add(Json.createObjectBuilder()
              .add("name", bp.getName()));
        }
      }
    }
    builder.add("blueprints", blueprintsArr);

    JsonArrayBuilder materialsArr = Json.createArrayBuilder();
    for (var m : de.gurkenlabs.litiengine.entities.Material.getMaterials()) {
      if (m != null && m.getName() != null) {
        materialsArr.add(m.getName());
      }
    }
    builder.add("materials", materialsArr);

    JsonObjectBuilder spritesheetsObj = Json.createObjectBuilder();
    JsonArrayBuilder propSprites = Json.createArrayBuilder();
    JsonArrayBuilder creatureSprites = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      var resources = Editor.instance().getGameFile().getSpriteSheets();
      var props = SpriteVariantSelector.selectBasePropResources(resources);
      var creatures = SpriteVariantSelector.selectBaseCreatureResources(resources);
      props.keySet().stream().sorted().forEach(propSprites::add);
      creatures.keySet().stream().sorted().forEach(creatureSprites::add);
    }
    spritesheetsObj.add("props", propSprites);
    spritesheetsObj.add("creatures", creatureSprites);
    builder.add("spritesheets", spritesheetsObj);

    return builder.build();
  }

  private static JsonObject getMap(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    JsonObjectBuilder builder = Json.createObjectBuilder()
        .add("success", true)
        .add("name", map.getName())
        .add("widthInTiles", map.getSizeInTiles() != null ? map.getSizeInTiles().width : map.getWidth())
        .add("heightInTiles", map.getSizeInTiles() != null ? map.getSizeInTiles().height : map.getHeight())
        .add("widthInPixels", map.getSizeInPixels() != null ? map.getSizeInPixels().width : 0)
        .add("heightInPixels", map.getSizeInPixels() != null ? map.getSizeInPixels().height : 0)
        .add("tileWidth", map.getTileSize() != null ? map.getTileSize().width : 16)
        .add("tileHeight", map.getTileSize() != null ? map.getTileSize().height : 16)
        .add("revision", McpRevisionTracker.getRevision(map));

    JsonArrayBuilder layersArr = Json.createArrayBuilder();
    for (ILayer layer : map.getRenderLayers()) {
      if (layer == null) continue;
      layersArr.add(Json.createObjectBuilder()
          .add("name", layer.getName() != null ? layer.getName() : "")
          .add("type", layer instanceof IMapObjectLayer ? "objectgroup" :
              layer instanceof ITileLayer ? "tilelayer" : layer.getClass().getSimpleName())
          .add("visible", layer.isVisible()));
    }
    builder.add("layers", layersArr);
    return builder.build();
  }

  private static JsonObject queryRegion(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    double x = getDouble(args, "x", 0.0);
    double y = getDouble(args, "y", 0.0);
    double w = getDouble(args, "width", map.getSizeInPixels() != null ? map.getSizeInPixels().width : 512);
    double h = getDouble(args, "height", map.getSizeInPixels() != null ? map.getSizeInPixels().height : 512);
    boolean includeEntities = getBoolean(args, "includeEntities", true);
    boolean includeTiles = getBoolean(args, "includeTiles", true);

    Rectangle2D queryBounds = new Rectangle2D.Double(x, y, w, h);
    JsonObjectBuilder builder = Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("revision", McpRevisionTracker.getRevision(map))
        .add("queryBounds", boundsJson(x, y, w, h));

    if (includeEntities) {
      JsonArrayBuilder entitiesArr = Json.createArrayBuilder();
      for (IMapObjectLayer layer : map.getMapObjectLayers()) {
        if (layer == null) continue;
        for (IMapObject obj : layer.getMapObjects()) {
          if (obj != null && queryBounds.intersects(obj.getBoundingBox())) {
            entitiesArr.add(serializeEntity(obj));
          }
        }
      }
      builder.add("entities", entitiesArr);
    }

    if (includeTiles) {
      JsonArrayBuilder tilesArr = Json.createArrayBuilder();
      int tileW = map.getTileSize().width;
      int tileH = map.getTileSize().height;
      int minCol = Math.max(0, (int) Math.floor(x / tileW));
      int minRow = Math.max(0, (int) Math.floor(y / tileH));
      int maxCol = Math.min(map.getWidth() - 1, (int) Math.ceil((x + w) / tileW));
      int maxRow = Math.min(map.getHeight() - 1, (int) Math.ceil((y + h) / tileH));

      for (ITileLayer tileLayer : map.getTileLayers()) {
        if (tileLayer == null || !tileLayer.isVisible()) continue;
        for (int col = minCol; col <= maxCol; col++) {
          for (int row = minRow; row <= maxRow; row++) {
            ITile tile = tileLayer.getTile(col, row);
            if (tile != null && tile.getGridId() > 0) {
              tilesArr.add(Json.createObjectBuilder()
                  .add("layer", tileLayer.getName() != null ? tileLayer.getName() : "")
                  .add("x", col).add("y", row).add("gid", tile.getGridId()));
            }
          }
        }
      }
      builder.add("tiles", tilesArr);
    }
    return builder.build();
  }

  // ── Search ────────────────────────────────────────────────────────

  private static JsonObject searchEntities(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    String query = getString(args, "query", "").toLowerCase();
    String typeFilter = getString(args, "type", null);
    String layerFilter = getString(args, "layer", null);

    JsonArrayBuilder arr = Json.createArrayBuilder();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null) continue;
      if (layerFilter != null && !layerFilter.equalsIgnoreCase(layer.getName())) continue;
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj == null) continue;
        if (typeFilter != null && !typeFilter.equalsIgnoreCase(obj.getType())) continue;
        if (!query.isBlank()) {
          String name = obj.getName() != null ? obj.getName().toLowerCase() : "";
          if (!name.contains(query) && !String.valueOf(obj.getId()).equals(query)) continue;
        }
        arr.add(serializeEntity(obj));
      }
    }
    return Json.createObjectBuilder().add("success", true).add("entities", arr).build();
  }

  private static JsonObject searchTiles(JsonObject args) {
    String query = getString(args, "query", "").toLowerCase();
    JsonArrayBuilder arr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (var ts : Editor.instance().getGameFile().getTilesets()) {
        if (ts != null && ts.getName() != null
            && (query.isBlank() || ts.getName().toLowerCase().contains(query))) {
          arr.add(Json.createObjectBuilder()
              .add("name", ts.getName())
              .add("tileWidth", ts.getTileWidth())
              .add("tileHeight", ts.getTileHeight()));
        }
      }
    }
    return Json.createObjectBuilder().add("success", true).add("tilesets", arr).build();
  }

  private static JsonObject searchBlueprints(JsonObject args) {
    String query = getString(args, "query", "").toLowerCase();
    JsonArrayBuilder arr = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (Blueprint bp : Editor.instance().getGameFile().getBluePrints()) {
        if (bp != null && bp.getName() != null
            && (query.isBlank() || bp.getName().toLowerCase().contains(query))) {
          arr.add(Json.createObjectBuilder().add("name", bp.getName()));
        }
      }
    }
    return Json.createObjectBuilder().add("success", true).add("blueprints", arr).build();
  }

  // ── Entity Mutations ──────────────────────────────────────────────

  private static JsonObject createEntities(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    JsonArray entities = args.getJsonArray("entities");
    if (entities == null || entities.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No entities provided", true, null);
    }

    List<JsonObject> definitions = new ArrayList<>(entities.size());
    for (int index = 0; index < entities.size(); index++) {
      JsonValue value = entities.get(index);
      JsonObject definition = parseEntityDefinition(value);
      if (definition != null) {
        definitions.add(definition);
        continue;
      }
      if (value.getValueType() == JsonValue.ValueType.STRING) {
        return McpResponseFactory.createError(
            "INVALID_ARGUMENTS",
            "Entity at index " + index + " must be a JSON object or a JSON-encoded object string",
            true,
            null);
      }
      {
        return McpResponseFactory.createError(
            "INVALID_ARGUMENTS",
            "Entity at index " + index + " must be an object, not "
                + value.getValueType().name().toLowerCase(),
            true,
            null);
      }
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    List<Integer> createdIds = new ArrayList<>();

    for (int i = 0; i < definitions.size(); i++) {
      JsonObject entDef = definitions.get(i);
      MapObject mo = new MapObject();
      mo.setType(getString(entDef, "type", "PROP"));
      mo.setName(getString(entDef, "name", "entity_" + (i + 1)));
      mo.setX(getFloat(entDef, "x", 0f));
      mo.setY(getFloat(entDef, "y", 0f));
      mo.setWidth(getFloat(entDef, "width", 16f));
      mo.setHeight(getFloat(entDef, "height", 16f));
      McpToolHandler.applyAdditionalProperties(
          mo, entDef.get("properties") instanceof JsonObject properties ? properties : null);

      if (entDef.containsKey("spritesheetName")) {
        String sprite = getString(entDef, "spritesheetName", "");
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.SPRITESHEETNAME, sprite);
      }
      if (entDef.containsKey("material")) {
        String material = getString(entDef, "material", "");
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.PROP_MATERIAL, material);
      }

      boolean enableCollision = getBoolean(entDef, "collision", true);
      if ("PROP".equalsIgnoreCase(mo.getType()) && enableCollision) {
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.COLLISION, true);
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.COLLISION_TYPE, getString(entDef, "collisionType", "STATIC"));
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.COLLISIONBOX_WIDTH, String.valueOf(mo.getWidth()));
        mo.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.COLLISIONBOX_HEIGHT, String.valueOf(mo.getHeight()));
      }

      boolean isActiveMap = Editor.instance().getGameFile() != null
          && Editor.instance().getMapComponent() != null
          && Game.world().environment() != null
          && Game.world().environment().getMap() != null
          && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

      IMapObjectLayer targetLayer = resolveObjectLayer(map, getString(entDef, "layer", null));
      if (targetLayer != null) {
        McpToolHandler.assignNextMapId(mo);
        targetLayer.addMapObject(mo);
        createdIds.add(mo.getId());

        if (isActiveMap) {
          Game.world().environment().loadFromMap(mo.getId());
          de.gurkenlabs.utiliti.controller.UndoManager.instance().mapObjectAdded(mo);
        }
      }
    }

    boolean isActiveMap = Editor.instance().getGameFile() != null
        && Editor.instance().getMapComponent() != null
        && Game.world().environment() != null
        && Game.world().environment().getMap() != null
        && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

    if (isActiveMap && !createdIds.isEmpty()) {
      IMapObject lastCreated = map.getMapObject(createdIds.getLast());
      McpToolHandler.refreshInspectorAfterMutation(lastCreated);
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, createdIds, null, null, null, null,
        "create-entities-" + newRev);
  }

  static JsonObject parseEntityDefinition(JsonValue value) {
    if (value == null) return null;
    if (value.getValueType() == JsonValue.ValueType.OBJECT) return value.asJsonObject();
    if (value.getValueType() != JsonValue.ValueType.STRING) return null;
    try (var reader = Json.createReader(new StringReader(((JsonString) value).getString()))) {
      JsonValue parsed = reader.readValue();
      return parsed.getValueType() == JsonValue.ValueType.OBJECT ? parsed.asJsonObject() : null;
    } catch (RuntimeException _) {
      return null;
    }
  }

  private static JsonObject updateEntities(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    JsonArray updates = args.getJsonArray("updates");
    if (updates == null || updates.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No updates provided", true, null);
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    List<Integer> updatedIds = new ArrayList<>();

    for (int i = 0; i < updates.size(); i++) {
      JsonObject upd = updates.getJsonObject(i);
      int id = getInt(upd, "id", -1);
      IMapObject target = findMapObject(map, id);
      if (target == null) continue;

      if (upd.containsKey("x")) target.setX(getFloat(upd, "x", target.getX()));
      if (upd.containsKey("y")) target.setY(getFloat(upd, "y", target.getY()));
      if (upd.containsKey("width")) target.setWidth(getFloat(upd, "width", target.getWidth()));
      if (upd.containsKey("height")) target.setHeight(getFloat(upd, "height", target.getHeight()));
      if (upd.containsKey("name")) target.setName(getString(upd, "name", target.getName()));
      updatedIds.add(id);
    }

    boolean isActiveMap = Editor.instance().getGameFile() != null
        && Editor.instance().getMapComponent() != null
        && Game.world().environment() != null
        && Game.world().environment().getMap() != null
        && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

    if (isActiveMap && !updatedIds.isEmpty()) {
      for (int id : updatedIds) {
        Game.world().environment().reloadFromMap(id);
      }
      IMapObject lastUpdated = map.getMapObject(updatedIds.getLast());
      McpToolHandler.refreshInspectorAfterMutation(lastUpdated);
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, updatedIds, null, null, null,
        "update-entities-" + newRev);
  }

  private static JsonObject duplicateEntities(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    JsonArray entityIdsArr = args.getJsonArray("entityIds");
    if (entityIdsArr == null || entityIdsArr.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No entityIds provided", true, null);
    }

    JsonObject offset = args.containsKey("offset") ? args.getJsonObject("offset") : null;
    double dx = offset != null ? getDouble(offset, "x", 32.0) : 32.0;
    double dy = offset != null ? getDouble(offset, "y", 0.0) : 0.0;

    long prevRev = McpRevisionTracker.getRevision(map);
    List<Integer> createdIds = new ArrayList<>();
    boolean isActiveMap = Editor.instance().getGameFile() != null
        && Editor.instance().getMapComponent() != null
        && Game.world().environment() != null
        && Game.world().environment().getMap() != null
        && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

    for (int i = 0; i < entityIdsArr.size(); i++) {
      int id = entityIdsArr.getInt(i);
      IMapObject src = findMapObject(map, id);
      if (src == null || !(src instanceof MapObject srcMo)) continue;

      MapObject dup = new MapObject(srcMo);
      dup.setX((float) (src.getX() + dx));
      dup.setY((float) (src.getY() + dy));

      IMapObjectLayer layer = findLayerContaining(map, id);
      if (layer != null) {
        McpToolHandler.assignNextMapId(dup);
        layer.addMapObject(dup);
        createdIds.add(dup.getId());

        if (isActiveMap) {
          Game.world().environment().loadFromMap(dup.getId());
          de.gurkenlabs.utiliti.controller.UndoManager.instance().mapObjectAdded(dup);
        }
      }
    }

    if (isActiveMap && !createdIds.isEmpty()) {
      IMapObject lastCreated = map.getMapObject(createdIds.getLast());
      McpToolHandler.refreshInspectorAfterMutation(lastCreated);
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, createdIds, null, null, null, null,
        "duplicate-entities-" + newRev);
  }

  private static JsonObject deleteEntities(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    Set<IMapObject> objectsToDelete = new LinkedHashSet<>();

    // 1. Collect by entityIds (supports array of numbers or strings)
    if (args.containsKey("entityIds") && !args.isNull("entityIds")) {
      JsonArray entityIdsArr = args.getJsonArray("entityIds");
      if (entityIdsArr != null) {
        for (int i = 0; i < entityIdsArr.size(); i++) {
          JsonValue val = entityIdsArr.get(i);
          if (val.getValueType() == JsonValue.ValueType.NUMBER) {
            int id = ((JsonNumber) val).intValue();
            IMapObject obj = map.getMapObject(id);
            if (obj != null) objectsToDelete.add(obj);
          } else if (val.getValueType() == JsonValue.ValueType.STRING) {
            String str = ((JsonString) val).getString();
            try {
              int id = Integer.parseInt(str.trim());
              IMapObject obj = map.getMapObject(id);
              if (obj != null) objectsToDelete.add(obj);
            } catch (NumberFormatException _) {
              for (IMapObject obj : map.getMapObjects()) {
                if (obj != null && str.equals(obj.getName())) {
                  objectsToDelete.add(obj);
                }
              }
            }
          }
        }
      }
    }

    // 2. Collect by names (array of entity string names)
    if (args.containsKey("names") && !args.isNull("names")) {
      JsonArray namesArr = args.getJsonArray("names");
      if (namesArr != null) {
        for (int i = 0; i < namesArr.size(); i++) {
          String name = namesArr.getString(i);
          for (IMapObject obj : map.getMapObjects()) {
            if (obj != null && name.equals(obj.getName())) {
              objectsToDelete.add(obj);
            }
          }
        }
      }
    }

    // 3. Collect by type, layer, or all
    String typeFilter = getString(args, "type", null);
    String layerFilter = getString(args, "layer", null);
    boolean deleteAll = getBoolean(args, "all", false);

    if (typeFilter != null || layerFilter != null || deleteAll) {
      for (IMapObjectLayer layer : map.getMapObjectLayers()) {
        if (layer == null) continue;
        if (layerFilter != null && !layerFilter.equalsIgnoreCase(layer.getName())) {
          continue;
        }
        for (IMapObject obj : layer.getMapObjects()) {
          if (obj == null) continue;
          if (typeFilter != null && !typeFilter.equalsIgnoreCase(obj.getType())) {
            continue;
          }
          objectsToDelete.add(obj);
        }
      }
    }

    if (objectsToDelete.isEmpty()) {
      return McpResponseFactory.createError(
          "ENTITIES_NOT_FOUND", "No matching entities found for deletion", true, null);
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    List<Integer> deletedIds = new ArrayList<>();
    boolean isActiveMap = Editor.instance().getGameFile() != null
        && Editor.instance().getMapComponent() != null
        && Game.world().environment() != null
        && Game.world().environment().getMap() != null
        && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

    for (IMapObject obj : objectsToDelete) {
      deletedIds.add(obj.getId());
      if (isActiveMap) {
        // Delete using MapComponent to remove live environment entity, UI controller, and focus
        Editor.instance().getMapComponent().delete(obj);
      } else {
        map.removeMapObject(obj.getId());
      }
    }

    if (isActiveMap) {
      McpToolHandler.refreshInspectorAfterMutation(null);
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, deletedIds, null, null,
        "delete-entities-" + newRev);
  }

  private static JsonObject instantiateBlueprints(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String blueprintName = getString(args, "blueprintName", null);
    Blueprint blueprint = null;
    if (blueprintName != null && Editor.instance().getGameFile() != null) {
      for (Blueprint bp : Editor.instance().getGameFile().getBluePrints()) {
        if (bp != null && blueprintName.equalsIgnoreCase(bp.getName())) {
          blueprint = bp;
          break;
        }
      }
    }
    if (blueprint == null) {
      return McpResponseFactory.createError(
          "ASSET_NOT_FOUND", "Blueprint not found: " + blueprintName, true, null);
    }

    JsonArray instances = args.getJsonArray("instances");
    if (instances == null || instances.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No target instances provided", true, null);
    }

    IMapObjectLayer targetLayer = resolveObjectLayer(map, getString(args, "layer", null));
    if (targetLayer == null && !map.getMapObjectLayers().isEmpty()) {
      targetLayer = map.getMapObjectLayers().getFirst();
    }
    if (targetLayer == null) {
      return McpResponseFactory.createError("LAYER_NOT_FOUND", "No object layer available to place blueprint entities", true, null);
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    List<Integer> createdIds = new ArrayList<>();
    boolean isActiveMap = Editor.instance().getGameFile() != null
        && Editor.instance().getMapComponent() != null
        && Game.world().environment() != null
        && Game.world().environment().getMap() != null
        && map.getName().equalsIgnoreCase(Game.world().environment().getMap().getName());

    for (int i = 0; i < instances.size(); i++) {
      JsonObject inst = instances.getJsonObject(i);
      float x = getFloat(inst, "x", 0f);
      float y = getFloat(inst, "y", 0f);
      List<IMapObject> builtObjects = blueprint.build(x, y);
      if (builtObjects != null) {
        for (IMapObject obj : builtObjects) {
          if (obj != null) {
            if (obj instanceof MapObject mapObj) {
              McpToolHandler.assignNextMapId(mapObj);
            }
            targetLayer.addMapObject(obj);
            createdIds.add(obj.getId());

            if (isActiveMap) {
              Game.world().environment().loadFromMap(obj.getId());
              de.gurkenlabs.utiliti.controller.UndoManager.instance().mapObjectAdded(obj);
            }
          }
        }
      }
    }

    if (isActiveMap && !createdIds.isEmpty()) {
      IMapObject lastCreated = map.getMapObject(createdIds.getLast());
      McpToolHandler.refreshInspectorAfterMutation(lastCreated);
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, createdIds, null, null, null, null,
        "instantiate-blueprints-" + newRev);
  }

  // ── Tile Mutations ────────────────────────────────────────────────

  private static JsonObject editTiles(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String layerName = getString(args, "layer", null);
    ITileLayer tileLayer = resolveTileLayer(map, layerName);
    if (tileLayer == null) {
      return McpResponseFactory.createError(
          "LAYER_NOT_FOUND", "Tile layer not found: " + layerName, true, null);
    }

    JsonArray tilesArr = args.getJsonArray("tiles");
    if (tilesArr == null || tilesArr.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No tiles provided", true, null);
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    for (int i = 0; i < tilesArr.size(); i++) {
      JsonObject t = tilesArr.getJsonObject(i);
      tileLayer.setTile(getInt(t, "x", 0), getInt(t, "y", 0), getInt(t, "gid", 0));
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, null, null, null,
        "edit-tiles-" + newRev);
  }

  private static JsonObject fillRegion(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String layerName = getString(args, "layer", null);
    ITileLayer tileLayer = resolveTileLayer(map, layerName);
    if (tileLayer == null) {
      return McpResponseFactory.createError(
          "LAYER_NOT_FOUND", "Tile layer not found: " + layerName, true, null);
    }

    int startX = getInt(args, "x", 0);
    int startY = getInt(args, "y", 0);
    int width = getInt(args, "width", 1);
    int height = getInt(args, "height", 1);
    int gid = getInt(args, "gid", 0);

    long prevRev = McpRevisionTracker.getRevision(map);
    for (int col = startX; col < startX + width; col++) {
      for (int row = startY; row < startY + height; row++) {
        if (col >= 0 && col < map.getWidth() && row >= 0 && row < map.getHeight()) {
          tileLayer.setTile(col, row, gid);
        }
      }
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, null, null, null,
        "fill-region-" + newRev);
  }

  private static JsonObject fillRegions(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) return mapNotFound(mapId);
    if (!checkRevision(map, args)) return revisionConflict(map, args);

    JsonArray regions = args.getJsonArray("regions");
    if (regions == null || regions.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No regions provided", true, null);
    }

    for (int i = 0; i < regions.size(); i++) {
      if (!(regions.get(i) instanceof JsonObject region)) {
        return McpResponseFactory.createError(
            "INVALID_ARGUMENTS", "Region at index " + i + " must be an object", true, null);
      }
      String layerName = getString(region, "layer", null);
      if (resolveTileLayer(map, layerName) == null) {
        return McpResponseFactory.createError(
            "LAYER_NOT_FOUND", "Tile layer not found at region index " + i + ": " + layerName, true, null);
      }
      if (getInt(region, "width", 0) <= 0 || getInt(region, "height", 0) <= 0) {
        return McpResponseFactory.createError(
            "INVALID_ARGUMENTS", "Region at index " + i + " must have positive width and height", true, null);
      }
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    int tileCount = 0;
    for (int i = 0; i < regions.size(); i++) {
      JsonObject region = regions.getJsonObject(i);
      ITileLayer tileLayer = resolveTileLayer(map, getString(region, "layer", null));
      int startX = getInt(region, "x", 0);
      int startY = getInt(region, "y", 0);
      int width = getInt(region, "width", 0);
      int height = getInt(region, "height", 0);
      int gid = getInt(region, "gid", 0);
      for (int col = startX; col < startX + width; col++) {
        for (int row = startY; row < startY + height; row++) {
          if (col >= 0 && col < map.getWidth() && row >= 0 && row < map.getHeight()) {
            tileLayer.setTile(col, row, gid);
            tileCount++;
          }
        }
      }
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("previousRevision", prevRev)
        .add("revision", newRev)
        .add("regionCount", regions.size())
        .add("tileCount", tileCount)
        .add("operationId", "fill-regions-" + newRev)
        .build();
  }

  private static JsonObject paintTerrain(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String layerName = getString(args, "layer", null);
    ITileLayer tileLayer = resolveTileLayer(map, layerName);
    if (tileLayer == null) {
      return McpResponseFactory.createError(
          "LAYER_NOT_FOUND", "Tile layer not found: " + layerName, true, null);
    }

    int startX = getInt(args, "x", 0);
    int startY = getInt(args, "y", 0);
    int width = getInt(args, "width", 1);
    int height = getInt(args, "height", 1);
    int gid = getInt(args, "gid", 0);

    long prevRev = McpRevisionTracker.getRevision(map);
    for (int col = startX; col < startX + width; col++) {
      for (int row = startY; row < startY + height; row++) {
        if (col >= 0 && col < map.getWidth() && row >= 0 && row < map.getHeight()) {
          tileLayer.setTile(col, row, gid);
        }
      }
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, null, null, null,
        "paint-terrain-" + newRev);
  }

  // ── Visualization ─────────────────────────────────────────────────

  private static JsonObject renderMap(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    int w = Math.max(1, map.getSizeInPixels() != null ? map.getSizeInPixels().width : 512);
    int h = Math.max(1, map.getSizeInPixels() != null ? map.getSizeInPixels().height : 512);
    BufferedImage img = renderMapSnapshot(map, w, h);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("revision", McpRevisionTracker.getRevision(map))
        .add("imageData", "data:image/png;base64," + encodePngBase64(img))
        .build();
  }

  private static JsonObject renderRegion(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    int x = (int) getDouble(args, "x", 0.0);
    int y = (int) getDouble(args, "y", 0.0);
    int w = Math.max(1, (int) getDouble(args, "width", 256));
    int h = Math.max(1, (int) getDouble(args, "height", 256));

    BufferedImage full = renderMapSnapshot(map,
        map.getSizeInPixels() != null ? map.getSizeInPixels().width : 512,
        map.getSizeInPixels() != null ? map.getSizeInPixels().height : 512);
    int cropX = Math.max(0, Math.min(x, full.getWidth() - 1));
    int cropY = Math.max(0, Math.min(y, full.getHeight() - 1));
    int cropW = Math.min(w, full.getWidth() - cropX);
    int cropH = Math.min(h, full.getHeight() - cropY);
    BufferedImage cropped = full.getSubimage(cropX, cropY, Math.max(1, cropW), Math.max(1, cropH));

    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("revision", McpRevisionTracker.getRevision(map))
        .add("bounds", boundsJson(cropX, cropY, cropW, cropH))
        .add("imageData", "data:image/png;base64," + encodePngBase64(cropped))
        .build();
  }

  // ── Analysis ──────────────────────────────────────────────────────

  private static JsonObject analyzeMap(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    JsonArrayBuilder issues = Json.createArrayBuilder();
    int issueCount = 0;
    Set<Integer> seenIds = new HashSet<>();

    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null) continue;
      for (IMapObject mo : layer.getMapObjects()) {
        if (mo == null) continue;
        if (!seenIds.add(mo.getId())) {
          issueCount++;
          issues.add(Json.createObjectBuilder()
              .add("severity", "ERROR")
              .add("type", "DUPLICATE_ID")
              .add("entityId", mo.getId()));
        }
      }
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("revision", McpRevisionTracker.getRevision(map))
        .add("issueCount", issueCount)
        .add("issues", issues)
        .build();
  }

  private static JsonObject analyzeCollision(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    // Detect overlapping COLLISIONBOX entities
    List<IMapObject> collisionBoxes = new ArrayList<>();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null) continue;
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj != null && "COLLISIONBOX".equalsIgnoreCase(obj.getType())) {
          collisionBoxes.add(obj);
        }
      }
    }

    JsonArrayBuilder overlaps = Json.createArrayBuilder();
    int overlapCount = 0;
    for (int i = 0; i < collisionBoxes.size(); i++) {
      for (int j = i + 1; j < collisionBoxes.size(); j++) {
        if (collisionBoxes.get(i).getBoundingBox().intersects(collisionBoxes.get(j).getBoundingBox())) {
          overlapCount++;
          overlaps.add(Json.createObjectBuilder()
              .add("entityA", collisionBoxes.get(i).getId())
              .add("entityB", collisionBoxes.get(j).getId()));
        }
      }
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("revision", McpRevisionTracker.getRevision(map))
        .add("overlappingCollisionCount", overlapCount)
        .add("overlaps", overlaps)
        .build();
  }

  private static JsonObject analyzePlayability(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) return mapNotFound(mapId);
    JsonObject actor = args.getJsonObject("actorProfile");
    if (actor == null || !actor.containsKey("width") || !actor.containsKey("height")) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS",
          "actorProfile.width and actorProfile.height are required", true, null);
    }
    PlayabilityResult result = playability(map, actor, args.getJsonArray("requiredTargets"));
    return result.toJson(map.getName(), McpRevisionTracker.getRevision(map));
  }

  private static JsonObject renderPlayability(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) return mapNotFound(mapId);
    JsonObject actor = args.getJsonObject("actorProfile");
    if (actor == null || !actor.containsKey("width") || !actor.containsKey("height")) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS",
          "actorProfile.width and actorProfile.height are required", true, null);
    }
    PlayabilityResult result = playability(map, actor, args.getJsonArray("requiredTargets"));
    BufferedImage image = renderMapSnapshot(map,
        Math.max(1, map.getSizeInPixels().width), Math.max(1, map.getSizeInPixels().height));
    Graphics2D graphics = image.createGraphics();
    try {
      for (int y = 0; y < map.getHeight(); y++) for (int x = 0; x < map.getWidth(); x++) {
        int alpha = result.blocked[y][x] ? 130 : result.reachable[y][x] ? 70 : 100;
        graphics.setColor(result.blocked[y][x] ? new java.awt.Color(220, 0, 180, alpha)
            : result.reachable[y][x] ? new java.awt.Color(0, 210, 80, alpha) : new java.awt.Color(220, 30, 30, alpha));
        graphics.fillRect(x * map.getTileWidth(), y * map.getTileHeight(), map.getTileWidth(), map.getTileHeight());
      }
    } finally { graphics.dispose(); }
    String encoded = encodePngBase64(image);
    return Json.createObjectBuilder(result.toJson(map.getName(), McpRevisionTracker.getRevision(map)))
        .add("imageBase64", encoded).add("imageData", "data:image/png;base64," + encoded)
        .add("mimeType", "image/png").build();
  }

  private static PlayabilityResult playability(IMap map, JsonObject actor, JsonArray requestedTargets) {
    int width = map.getWidth(), height = map.getHeight();
    boolean[][] blocked = new boolean[height][width], reachable = new boolean[height][width];
    double actorWidth = getDouble(actor, "width", 0), actorHeight = getDouble(actor, "height", 0);
    double clearance = getDouble(actor, "clearance", 2);
    List<IMapObject> objects = new ArrayList<>(), collisions = new ArrayList<>(), starts = new ArrayList<>();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) if (layer != null) for (IMapObject object : layer.getMapObjects()) {
      if (object == null) continue; objects.add(object);
      if ("COLLISIONBOX".equalsIgnoreCase(object.getType())) collisions.add(object);
      if ("SPAWNPOINT".equalsIgnoreCase(object.getType())) starts.add(object);
    }
    for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
      double px = x * map.getTileWidth() + (map.getTileWidth() - actorWidth) / 2d;
      double py = y * map.getTileHeight() + (map.getTileHeight() - actorHeight) / 2d;
      java.awt.geom.Rectangle2D footprint = new java.awt.geom.Rectangle2D.Double(px, py, actorWidth, actorHeight);
      if (footprint.getMinX() < clearance || footprint.getMinY() < clearance
          || footprint.getMaxX() > map.getSizeInPixels().width - clearance
          || footprint.getMaxY() > map.getSizeInPixels().height - clearance) { blocked[y][x] = true; continue; }
      for (IMapObject collision : collisions) if (collision.getBoundingBox().intersects(footprint)) { blocked[y][x] = true; break; }
    }
    java.util.ArrayDeque<Point> queue = new java.util.ArrayDeque<>();
    for (IMapObject start : starts) {
      Point cell = cell(map, start); if (inBounds(cell, width, height) && !blocked[cell.y][cell.x]) { reachable[cell.y][cell.x] = true; queue.add(cell); }
    }
    while (!queue.isEmpty()) { Point current = queue.remove(); for (Point next : List.of(new Point(current.x + 1, current.y), new Point(current.x - 1, current.y), new Point(current.x, current.y + 1), new Point(current.x, current.y - 1))) {
      if (inBounds(next, width, height) && !blocked[next.y][next.x] && !reachable[next.y][next.x]) { reachable[next.y][next.x] = true; queue.add(next); }
    }}
    List<IMapObject> targets = new ArrayList<>();
    if (requestedTargets != null) for (JsonValue value : requestedTargets) if (value instanceof JsonObject target) {
      for (IMapObject object : objects) if ((target.containsKey("entityId") && object.getId() == getInt(target, "entityId", -1))
          || (!target.getString("name", "").isBlank() && target.getString("name").equalsIgnoreCase(object.getName()))
          || (!target.getString("type", "").isBlank() && target.getString("type").equalsIgnoreCase(object.getType()))) targets.add(object);
    }
    if (targets.isEmpty()) for (IMapObject object : objects) if (object.getName() != null && object.getName().equalsIgnoreCase("END_LEVEL")) targets.add(object);
    return new PlayabilityResult(blocked, reachable, starts, targets, map);
  }

  private static Point cell(IMap map, IMapObject object) { return new Point((int) (object.getX() / map.getTileWidth()), (int) (object.getY() / map.getTileHeight())); }
  private static boolean inBounds(Point point, int width, int height) { return point.x >= 0 && point.y >= 0 && point.x < width && point.y < height; }

  private record PlayabilityResult(boolean[][] blocked, boolean[][] reachable, List<IMapObject> starts, List<IMapObject> targets, IMap map) {
    JsonObject toJson(String mapId, long revision) {
      JsonArrayBuilder failures = Json.createArrayBuilder(), targetResults = Json.createArrayBuilder();
      if (starts.isEmpty()) failures.add(Json.createObjectBuilder().add("code", "NO_PLAYER_SPAWN").add("message", "No SPAWNPOINT is available for navigation analysis."));
      for (IMapObject target : targets) { Point point = cell(map, target); boolean found = inBounds(point, map.getWidth(), map.getHeight()) && reachable[point.y][point.x];
        targetResults.add(Json.createObjectBuilder().add("targetEntity", target.getName() == null ? String.valueOf(target.getId()) : target.getName()).add("found", found));
        if (!found) failures.add(Json.createObjectBuilder().add("code", "GOAL_UNREACHABLE").add("message", "Required target '" + (target.getName() == null ? target.getId() : target.getName()) + "' cannot be reached from a player spawn.").add("targetEntityId", target.getId())); }
      int reachableCount = 0, blockedCount = 0; for (int y = 0; y < map.getHeight(); y++) for (int x = 0; x < map.getWidth(); x++) { if (blocked[y][x]) blockedCount++; else if (reachable[y][x]) reachableCount++; }
      JsonArray hardFailures = failures.build();
      return Json.createObjectBuilder().add("success", true).add("status", hardFailures.isEmpty() ? "PASS" : "FAIL").add("mapId", mapId).add("revision", revision)
          .add("hardFailures", hardFailures).add("navigation", Json.createObjectBuilder().add("reachableCellCount", reachableCount).add("blockedCellCount", blockedCount).add("startCount", starts.size()).add("requiredTargetCount", targets.size()).add("paths", targetResults)).build();
    }
  }

  // ── Transactions ──────────────────────────────────────────────────

  private static JsonObject previewChanges(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }

    JsonArray operations = args.getJsonArray("operations");
    int count = operations != null ? operations.size() : 0;

    return Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", map.getName())
        .add("valid", true)
        .add("operationCount", count)
        .add("warnings", Json.createArrayBuilder())
        .build();
  }

  private static JsonObject applyChanges(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    return McpResponseFactory.createError(
        "NOT_IMPLEMENTED",
        "apply_changes does not execute operations yet. Use the specific mutation tools instead.",
        true,
        null);
  }

  private static JsonObject setAmbientLight(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String colorHex = getString(args, "color", "#000000");
    int alpha = getInt(args, "alpha", 255);
    String formattedColor = colorHex;
    if (alpha >= 0 && alpha < 255 && colorHex.length() == 7 && colorHex.startsWith("#")) {
      formattedColor = String.format("#%02x%s", alpha, colorHex.substring(1));
    }

    map.setValue(de.gurkenlabs.litiengine.environment.tilemap.MapProperty.AMBIENTCOLOR, formattedColor);
    if (Game.world().environment() != null && Game.world().environment().getMap() == map
        && Game.world().environment().getAmbientLight() != null) {
      Game.world().environment().getAmbientLight().setColor(
          map.getColorValue(
              de.gurkenlabs.litiengine.environment.tilemap.MapProperty.AMBIENTCOLOR,
              de.gurkenlabs.litiengine.graphics.AmbientLight.DEFAULT_COLOR));
    }

    long prevRev = McpRevisionTracker.getRevision(map);
    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, null, null, null,
        "set-ambient-light-" + newRev);
  }

  private static JsonObject scatterFloorDetails(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);
    if (map == null) {
      return mapNotFound(mapId);
    }
    if (!checkRevision(map, args)) {
      return revisionConflict(map, args);
    }

    String layerName = getString(args, "layer", null);
    ITileLayer tileLayer = resolveTileLayer(map, layerName);
    if (tileLayer == null) {
      return McpResponseFactory.createError(
          "LAYER_NOT_FOUND", "Tile layer not found: " + layerName, true, null);
    }

    int startX = getInt(args, "x", 0);
    int startY = getInt(args, "y", 0);
    int width = getInt(args, "width", 1);
    int height = getInt(args, "height", 1);

    JsonArray gidsArr = args.getJsonArray("gids");
    if (gidsArr == null || gidsArr.isEmpty()) {
      return McpResponseFactory.createError("INVALID_ARGUMENTS", "No GIDs provided for scatter", true, null);
    }

    List<Integer> gids = new ArrayList<>();
    for (int i = 0; i < gidsArr.size(); i++) {
      gids.add(gidsArr.getInt(i));
    }

    double density = getDouble(args, "density", 0.15);
    java.util.Random rnd = new java.util.Random();

    long prevRev = McpRevisionTracker.getRevision(map);

    for (int col = startX; col < startX + width; col++) {
      for (int row = startY; row < startY + height; row++) {
        if (col >= 0 && col < map.getWidth() && row >= 0 && row < map.getHeight()) {
          if (rnd.nextDouble() < density) {
            int selectedGid = gids.get(rnd.nextInt(gids.size()));
            tileLayer.setTile(col, row, selectedGid);
          }
        }
      }
    }

    long newRev = McpRevisionTracker.incrementRevision(map);
    return McpResponseFactory.createMutationResult(
        map.getName(), prevRev, newRev, null, null, null, null, null,
        "scatter-floor-details-" + newRev);
  }

  // ── Helpers ───────────────────────────────────────────────────────

  private static IMap resolveMap(String mapId) {
    if (mapId != null && !mapId.isBlank()) {
      if (Editor.instance().getGameFile() != null) {
        for (IMap m : Editor.instance().getGameFile().getMaps()) {
          if (m != null && mapId.equalsIgnoreCase(m.getName())) {
            return m;
          }
        }
      }
      IMap activeMap = Game.world().environment() != null ? Game.world().environment().getMap() : null;
      if (activeMap != null && mapId.equalsIgnoreCase(activeMap.getName())) {
        return activeMap;
      }
      return null;
    }
    // Fallback to currently loaded map
    return Game.world().environment() != null ? Game.world().environment().getMap() : null;
  }

  private static BufferedImage renderMapSnapshot(IMap map, int width, int height) {
    if (Game.world().environment() != null && Game.world().environment().getMap() == map) {
      return McpToolHandler.renderCanvasSnapshot(width, height);
    }
    BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      MapRenderer.render(graphics, map, map.getBounds());
    } finally {
      graphics.dispose();
    }
    return image;
  }

  private static IMapObject findMapObject(IMap map, int id) {
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null) continue;
      IMapObject found = findInLayer(layer, id);
      if (found != null) return found;
    }
    return null;
  }

  private static IMapObject findInLayer(IMapObjectLayer layer, int id) {
    for (IMapObject obj : layer.getMapObjects()) {
      if (obj != null && obj.getId() == id) return obj;
    }
    return null;
  }

  private static IMapObjectLayer findLayerContaining(IMap map, int entityId) {
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null) continue;
      if (findInLayer(layer, entityId) != null) return layer;
    }
    // Fallback to first layer
    return map.getMapObjectLayers().isEmpty() ? null : map.getMapObjectLayers().getFirst();
  }

  private static IMapObjectLayer resolveObjectLayer(IMap map, String name) {
    if (name != null && !name.isBlank()) {
      for (IMapObjectLayer layer : map.getMapObjectLayers()) {
        if (layer != null && name.equalsIgnoreCase(layer.getName())) {
          return layer;
        }
      }
    }
    if (map.getMapObjectLayers() != null && !map.getMapObjectLayers().isEmpty()) {
      return map.getMapObjectLayers().getFirst();
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer defaultLayer =
        new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer();
    defaultLayer.setName(name != null && !name.isBlank() ? name : "default");
    map.addLayer(defaultLayer);
    return defaultLayer;
  }

  private static ITileLayer resolveTileLayer(IMap map, String name) {
    for (ITileLayer layer : map.getTileLayers()) {
      if (layer != null && (name == null || name.equalsIgnoreCase(layer.getName()))) {
        return layer;
      }
    }
    return null;
  }

  private static JsonObject serializeEntity(IMapObject obj) {
    return Json.createObjectBuilder()
        .add("id", obj.getId())
        .add("name", obj.getName() != null ? obj.getName() : "")
        .add("type", obj.getType() != null ? obj.getType() : "")
        .add("x", obj.getX())
        .add("y", obj.getY())
        .add("width", obj.getWidth())
        .add("height", obj.getHeight())
        .add("layer", obj.getLayer() != null && obj.getLayer().getName() != null
            ? obj.getLayer().getName() : "")
        .build();
  }

  private static boolean checkRevision(IMap map, JsonObject args) {
    Long expected = getLong(args, "expectedRevision", null);
    return McpRevisionTracker.validateRevision(map, expected);
  }

  private static JsonObject revisionConflict(IMap map, JsonObject args) {
    Long expected = getLong(args, "expectedRevision", null);
    long actual = McpRevisionTracker.getRevision(map);
    return McpResponseFactory.createError("REVISION_CONFLICT",
        "Map revision conflict: expected " + expected + " but actual is " + actual,
        true,
        Json.createObjectBuilder()
            .add("mapId", map.getName() != null ? map.getName() : "")
            .add("expectedRevision", expected != null ? expected : -1)
            .add("actualRevision", actual)
            .add("recovery", "Retry with expectedRevision set to actualRevision after reviewing any intervening edits.")
            .build());
  }

  private static JsonObject mapNotFound(String mapId) {
    return McpResponseFactory.createError(
        "MAP_NOT_FOUND", "Map not found: " + mapId, true, null);
  }

  private static JsonObject boundsJson(double x, double y, double w, double h) {
    return Json.createObjectBuilder()
        .add("x", x).add("y", y).add("width", w).add("height", h).build();
  }

  private static String encodePngBase64(BufferedImage image) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      javax.imageio.ImageIO.write(image, "png", baos);
      return Base64.getEncoder().encodeToString(baos.toByteArray());
    } catch (Exception e) {
      return "";
    }
  }

  // ── Project Analysis & Level-Design Orchestration ─────────────────

  private static JsonObject analyzeProject(JsonObject args) {
    JsonObjectBuilder result = Json.createObjectBuilder();
    result.add("success", true);

    if (Editor.instance().getGameFile() == null) {
      return McpResponseFactory.createError("NO_PROJECT_LOADED", "No project file is currently loaded in utiLITI", false, null);
    }

    JsonObjectBuilder profile = Json.createObjectBuilder();
    profile.add("tileSize", Json.createObjectBuilder().add("width", 16).add("height", 16));

    Set<String> layerConventions = new LinkedHashSet<>();
    Set<String> entityTypes = new LinkedHashSet<>();

    JsonArrayBuilder mapsArr = Json.createArrayBuilder();
    JsonArrayBuilder nodesArr = Json.createArrayBuilder();
    JsonArrayBuilder transitionsArr = Json.createArrayBuilder();
    JsonArrayBuilder unresolvedTransitionsArr = Json.createArrayBuilder();
    JsonArrayBuilder findingsArr = Json.createArrayBuilder();

    for (IMap map : Editor.instance().getGameFile().getMaps()) {
      if (map == null || map.getName() == null) continue;
      String mapId = map.getName();

      nodesArr.add(Json.createObjectBuilder()
          .add("id", mapId)
          .add("width", map.getWidth())
          .add("height", map.getHeight())
          .add("tileWidth", map.getTileWidth())
          .add("tileHeight", map.getTileHeight()));

      if (map.getMapObjectLayers() != null) {
        for (IMapObjectLayer layer : map.getMapObjectLayers()) {
          if (layer != null && layer.getName() != null) {
            layerConventions.add(layer.getName());
          }
        }
      }
      if (map.getTileLayers() != null) {
        for (ITileLayer layer : map.getTileLayers()) {
          if (layer != null && layer.getName() != null) {
            layerConventions.add(layer.getName());
          }
        }
      }

      int spawnCount = 0;
      for (IMapObject obj : map.getMapObjects()) {
        if (obj == null) continue;
        if (obj.getType() != null) {
          entityTypes.add(obj.getType());
        }

        if ("SPAWNPOINT".equalsIgnoreCase(obj.getType())) {
          spawnCount++;
        }

        if ("TRIGGER".equalsIgnoreCase(obj.getType()) || "AREA".equalsIgnoreCase(obj.getType())) {
          // Optional custom properties must use the default-value overload. The no-default
          // overload throws NoSuchElementException (for example, with message "targetMap")
          // on ordinary triggers that do not define cross-map navigation.
          String targetMap = obj.getStringValue("targetMap", null);
          if (targetMap == null) targetMap = obj.getStringValue("destination", null);
          String targetSpawn = obj.getStringValue("targetSpawn", null);

          if (targetMap != null && !targetMap.isBlank()) {
            JsonObjectBuilder transBuilder = Json.createObjectBuilder()
                .add("sourceMap", mapId)
                .add("triggerId", obj.getId())
                .add("triggerName", obj.getName() != null ? obj.getName() : "")
                .add("destinationMap", targetMap)
                .add("targetSpawn", targetSpawn != null ? targetSpawn : "");
            transitionsArr.add(transBuilder);

            IMap destMap = resolveMap(targetMap);
            if (destMap == null) {
              unresolvedTransitionsArr.add(transBuilder);
              findingsArr.add(Json.createObjectBuilder()
                  .add("severity", "critical")
                  .add("category", "gameplay-flow")
                  .add("mapId", mapId)
                  .add("message", "Trigger references destination map '" + targetMap + "' which does not exist in the project")
                  .add("confidence", "confirmed")
                  .add("relatedEntityIds", Json.createArrayBuilder().add(obj.getId())));
            }
          }
        }
      }

      if (spawnCount == 0) {
        findingsArr.add(Json.createObjectBuilder()
            .add("severity", "high")
            .add("category", "level-design")
            .add("mapId", mapId)
            .add("message", "Map '" + mapId + "' has no player SPAWNPOINT entities defined")
            .add("confidence", "confirmed"));
      }

      int layerCount = (map.getMapObjectLayers() != null ? map.getMapObjectLayers().size() : 0)
          + (map.getTileLayers() != null ? map.getTileLayers().size() : 0);

      mapsArr.add(Json.createObjectBuilder()
          .add("id", mapId)
          .add("width", map.getWidth())
          .add("height", map.getHeight())
          .add("layerCount", layerCount)
          .add("entityCount", map.getMapObjects().size())
          .add("spawnCount", spawnCount));
    }

    JsonArrayBuilder layersArr = Json.createArrayBuilder();
    layerConventions.forEach(layersArr::add);
    profile.add("layerConventions", layersArr);

    JsonArrayBuilder typesArr = Json.createArrayBuilder();
    entityTypes.forEach(typesArr::add);
    profile.add("entityConventions", typesArr);

    result.add("projectProfile", profile);
    result.add("mapGraph", Json.createObjectBuilder()
        .add("nodes", nodesArr)
        .add("transitions", transitionsArr)
        .add("unresolvedTransitions", unresolvedTransitionsArr));
    result.add("maps", mapsArr);
    result.add("findings", findingsArr);

    JsonArrayBuilder recommendations = Json.createArrayBuilder();
    recommendations.add("Verify map transition target maps and target spawn IDs before editing");
    recommendations.add("Follow the Big -> Medium -> Small pass sequence for map construction");
    result.add("recommendedNextActions", recommendations);

    return result.build();
  }

  private static JsonObject planMapChanges(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    String goal = getString(args, "goal", "");

    JsonObjectBuilder plan = Json.createObjectBuilder();
    plan.add("success", true);
    plan.add("mapId", mapId != null ? mapId : "new_map");
    plan.add("goal", goal);
    plan.add("summary", "Staged Level-Design Plan (Big -> Medium -> Small) for: " + goal);

    JsonArrayBuilder criticalPath = Json.createArrayBuilder();
    criticalPath.add("1. Arrival: Player enters via SPAWNPOINT");
    criticalPath.add("2. Orientation: Clear visual sightlines to objective");
    criticalPath.add("3. Engagement: Encounter space / gate");
    criticalPath.add("4. Resolution: Objective / exit transition");
    plan.add("criticalPath", criticalPath);

    plan.add("tileOperations", Json.createArrayBuilder());
    plan.add("entityCreates", Json.createArrayBuilder());
    plan.add("entityUpdates", Json.createArrayBuilder());
    plan.add("entityRemovals", Json.createArrayBuilder());
    plan.add("collisionOperations", Json.createArrayBuilder());

    JsonArrayBuilder flow = Json.createArrayBuilder();
    flow.add("Enter room -> Engage threat -> Activate switch/key -> Proceed to exit");
    plan.add("expectedGameplayFlow", flow);

    JsonArrayBuilder checks = Json.createArrayBuilder();
    checks.add("run analyze_map to check layer integrity");
    checks.add("run analyze_collision to verify traversal bounds");
    checks.add("run validate_map_changes before saving");
    plan.add("validationChecks", checks);

    plan.add("requiresUserDecision", Json.createArrayBuilder());

    return plan.build();
  }

  private static JsonObject validateMapPlan(JsonObject args) {
    String mapId = getString(args, "mapId", null);
    IMap map = resolveMap(mapId);

    JsonObjectBuilder result = Json.createObjectBuilder();
    result.add("success", true);

    if (map == null) {
      return McpResponseFactory.createError("MAP_NOT_FOUND", "Specified map could not be found", false, null);
    }

    JsonArrayBuilder diagnostics = Json.createArrayBuilder();
    int spawnCount = 0;
    for (IMapObject obj : map.getMapObjects()) {
      if ("SPAWNPOINT".equalsIgnoreCase(obj.getType())) {
        spawnCount++;
      }
    }

    if (spawnCount == 0) {
      diagnostics.add(Json.createObjectBuilder()
          .add("severity", "high")
          .add("type", "missing_spawn")
          .add("message", "Map '" + map.getName() + "' contains 0 SPAWNPOINT entities"));
    }

    JsonArray diagBuilt = diagnostics.build();
    result.add("status", diagBuilt.isEmpty() ? "valid" : "issues_found");
    result.add("diagnostics", diagBuilt);
    return result.build();
  }

  private static String getString(JsonObject obj, String key, String def) {
    return obj != null && obj.containsKey(key) && !obj.isNull(key)
        ? obj.getString(key) : def;
  }

  private static boolean getBoolean(JsonObject obj, String key, boolean def) {
    return obj != null && obj.containsKey(key) && !obj.isNull(key)
        ? obj.getBoolean(key) : def;
  }

  private static int getInt(JsonObject obj, String key, int def) {
    return obj != null && obj.containsKey(key) && !obj.isNull(key)
        ? obj.getInt(key) : def;
  }

  private static float getFloat(JsonObject obj, String key, float def) {
    if (obj != null && obj.containsKey(key) && !obj.isNull(key)) {
      return ((Number) obj.getJsonNumber(key).numberValue()).floatValue();
    }
    return def;
  }

  private static double getDouble(JsonObject obj, String key, double def) {
    if (obj != null && obj.containsKey(key) && !obj.isNull(key)) {
      return obj.getJsonNumber(key).doubleValue();
    }
    return def;
  }

  private static Long getLong(JsonObject obj, String key, Long def) {
    if (obj != null && obj.containsKey(key) && !obj.isNull(key)) {
      return obj.getJsonNumber(key).longValue();
    }
    return def;
  }
}
