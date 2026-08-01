package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Frame;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangTile;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.tool.TerrainResolver;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class McpTilesetHandler {
  private static final Set<String> TOOLS = Set.of(
      "list-tile-animations",
      "get-tile-animation",
      "set-tile-animation",
      "clear-tile-animation",
      "list-terrains",
      "create-terrain-set",
      "delete-terrain-set",
      "add-terrain",
      "update-terrain",
      "remove-terrain",
      "set-tile-terrain",
      "paint-terrain",
      "render-tileset",
      "find-tile-usage",
      "render-tile-context",
      "preview-tile-edits");

  private McpTilesetHandler() {
    throw new UnsupportedOperationException();
  }

  static void addToolDefinitions(JsonArrayBuilder tools) {
    tools.add(McpToolHandler.createToolDef(
        "list-tile-animations", "List animated tiles in a tileset", tilesetParam()));

    tools.add(McpToolHandler.createToolDef(
        "get-tile-animation", "Get the frame sequence for an animated tile", tileParam()));
    tools.add(McpToolHandler.createToolDef(
        "clear-tile-animation", "Remove the animation from a tile", tileParam()));

    JsonObject frameSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("tileId", Json.createObjectBuilder().add("type", "integer"))
            .add("duration", Json.createObjectBuilder().add("type", "integer").add("minimum", 1)))
        .add("required", Json.createArrayBuilder().add("tileId").add("duration"))
        .build();
    JsonObjectBuilder animationParam = Json.createObjectBuilder(tileParam())
        .add("frames", McpToolHandler.createArrayParam(
            "Ordered animation frames", true, frameSchema));
    tools.add(McpToolHandler.createToolDef(
        "set-tile-animation", "Create or replace a tile animation", animationParam.build()));

    tools.add(McpToolHandler.createToolDef(
        "list-terrains",
        "Inspect Wang terrain sets before painting. Returns terrain names and indices, local tile assignments, Wang ID order, and bulk-painting guidance.",
        tilesetParam()));

    JsonObjectBuilder createSetParam = Json.createObjectBuilder(terrainSetParam())
        .add("type", McpToolHandler.createParam("string", "Terrain type: corner, edge, or mixed", false));
    tools.add(McpToolHandler.createToolDef(
        "create-terrain-set", "Create a Wang terrain set", createSetParam.build()));
    tools.add(McpToolHandler.createToolDef(
        "delete-terrain-set", "Delete a Wang terrain set and all assignments", terrainSetParam()));

    JsonObjectBuilder addTerrainParam = Json.createObjectBuilder(terrainParam())
        .add("color", McpToolHandler.createParam("string", "Terrain color as #RRGGBB or #AARRGGBB", false))
        .add("tileId", McpToolHandler.createParam("integer", "Representative local tile ID, or -1", false))
        .add("probability", McpToolHandler.createParam("number", "Non-negative selection probability", false));
    tools.add(McpToolHandler.createToolDef(
        "add-terrain", "Add a terrain color to a terrain set", addTerrainParam.build()));

    JsonObjectBuilder updateTerrainParam = Json.createObjectBuilder(addTerrainParam.build())
        .add("newName", McpToolHandler.createParam("string", "Optional replacement terrain name", false));
    tools.add(McpToolHandler.createToolDef(
        "update-terrain", "Edit a terrain name, color, representative tile, or probability", updateTerrainParam.build()));
    tools.add(McpToolHandler.createToolDef(
        "remove-terrain", "Remove a terrain and repair all Wang IDs in the set", terrainParam()));

    JsonObjectBuilder tileTerrainParam = Json.createObjectBuilder(tileParam())
        .add("set", McpToolHandler.createParam("string", "Terrain set name", true))
        .add("wangId", McpToolHandler.createArrayParam(
            "Eight Wang terrain indices, with 0 meaning no terrain",
            true,
            Json.createObjectBuilder().add("type", "integer").add("minimum", 0).build()));
    tools.add(McpToolHandler.createToolDef(
        "set-tile-terrain",
        "Assign a local tile's Wang terrain indices in order [top, top-right, right, bottom-right, bottom, bottom-left, left, top-left]. Use 0 for no terrain; use list-terrains to discover terrain indices.",
        tileTerrainParam.build()));

    JsonObjectBuilder paintParam = Json.createObjectBuilder()
        .add("tileset", McpToolHandler.createParam("string", "Tileset resource name used by the active map", true))
        .add("set", McpToolHandler.createParam("string", "Terrain set name returned by list-terrains", true))
        .add("terrain", McpToolHandler.createParam("string", "Terrain name returned by list-terrains", true))
        .add("layer", McpToolHandler.createParam("string", "Tile layer name", true))
        .add("x", McpToolHandler.createParam("integer", "Legacy rectangle start X; use with y and optional width/height", false))
        .add("y", McpToolHandler.createParam("integer", "Legacy rectangle start Y; use with x and optional width/height", false))
        .add("width", McpToolHandler.createParam("integer", "Legacy rectangle width in tiles", false))
        .add("height", McpToolHandler.createParam("integer", "Legacy rectangle height in tiles", false));
    JsonObject cellSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("x", Json.createObjectBuilder().add("type", "integer"))
            .add("y", Json.createObjectBuilder().add("type", "integer")))
        .add("required", Json.createArrayBuilder().add("x").add("y"))
        .build();
    JsonObject regionSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("x", Json.createObjectBuilder().add("type", "integer"))
            .add("y", Json.createObjectBuilder().add("type", "integer"))
            .add("width", Json.createObjectBuilder().add("type", "integer").add("minimum", 1))
            .add("height", Json.createObjectBuilder().add("type", "integer").add("minimum", 1)))
        .add("required", Json.createArrayBuilder().add("x").add("y").add("width").add("height"))
        .build();
    paintParam.add(
        "cells",
        McpToolHandler.createArrayParam(
            "Sparse grid cells to paint in one call", false, cellSchema));
    paintParam.add(
        "regions",
        McpToolHandler.createArrayParam(
            "Multiple rectangular grid regions to paint in one call", false, regionSchema));
    tools.add(McpToolHandler.createToolDef(
        "paint-terrain",
        "Paint a named Wang terrain on sparse cells and/or rectangles in one call. The resolver chooses local tiles and updates neighboring transitions; do not calculate GIDs manually. Call list-terrains first.",
        paintParam.build()));

    JsonObjectBuilder renderTileset = Json.createObjectBuilder(tilesetParam())
        .add("scale", McpToolHandler.createParam("integer", "Nearest-neighbor tile scale (default 4)", false));
    tools.add(McpToolHandler.createToolDef(
        "render-tileset", "Render a tileset atlas with local tile ID annotations and grid lines", renderTileset.build()));

    JsonObjectBuilder usage = Json.createObjectBuilder(tilesetParam())
        .add("mapId", McpToolHandler.createParam("string", "Optional map name; searches every project map by default", false))
        .add("tileId", McpToolHandler.createParam("integer", "Optional local tile ID filter", false));
    tools.add(McpToolHandler.createToolDef(
        "find-tile-usage", "Find tileset usage across maps and summarize top, bottom, left, and right neighbor frequencies", usage.build()));

    JsonObjectBuilder context = Json.createObjectBuilder()
        .add("mapId", McpToolHandler.createParam("string", "Map name (active map by default)", false))
        .add("layer", McpToolHandler.createParam("string", "Selected tile layer", false))
        .add("x", McpToolHandler.createParam("integer", "Center column", true))
        .add("y", McpToolHandler.createParam("integer", "Center row", true))
        .add("radius", McpToolHandler.createParam("integer", "Context radius in tiles (default 2)", false))
        .add("mode", McpToolHandler.createParam("string", "selected-layer, composite, or layer-stack", false))
        .add("scale", McpToolHandler.createParam("integer", "Nearest-neighbor pixel scale (default 4)", false));
    tools.add(McpToolHandler.createToolDef(
        "render-tile-context", "Render a tile neighborhood on one layer, composited layers, or a layer-stack contact sheet", context.build()));

    JsonObject editSchema = Json.createObjectBuilder().add("type", "object")
        .add("properties", Json.createObjectBuilder()
            .add("x", Json.createObjectBuilder().add("type", "integer"))
            .add("y", Json.createObjectBuilder().add("type", "integer"))
            .add("gid", Json.createObjectBuilder().add("type", "integer")))
        .add("required", Json.createArrayBuilder().add("x").add("y").add("gid")).build();
    JsonObjectBuilder preview = Json.createObjectBuilder()
        .add("mapId", McpToolHandler.createParam("string", "Map name (active map by default)", false))
        .add("layer", McpToolHandler.createParam("string", "Target tile layer", true))
        .add("edits", McpToolHandler.createArrayParam("Candidate tile edits; never committed", true, editSchema))
        .add("padding", McpToolHandler.createParam("integer", "Preview padding in tiles (default 1)", false))
        .add("scale", McpToolHandler.createParam("integer", "Nearest-neighbor pixel scale (default 4)", false));
    tools.add(McpToolHandler.createToolDef(
        "preview-tile-edits", "Non-destructively render candidate tile edits with collision and bounds diagnostics", preview.build()));
  }

  private static JsonObject tilesetParam() {
    return Json.createObjectBuilder()
        .add("tileset", McpToolHandler.createParam("string", "Tileset resource name", true))
        .build();
  }

  private static JsonObject tileParam() {
    return Json.createObjectBuilder(tilesetParam())
        .add("tileId", McpToolHandler.createParam("integer", "Local tile ID", true))
        .build();
  }

  private static JsonObject terrainSetParam() {
    return Json.createObjectBuilder(tilesetParam())
        .add("set", McpToolHandler.createParam("string", "Terrain set name", true))
        .build();
  }

  private static JsonObject terrainParam() {
    return Json.createObjectBuilder(terrainSetParam())
        .add("terrain", McpToolHandler.createParam("string", "Terrain name", true))
        .build();
  }

  static boolean handles(String toolName) {
    return TOOLS.contains(toolName);
  }

  static JsonObject handle(String toolName, JsonObject args) {
    return switch (toolName) {
      case "list-tile-animations" -> listTileAnimations(args);
      case "get-tile-animation" -> getTileAnimation(args);
      case "set-tile-animation" -> setTileAnimation(args);
      case "clear-tile-animation" -> clearTileAnimation(args);
      case "list-terrains" -> listTerrains(args);
      case "create-terrain-set" -> createTerrainSet(args);
      case "delete-terrain-set" -> deleteTerrainSet(args);
      case "add-terrain" -> addTerrain(args);
      case "update-terrain" -> updateTerrain(args);
      case "remove-terrain" -> removeTerrain(args);
      case "set-tile-terrain" -> setTileTerrain(args);
      case "paint-terrain" -> paintTerrain(args);
      case "render-tileset" -> renderTileset(args);
      case "find-tile-usage" -> findTileUsage(args);
      case "render-tile-context" -> renderTileContext(args);
      case "preview-tile-edits" -> previewTileEdits(args);
      default -> error("Unknown tileset tool: " + toolName);
    };
  }

  private static JsonObject listTileAnimations(JsonObject args) {
    Tileset tileset = tileset(args);
    if (tileset == null) {
      return error("Tileset not found: " + McpToolHandler.getString(args, "tileset", ""));
    }
    JsonArrayBuilder animations = Json.createArrayBuilder();
    for (int tileId = 0; tileId < tileset.getTileCount(); tileId++) {
      if (tileset.getTile(tileId) instanceof TilesetEntry entry && entry.getAnimation() != null) {
        animations.add(tileAnimationInfo(tileId, entry));
      }
    }
    return Json.createObjectBuilder()
        .add("success", true)
        .add("tileset", tileset.getName())
        .add("animations", animations)
        .build();
  }

  private static JsonObject getTileAnimation(JsonObject args) {
    TilesetEntry entry = tile(args);
    if (entry == null) {
      return error("Tile not found");
    }
    return entry.getAnimation() == null
        ? Json.createObjectBuilder()
            .add("success", true)
            .add("animated", false)
            .add("message", "Tile " + entry.getId() + " has no animation")
            .build()
        : Json.createObjectBuilder()
            .add("success", true)
            .add("animated", true)
            .add("animation", tileAnimationInfo(entry.getId(), entry))
            .build();
  }

  private static JsonObject setTileAnimation(JsonObject args) {
    Tileset tileset = tileset(args);
    TilesetEntry entry = tile(tileset, args);
    if (tileset == null || entry == null) {
      return error("Tileset or tile not found");
    }
    JsonArray values = args.getJsonArray("frames");
    if (values == null || values.isEmpty()) {
      return error("'frames' must contain at least one frame");
    }

    List<ITileAnimationFrame> frames = new ArrayList<>();
    for (int index = 0; index < values.size(); index++) {
      JsonObject frame;
      try {
        frame = values.getJsonObject(index);
      } catch (ClassCastException ex) {
        return error("Frame " + index + " must be an object");
      }
      int tileId = McpToolHandler.getInt(frame, "tileId", -1);
      int duration = McpToolHandler.getInt(frame, "duration", -1);
      if (tileset.getTile(tileId) == null || duration <= 0) {
        return error("Frame " + index + " needs a valid tileId and positive duration");
      }
      frames.add(new Frame(tileId, duration));
    }
    entry.setAnimation(new TileAnimation(frames));
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Updated tile animation")
        .add("animation", tileAnimationInfo(entry.getId(), entry))
        .build();
  }

  private static JsonObject clearTileAnimation(JsonObject args) {
    TilesetEntry entry = tile(args);
    if (entry == null) {
      return error("Tile not found");
    }
    entry.setAnimation(null);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Cleared animation for tile " + entry.getId())
        .build();
  }

  private static JsonObject listTerrains(JsonObject args) {
    Tileset tileset = tileset(args);
    if (tileset == null) {
      return error("Tileset not found: " + McpToolHandler.getString(args, "tileset", ""));
    }
    JsonArrayBuilder sets = Json.createArrayBuilder();
    List<ITerrainSet> terrainSets = tileset.getTerrainSets();
    for (ITerrainSet terrainSet : terrainSets != null ? terrainSets : List.<ITerrainSet>of()) {
      if (terrainSet instanceof WangSet wangSet) {
        sets.add(terrainSetInfo(wangSet));
      }
    }
    return Json.createObjectBuilder()
        .add("success", true)
        .add("tileset", tileset.getName())
        .add("firstGid", tileset.getFirstGridId())
        .add("terrainSets", sets)
        .add("paintingGuide", terrainPaintingGuide())
        .build();
  }

  private static JsonObject createTerrainSet(JsonObject args) {
    Tileset tileset = tileset(args);
    String name = McpToolHandler.getString(args, "set", "");
    if (tileset == null || name.isBlank()) {
      return error("A valid 'tileset' and non-empty 'set' are required");
    }
    if (terrainSet(tileset, name) != null) {
      return error("Terrain set already exists: " + name);
    }
    TerrainType type;
    try {
      type = TerrainType.valueOf(McpToolHandler.getString(args, "type", "mixed").toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return error("Terrain type must be corner, edge, or mixed");
    }
    WangSet set = new WangSet(name, type);
    tileset.getOrCreateTerrainSets().add(set);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("terrainSet", terrainSetInfo(set))
        .build();
  }

  private static JsonObject deleteTerrainSet(JsonObject args) {
    Tileset tileset = tileset(args);
    WangSet set = terrainSet(tileset, McpToolHandler.getString(args, "set", ""));
    if (tileset == null || set == null) {
      return error("Terrain set not found");
    }
    tileset.getOrCreateTerrainSets().remove(set);
    McpAssetHandler.refreshAssets();
    return success("Deleted terrain set: " + set.getName());
  }

  private static JsonObject addTerrain(JsonObject args) {
    WangSet set = terrainSet(args);
    String name = McpToolHandler.getString(args, "terrain", "");
    if (set == null || name.isBlank()) {
      return error("Terrain set not found or terrain name is blank");
    }
    if (terrain(set, name) != null) {
      return error("Terrain already exists: " + name);
    }
    Color color = color(args, "color", Color.WHITE);
    if (color == null) {
      return error("Invalid terrain color");
    }
    WangColor terrain = new WangColor(name, color);
    try {
      terrain.setTileId(McpToolHandler.getInt(args, "tileId", -1));
      terrain.setProbability(McpToolHandler.getDouble(args, "probability", 1.0));
    } catch (IllegalArgumentException ex) {
      return error(ex.getMessage());
    }
    set.getTerrains().add(terrain);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("terrain", terrainInfo(set, terrain))
        .build();
  }

  private static JsonObject updateTerrain(JsonObject args) {
    WangSet set = terrainSet(args);
    WangColor terrain = terrain(set, McpToolHandler.getString(args, "terrain", ""));
    if (terrain == null) {
      return error("Terrain not found");
    }
    if (args.containsKey("newName")) {
      String newName = args.getString("newName", "");
      if (newName.isBlank()) {
        return error("'newName' cannot be blank");
      }
      WangColor existing = terrain(set, newName);
      if (existing != null && existing != terrain) {
        return error("Terrain already exists: " + newName);
      }
      terrain.setName(newName);
    }
    if (args.containsKey("color")) {
      Color color = color(args, "color", null);
      if (color == null) {
        return error("Invalid terrain color");
      }
      terrain.setColor(color);
    }
    try {
      if (args.containsKey("tileId")) {
        terrain.setTileId(args.getInt("tileId"));
      }
      if (args.containsKey("probability")) {
        terrain.setProbability(args.getJsonNumber("probability").doubleValue());
      }
    } catch (IllegalArgumentException | ClassCastException ex) {
      return error(ex.getMessage());
    }
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("terrain", terrainInfo(set, terrain))
        .build();
  }

  private static JsonObject removeTerrain(JsonObject args) {
    WangSet set = terrainSet(args);
    WangColor terrain = terrain(set, McpToolHandler.getString(args, "terrain", ""));
    if (set == null || terrain == null) {
      return error("Terrain not found");
    }
    int removed = set.getTerrains().indexOf(terrain) + 1;
    set.getTerrains().remove(terrain);
    for (WangTile tile : set.getWangTiles()) {
      int[] wangId = tile.getWangId();
      for (int index = 0; index < wangId.length; index++) {
        if (wangId[index] == removed) {
          wangId[index] = 0;
        } else if (wangId[index] > removed) {
          wangId[index]--;
        }
      }
      tile.setWangId(wangId);
    }
    McpAssetHandler.refreshAssets();
    return success("Removed terrain and repaired Wang IDs: " + terrain.getName());
  }

  private static JsonObject setTileTerrain(JsonObject args) {
    Tileset tileset = tileset(args);
    WangSet set = terrainSet(tileset, McpToolHandler.getString(args, "set", ""));
    int tileId = McpToolHandler.getInt(args, "tileId", -1);
    JsonArray values = args.getJsonArray("wangId");
    if (tileset == null || set == null || tileset.getTile(tileId) == null) {
      return error("Tileset, terrain set, or tile not found");
    }
    if (values == null || values.size() != 8) {
      return error("'wangId' must contain exactly eight terrain indices");
    }
    int[] wangId = new int[8];
    try {
      for (int index = 0; index < wangId.length; index++) {
        wangId[index] = values.getInt(index);
        if (wangId[index] < 0 || wangId[index] > set.getTerrains().size()) {
          return error("Wang ID " + wangId[index] + " is outside 0.." + set.getTerrains().size());
        }
      }
    } catch (ClassCastException ex) {
      return error("'wangId' values must be integers");
    }
    set.getOrCreateWangTile(tileId).setWangId(wangId);
    set.removeWangTileIfEmpty(tileId);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("tileId", tileId)
        .add("wangId", intArray(wangId))
        .build();
  }

  private static JsonObject paintTerrain(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return error("No active map loaded");
    }
    IMap map = Game.world().environment().getMap();
    if (map.getOrientation() == MapOrientations.ISOMETRIC_STAGGERED
        || map.getOrientation() == MapOrientations.HEXAGONAL) {
      return error("Terrain painting is not supported for staggered-isometric or hexagonal maps");
    }

    String layerName = McpToolHandler.getString(args, "layer", "");
    String tilesetName = McpToolHandler.getString(args, "tileset", "");
    String setName = McpToolHandler.getString(args, "set", "");
    String terrainName = McpToolHandler.getString(args, "terrain", "");
    if (tilesetName.isBlank()) {
      return error("Missing required argument: 'tileset'");
    }
    if (setName.isBlank()) {
      return error("Missing required argument: 'set'");
    }
    if (terrainName.isBlank()) {
      return error("Missing required argument: 'terrain'");
    }
    if (layerName.isBlank()) {
      return error("Missing required argument: 'layer'");
    }

    ITileLayer layer = map.getTileLayers().stream()
        .filter(
            candidate ->
                candidate.getName() != null
                    && candidate.getName().equalsIgnoreCase(layerName))
        .findFirst()
        .orElse(null);
    if (layer == null) {
      return error(
          "Tile layer not found on the active map: '"
              + layerName
              + "'. Available tile layers: "
              + map.getTileLayers().stream()
                  .map(ITileLayer::getName)
                  .filter(java.util.Objects::nonNull)
                  .toList());
    }

    ITileset tileset = map.getTilesets().stream()
        .filter(
            candidate ->
                candidate.getName() != null
                    && candidate.getName().equalsIgnoreCase(tilesetName))
        .findFirst()
        .orElse(null);
    if (tileset == null) {
      return error(
          "Tileset is not used by the active map: '"
              + tilesetName
              + "'. Available map tilesets: "
              + map.getTilesets().stream()
                  .map(ITileset::getName)
                  .filter(java.util.Objects::nonNull)
                  .toList());
    }

    WangSet set = terrainSet(tileset, setName);
    if (set == null) {
      return error(
          "Terrain set not found in tileset '"
              + tileset.getName()
              + "': '"
              + setName
              + "'");
    }

    WangColor terrain = terrain(set, terrainName);
    if (terrain == null) {
      return error(
          "Terrain not found in set '"
              + set.getName()
              + "': '"
              + terrainName
              + "'");
    }
    final Set<Point> requestedCells;
    try {
      requestedCells = terrainPaintCells(args);
    } catch (IllegalArgumentException | ClassCastException ex) {
      return error(ex.getMessage());
    }
    if (requestedCells.isEmpty()) {
      return error("Provide x/y, one or more cells, or one or more regions to paint");
    }
    for (Point cell : requestedCells) {
      if (cell.x < 0 || cell.y < 0 || cell.x >= map.getWidth() || cell.y >= map.getHeight()) {
        return error("Paint cell (" + cell.x + ", " + cell.y + ") is outside the active map");
      }
    }

    int terrainIndex = set.getTerrains().indexOf(terrain) + 1;
    Map<Point, Integer> changedCells = new LinkedHashMap<>();
    int invalid = 0;
    UndoManager.forMap(map).layerChanging(layer);
    for (Point cell : requestedCells) {
      TerrainResolver.Result result =
          TerrainResolver.resolve(layer, tileset, set, terrainIndex, cell);
      invalid += result.invalidCells();
      for (var change : result.changes().entrySet()) {
        layer.setTile(change.getKey().x, change.getKey().y, change.getValue());
        changedCells.put(new Point(change.getKey()), change.getValue());
      }
    }
    UndoManager.forMap(map).layerChanged(layer);
    String refreshWarning = McpToolHandler.refreshInspectorAfterMutation(null);
    JsonArrayBuilder changes = Json.createArrayBuilder();
    changedCells.forEach(
        (cell, gid) ->
            changes.add(
                Json.createObjectBuilder()
                    .add("x", cell.x)
                    .add("y", cell.y)
                    .add("gid", gid)));
    JsonObjectBuilder result = Json.createObjectBuilder()
        .add("success", true)
        .add("requestedCells", requestedCells.size())
        .add("changedTiles", changedCells.size())
        .add("invalidCells", invalid)
        .add("changes", changes);
    if (refreshWarning != null) {
      result.add("warnings", Json.createArrayBuilder().add(refreshWarning));
    }
    return result.build();
  }

  private static Set<Point> terrainPaintCells(JsonObject args) {
    Set<Point> cells = new LinkedHashSet<>();
    boolean hasX = args != null && args.containsKey("x");
    boolean hasY = args != null && args.containsKey("y");
    if (hasX != hasY) {
      throw new IllegalArgumentException("'x' and 'y' must be provided together");
    }
    if (hasX) {
      int startX = McpToolHandler.getInt(args, "x", 0);
      int startY = McpToolHandler.getInt(args, "y", 0);
      int width = McpToolHandler.getInt(args, "width", 1);
      int height = McpToolHandler.getInt(args, "height", 1);
      addRegion(cells, startX, startY, width, height);
    } else if (args != null && (args.containsKey("width") || args.containsKey("height"))) {
      throw new IllegalArgumentException("'width' and 'height' require 'x' and 'y'");
    }

    if (args != null && args.containsKey("cells")) {
      JsonArray sparseCells = args.getJsonArray("cells");
      if (sparseCells == null) {
        throw new IllegalArgumentException("'cells' must be an array");
      }
      for (int index = 0; index < sparseCells.size(); index++) {
        JsonObject cell = sparseCells.getJsonObject(index);
        if (!cell.containsKey("x") || !cell.containsKey("y")) {
          throw new IllegalArgumentException("'cells[" + index + "]' requires x and y");
        }
        cells.add(new Point(cell.getInt("x"), cell.getInt("y")));
      }
    }

    if (args != null && args.containsKey("regions")) {
      JsonArray regions = args.getJsonArray("regions");
      if (regions == null) {
        throw new IllegalArgumentException("'regions' must be an array");
      }
      for (int index = 0; index < regions.size(); index++) {
        JsonObject region = regions.getJsonObject(index);
        if (!region.containsKey("x")
            || !region.containsKey("y")
            || !region.containsKey("width")
            || !region.containsKey("height")) {
          throw new IllegalArgumentException(
              "'regions[" + index + "]' requires x, y, width, and height");
        }
        addRegion(
            cells,
            region.getInt("x"),
            region.getInt("y"),
            region.getInt("width"),
            region.getInt("height"));
      }
    }
    return cells;
  }

  private static void addRegion(
      Set<Point> cells, int startX, int startY, int width, int height) {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("Terrain paint region width and height must be positive");
    }
    for (int y = startY; y < startY + height; y++) {
      for (int x = startX; x < startX + width; x++) {
        cells.add(new Point(x, y));
      }
    }
  }

  private static JsonObject terrainPaintingGuide() {
    return Json.createObjectBuilder()
        .add(
            "workflow",
            Json.createArrayBuilder()
                .add("Call list-terrains and choose a terrain set and terrain by name.")
                .add("Call paint-terrain with required arguments tileset, set, terrain, and layer, plus either cells or regions.")
                .add("The resolver selects tiles and repairs neighboring Wang transitions automatically."))
        .add(
            "requiredArguments",
            Json.createArrayBuilder()
                .add("tileset")
                .add("set")
                .add("terrain")
                .add("layer"))
        .add(
            "wangIdOrder",
            Json.createArrayBuilder()
                .add("top")
                .add("top-right")
                .add("right")
                .add("bottom-right")
                .add("bottom")
                .add("bottom-left")
                .add("left")
                .add("top-left"))
        .add(
            "tileIds",
            "Wang assignments use tileset-local tileId values. Map layers store global GIDs using gid = firstGid + tileId; paint-terrain performs that conversion.")
        .add(
            "bulkPainting",
            "Use cells for disconnected positions, regions for rectangles, or combine both in one paint-terrain call.")
        .build();
  }

  private static JsonObject tileAnimationInfo(int tileId, TilesetEntry entry) {
    JsonArrayBuilder frames = Json.createArrayBuilder();
    for (ITileAnimationFrame frame : entry.getAnimation().getFrames()) {
      frames.add(Json.createObjectBuilder()
          .add("tileId", frame.getTileId())
          .add("duration", frame.getDuration()));
    }
    return Json.createObjectBuilder()
        .add("tileId", tileId)
        .add("totalDuration", entry.getAnimation().getTotalDuration())
        .add("frames", frames)
        .build();
  }

  private static JsonObject terrainSetInfo(WangSet set) {
    JsonArrayBuilder terrains = Json.createArrayBuilder();
    for (ITerrain item : set.getTerrains()) {
      if (item instanceof WangColor terrain) {
        terrains.add(terrainInfo(set, terrain));
      }
    }
    JsonArrayBuilder tiles = Json.createArrayBuilder();
    set.getWangTiles().stream()
        .sorted((first, second) -> Integer.compare(first.getTileId(), second.getTileId()))
        .forEach(tile -> tiles.add(Json.createObjectBuilder()
            .add("tileId", tile.getTileId())
            .add("wangId", intArray(tile.getWangId()))));
    return Json.createObjectBuilder()
        .add("name", set.getName())
        .add("type", set.getType().name().toLowerCase(Locale.ROOT))
        .add("terrains", terrains)
        .add("tiles", tiles)
        .build();
  }

  private static JsonObject terrainInfo(WangSet set, WangColor terrain) {
    return Json.createObjectBuilder()
        .add("index", set.getTerrains().indexOf(terrain) + 1)
        .add("name", terrain.getName())
        .add("color", String.format("#%08X", terrain.getColor().getRGB()))
        .add("tileId", terrain.getTileId())
        .add("probability", terrain.getProbability())
        .build();
  }

  private static JsonArray intArray(int[] values) {
    JsonArrayBuilder array = Json.createArrayBuilder();
    Arrays.stream(values).forEach(array::add);
    return array.build();
  }

  private static JsonObject renderTileset(JsonObject args) {
    Tileset tileset = tileset(args);
    if (tileset == null) {
      return error("Tileset not found: " + McpToolHandler.getString(args, "tileset", ""));
    }
    int scale = Math.max(1, McpToolHandler.getInt(args, "scale", 4));
    int tileWidth = Math.max(1, tileset.getTileWidth());
    int tileHeight = Math.max(1, tileset.getTileHeight());
    int columns = Math.max(1, tileset.getColumns());
    int rows = Math.max(1, (int) Math.ceil((double) tileset.getTileCount() / columns));
    int labelHeight = Math.max(12, 10 * scale / 2);
    BufferedImage image = new BufferedImage(columns * tileWidth * scale + 1,
        rows * (tileHeight * scale + labelHeight) + 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      graphics.setColor(new Color(38, 38, 44));
      graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      for (int tileId = 0; tileId < tileset.getTileCount(); tileId++) {
        int col = tileId % columns;
        int row = tileId / columns;
        int x = col * tileWidth * scale;
        int y = row * (tileHeight * scale + labelHeight);
        if (tileset.getTile(tileId) instanceof TilesetEntry entry && entry.getBasicImage() != null) {
          graphics.drawImage(entry.getBasicImage(), x, y, tileWidth * scale, tileHeight * scale, null);
        }
        graphics.setColor(new Color(255, 255, 255, 210));
        graphics.drawRect(x, y, tileWidth * scale, tileHeight * scale);
        graphics.setColor(Color.WHITE);
        graphics.drawString(String.valueOf(tileId), x + 2, y + tileHeight * scale + labelHeight - 3);
      }
    } finally {
      graphics.dispose();
    }
    return imageResult("tileset", tileset.getName(), image)
        .add("tileWidth", tileWidth).add("tileHeight", tileHeight).add("columns", columns)
        .add("tileCount", tileset.getTileCount()).add("scale", scale).build();
  }

  private static JsonObject findTileUsage(JsonObject args) {
    Tileset tileset = tileset(args);
    if (tileset == null) {
      return error("Tileset not found: " + McpToolHandler.getString(args, "tileset", ""));
    }
    String requestedMap = McpToolHandler.getString(args, "mapId", "");
    int requestedTileId = McpToolHandler.getInt(args, "tileId", -1);
    Map<Integer, Integer> occurrences = new LinkedHashMap<>();
    Map<String, Map<Integer, Integer>> neighbors = new LinkedHashMap<>();
    for (String direction : List.of("top", "bottom", "left", "right")) neighbors.put(direction, new LinkedHashMap<>());
    JsonArrayBuilder maps = Json.createArrayBuilder();
    if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map == null || (!requestedMap.isBlank() && !requestedMap.equalsIgnoreCase(map.getName()))) continue;
        int mapMatches = analyzeUsage(map, tileset, requestedTileId, occurrences, neighbors);
        maps.add(Json.createObjectBuilder().add("mapId", map.getName()).add("occurrenceCount", mapMatches));
      }
    }
    JsonObjectBuilder neighborJson = Json.createObjectBuilder();
    neighbors.forEach((direction, counts) -> neighborJson.add(direction, frequencyArray(counts)));
    return Json.createObjectBuilder().add("success", true).add("tileset", tileset.getName())
        .add("tileId", requestedTileId).add("maps", maps).add("occurrences", frequencyArray(occurrences))
        .add("neighborFrequencies", neighborJson).build();
  }

  private static int analyzeUsage(IMap map, Tileset tileset, int requestedTileId,
      Map<Integer, Integer> occurrences, Map<String, Map<Integer, Integer>> neighbors) {
    int matches = 0;
    for (ITileLayer layer : map.getTileLayers()) {
      for (int y = 0; y < map.getHeight(); y++) for (int x = 0; x < map.getWidth(); x++) {
        ITile tile = layer.getTile(x, y);
        if (tile == null || tile.getTilesetEntry() == null || tile.getTilesetEntry().getTileset() != tileset) continue;
        int id = tile.getTilesetEntry().getId();
        if (requestedTileId >= 0 && id != requestedTileId) continue;
        matches++; increment(occurrences, id);
        addNeighbor(layer, x, y - 1, "top", neighbors); addNeighbor(layer, x, y + 1, "bottom", neighbors);
        addNeighbor(layer, x - 1, y, "left", neighbors); addNeighbor(layer, x + 1, y, "right", neighbors);
      }
    }
    return matches;
  }

  private static void addNeighbor(ITileLayer layer, int x, int y, String direction,
      Map<String, Map<Integer, Integer>> neighbors) {
    if (x < 0 || y < 0) return;
    ITile neighbor = layer.getTile(x, y);
    if (neighbor != null) increment(neighbors.get(direction), neighbor.getGridId());
  }

  private static JsonObject renderTileContext(JsonObject args) {
    IMap map = map(args);
    if (map == null) return error("Map not found");
    int x = McpToolHandler.getInt(args, "x", 0), y = McpToolHandler.getInt(args, "y", 0);
    int radius = Math.max(0, McpToolHandler.getInt(args, "radius", 2));
    int scale = Math.max(1, McpToolHandler.getInt(args, "scale", 4));
    String mode = McpToolHandler.getString(args, "mode", "selected-layer").toLowerCase(Locale.ROOT);
    List<ITileLayer> layers = contextLayers(map, McpToolHandler.getString(args, "layer", ""), mode);
    if (layers.isEmpty()) return error("Tile layer not found");
    BufferedImage image = renderContext(map, layers, x, y, radius, scale, "layer-stack".equals(mode));
    return imageResult("mapId", map.getName(), image).add("mode", mode).add("center", pointJson(x, y))
        .add("radius", radius).add("layers", layerNames(layers)).build();
  }

  private static JsonObject previewTileEdits(JsonObject args) {
    IMap map = map(args);
    if (map == null) return error("Map not found");
    ITileLayer layer = tileLayer(map, McpToolHandler.getString(args, "layer", ""));
    JsonArray edits = args.getJsonArray("edits");
    if (layer == null || edits == null || edits.isEmpty()) return error("A target layer and at least one edit are required");
    int minX = map.getWidth(), minY = map.getHeight(), maxX = -1, maxY = -1;
    JsonArrayBuilder warnings = Json.createArrayBuilder();
    Map<Long, int[]> originals = new LinkedHashMap<>();
    try {
      for (int index = 0; index < edits.size(); index++) {
        JsonObject edit = edits.getJsonObject(index); int x = McpToolHandler.getInt(edit, "x", -1); int y = McpToolHandler.getInt(edit, "y", -1);
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) { warnings.add("Edit " + index + " is outside map bounds"); continue; }
        originals.computeIfAbsent(
            (((long) x) << 32) | (y & 0xffffffffL),
            _ -> new int[] {x, y, layer.getTile(x, y) != null ? layer.getTile(x, y).getGridId() : 0});
        layer.setTile(x, y, McpToolHandler.getInt(edit, "gid", 0));
        minX = Math.min(minX, x); minY = Math.min(minY, y); maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
      }
      if (maxX < 0) return error("No valid in-bounds edits supplied");
      int padding = Math.max(0, McpToolHandler.getInt(args, "padding", 1));
      int centerX = (minX + maxX) / 2, centerY = (minY + maxY) / 2;
      int radius = Math.max(Math.max(maxX - minX, maxY - minY) / 2 + padding, padding);
      BufferedImage image = renderContext(map, List.of(layer), centerX, centerY, radius,
          Math.max(1, McpToolHandler.getInt(args, "scale", 4)), false);
      int collisionCount = 0;
      for (int[] original : originals.values()) {
        ITile preview = layer.getTile(original[0], original[1]);
        if (preview != null && preview.getTilesetEntry() != null && preview.getTilesetEntry().getCollisionInfo() != null) collisionCount++;
      }
      return imageResult("mapId", map.getName(), image).add("layer", layer.getName())
          .add("affectedBounds", boundsJson(minX, minY, maxX - minX + 1, maxY - minY + 1))
          .add("affectedCollisionTileCount", collisionCount).add("warnings", warnings).build();
    } finally {
      for (int[] original : originals.values()) layer.setTile(original[0], original[1], original[2]);
    }
  }

  private static BufferedImage renderContext(IMap map, List<ITileLayer> layers, int centerX, int centerY,
      int radius, int scale, boolean stack) {
    int count = radius * 2 + 1, tileWidth = Math.max(1, map.getTileWidth()), tileHeight = Math.max(1, map.getTileHeight());
    int panelWidth = count * tileWidth * scale, panelHeight = count * tileHeight * scale;
    BufferedImage image = new BufferedImage(panelWidth, panelHeight * (stack ? layers.size() : 1), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try { graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
        List<ITileLayer> drawLayers = stack ? List.of(layers.get(layerIndex)) : layers;
        int offsetY = stack ? layerIndex * panelHeight : 0;
        for (ITileLayer layer : drawLayers) for (int y = 0; y < count; y++) for (int x = 0; x < count; x++) {
          ITile tile = layer.getTile(centerX - radius + x, centerY - radius + y);
          if (tile != null && tile.getImage() != null) graphics.drawImage(tile.getImage(), x * tileWidth * scale, offsetY + y * tileHeight * scale, tileWidth * scale, tileHeight * scale, null);
        }
        if (!stack) break;
      }
    } finally { graphics.dispose(); }
    return image;
  }

  private static JsonObjectBuilder imageResult(String identityKey, String identity, BufferedImage image) {
    String base64 = encodePng(image); String filePath = writePreview(image);
    JsonObjectBuilder builder = Json.createObjectBuilder().add("success", true).add(identityKey, identity == null ? "" : identity)
        .add("imageBase64", base64).add("imageData", "data:image/png;base64," + base64).add("mimeType", "image/png");
    if (filePath != null) builder.add("filePath", filePath);
    return builder;
  }

  private static String encodePng(BufferedImage image) { try { ByteArrayOutputStream out = new ByteArrayOutputStream(); javax.imageio.ImageIO.write(image, "png", out); return Base64.getEncoder().encodeToString(out.toByteArray()); } catch (Exception ex) { return ""; } }
  private static String writePreview(BufferedImage image) { try { Path path = Files.createTempFile("utiliti-mcp-", ".png"); javax.imageio.ImageIO.write(image, "png", path.toFile()); return path.toString(); } catch (Exception ex) { return null; } }
  private static IMap map(JsonObject args) { String name = McpToolHandler.getString(args, "mapId", ""); if (Editor.instance().getGameFile() != null) for (IMap candidate : Editor.instance().getGameFile().getMaps()) if (name.isBlank() || name.equalsIgnoreCase(candidate.getName())) return candidate; return Game.world().environment() != null ? Game.world().environment().getMap() : null; }
  private static ITileLayer tileLayer(IMap map, String name) { for (ITileLayer layer : map.getTileLayers()) if (name.isBlank() || name.equalsIgnoreCase(layer.getName())) return layer; return null; }
  private static List<ITileLayer> contextLayers(IMap map, String name, String mode) { if ("selected-layer".equals(mode)) { ITileLayer layer = tileLayer(map, name); return layer == null ? List.of() : List.of(layer); } return map.getTileLayers(); }
  private static JsonArray frequencyArray(Map<Integer, Integer> values) { JsonArrayBuilder result = Json.createArrayBuilder(); values.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).forEach(entry -> result.add(Json.createObjectBuilder().add("gid", entry.getKey()).add("count", entry.getValue()))); return result.build(); }
  private static void increment(Map<Integer, Integer> values, int key) { values.merge(key, 1, Integer::sum); }
  private static JsonArray layerNames(List<ITileLayer> layers) { JsonArrayBuilder result = Json.createArrayBuilder(); layers.forEach(layer -> result.add(layer.getName())); return result.build(); }
  private static JsonObject pointJson(int x, int y) { return Json.createObjectBuilder().add("x", x).add("y", y).build(); }
  private static JsonObject boundsJson(int x, int y, int width, int height) { return Json.createObjectBuilder().add("x", x).add("y", y).add("width", width).add("height", height).build(); }

  private static Tileset tileset(JsonObject args) {
    return McpAssetHandler.findTileset(McpToolHandler.getString(args, "tileset", ""));
  }

  private static TilesetEntry tile(JsonObject args) {
    return tile(tileset(args), args);
  }

  private static TilesetEntry tile(Tileset tileset, JsonObject args) {
    int tileId = McpToolHandler.getInt(args, "tileId", -1);
    return tileset != null && tileset.getTile(tileId) instanceof TilesetEntry entry ? entry : null;
  }

  private static WangSet terrainSet(JsonObject args) {
    return terrainSet(tileset(args), McpToolHandler.getString(args, "set", ""));
  }

  private static WangSet terrainSet(ITileset tileset, String name) {
    if (tileset == null || tileset.getTerrainSets() == null) {
      return null;
    }
    return tileset.getTerrainSets().stream()
        .filter(WangSet.class::isInstance)
        .map(WangSet.class::cast)
        .filter(set -> set.getName() != null && set.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(null);
  }

  private static WangColor terrain(WangSet set, String name) {
    if (set == null) {
      return null;
    }
    return set.getTerrains().stream()
        .filter(WangColor.class::isInstance)
        .map(WangColor.class::cast)
        .filter(terrain -> terrain.getName() != null && terrain.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(null);
  }

  private static Color color(JsonObject args, String property, Color defaultValue) {
    if (!args.containsKey(property)) {
      return defaultValue;
    }
    return ColorHelper.decode(args.getString(property, ""));
  }

  private static JsonObject success(String message) {
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", message)
        .build();
  }

  private static JsonObject error(String message) {
    return Json.createObjectBuilder()
        .add("success", false)
        .add("error", message == null ? "Invalid request" : message)
        .build();
  }
}
