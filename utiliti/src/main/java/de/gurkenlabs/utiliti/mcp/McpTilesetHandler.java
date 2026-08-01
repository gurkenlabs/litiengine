package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
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
import de.gurkenlabs.utiliti.controller.tool.TerrainResolver;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.awt.Color;
import java.awt.Point;
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
      "paint-terrain");

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
