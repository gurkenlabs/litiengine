package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.PolyShape;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.environment.tilemap.TmxPropertyMetadataRegistry;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.view.components.UI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class McpToolHandler {

  private static final Logger log = Logger.getLogger(McpToolHandler.class.getName());

  // ---- Robust Type-Safe JSON Parsing Helpers ----
  static Float getFloat(JsonObject args, String key, Float defaultValue) {
    if (args == null || !args.containsKey(key) || args.isNull(key)) {
      return defaultValue;
    }
    JsonValue val = args.get(key);
    if (val instanceof JsonNumber num) {
      return (float) num.doubleValue();
    } else if (val.getValueType() == JsonValue.ValueType.STRING) {
      try {
        return Float.parseFloat(args.getString(key));
      } catch (Exception e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  static Double getDouble(JsonObject args, String key, Double defaultValue) {
    if (args == null || !args.containsKey(key) || args.isNull(key)) {
      return defaultValue;
    }
    JsonValue val = args.get(key);
    if (val instanceof JsonNumber num) {
      return num.doubleValue();
    } else if (val.getValueType() == JsonValue.ValueType.STRING) {
      try {
        return Double.parseDouble(args.getString(key));
      } catch (Exception e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  static Integer getInt(JsonObject args, String key, Integer defaultValue) {
    if (args == null || !args.containsKey(key) || args.isNull(key)) {
      return defaultValue;
    }
    JsonValue val = args.get(key);
    if (val instanceof JsonNumber num) {
      return num.intValue();
    } else if (val.getValueType() == JsonValue.ValueType.STRING) {
      try {
        return Integer.parseInt(args.getString(key));
      } catch (Exception e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  static Boolean getBoolean(JsonObject args, String key, Boolean defaultValue) {
    if (args == null || !args.containsKey(key) || args.isNull(key)) {
      return defaultValue;
    }
    JsonValue val = args.get(key);
    if (val.getValueType() == JsonValue.ValueType.TRUE) {
      return true;
    } else if (val.getValueType() == JsonValue.ValueType.FALSE) {
      return false;
    } else if (val.getValueType() == JsonValue.ValueType.STRING) {
      return Boolean.parseBoolean(args.getString(key));
    }
    return defaultValue;
  }

  static String getString(JsonObject args, String key, String defaultValue) {
    if (args == null || !args.containsKey(key) || args.isNull(key)) {
      return defaultValue;
    }
    JsonValue val = args.get(key);
    if (val.getValueType() == JsonValue.ValueType.STRING) {
      return args.getString(key);
    }
    return val.toString();
  }

  public static JsonObject getToolsList() {
    JsonArrayBuilder toolsArr = Json.createArrayBuilder();

    // ---- Project & File Commands ----
    toolsArr.add(createToolDef("save-project", "Save the active project game file (.litidata)", null));
    
    JsonObjectBuilder loadParams = Json.createObjectBuilder();
    loadParams.add("path", createParam("string", "Path to project file (.litidata)", true));
    toolsArr.add(createToolDef("load-project", "Load a project file into the editor", loadParams.build()));

    // ---- Undo / Redo Commands ----
    toolsArr.add(createToolDef("undo", "Undo the last editor operation", null));
    toolsArr.add(createToolDef("redo", "Redo the last undone editor operation", null));

    // ---- Map Management Commands ----
    JsonObjectBuilder selectMapParams = Json.createObjectBuilder();
    selectMapParams.add("name", createParam("string", "Map name to switch to", true));
    toolsArr.add(createToolDef("select-map", "Switch the active editor view to specified map", selectMapParams.build()));

    JsonObjectBuilder createMapParams = Json.createObjectBuilder();
    createMapParams.add("name", createParam("string", "New map name", false));
    createMapParams.add(
        "orientation",
        createParam(
            "string",
            "Map orientation: orthogonal, isometric, staggered, or hexagonal",
            false));
    createMapParams.add(
        "width", createParam("integer", "Map width in tiles (default 30)", false));
    createMapParams.add(
        "height", createParam("integer", "Map height in tiles (default 20)", false));
    createMapParams.add(
        "tileWidth", createParam("integer", "Tile width in pixels (default 32)", false));
    createMapParams.add(
        "tileHeight", createParam("integer", "Tile height in pixels (default 32)", false));
    createMapParams.add(
        "staggerAxis",
        createParam("string", "Stagger axis X or Y (default Y)", false));
    createMapParams.add(
        "staggerIndex",
        createParam("string", "Stagger index ODD or EVEN (default ODD)", false));
    createMapParams.add(
        "hexSideLength",
        createParam(
            "integer", "Even hex side length in pixels (default 0)", false));
    createMapParams.add(
        "tilesets",
        Json.createObjectBuilder()
            .add("type", "array")
            .add(
                "description",
                "Existing project tileset names to attach to the new map, in GID order")
            .add("items", Json.createObjectBuilder().add("type", "string"))
            .add("uniqueItems", true)
            .add("required", false));
    createMapParams.add(
        "overwrite",
        createParam("boolean", "Replace/overwrite existing map with same name in ONE step", false));
    createMapParams.add(
        "initialLayers",
        Json.createObjectBuilder()
            .add("type", "array")
            .add("description", "Initial layers to create on the map in ONE step, e.g. [{'name': 'ground', 'type': 'tile'}, {'name': 'walls', 'type': 'tile'}]")
            .add("items", Json.createObjectBuilder()
                .add("type", "object")
                .add("properties", Json.createObjectBuilder()
                    .add("name", Json.createObjectBuilder().add("type", "string"))
                    .add("type", Json.createObjectBuilder().add("type", "string").add("description", "Layer type: tile, object, or group")))
                .add("required", Json.createArrayBuilder().add("name")))
            .add("required", false));
    toolsArr.add(createToolDef("create-map", "Create a new map in the active project. Use 'overwrite': true to replace existing maps, and 'initialLayers' to define all layers in ONE call.", createMapParams.build()));

    JsonObjectBuilder deleteMapParams = Json.createObjectBuilder();
    deleteMapParams.add("name", createParam("string", "Map name to delete", true));
    toolsArr.add(createToolDef("delete-map", "Delete a map from the active project", deleteMapParams.build()));

    JsonObjectBuilder reassignIdsParams = Json.createObjectBuilder();
    reassignIdsParams.add("minId", createParam("number", "Starting minimum ID (default: 1)", false));
    JsonObject projectReferenceItem = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("idProperty", Json.createObjectBuilder()
                .add("type", "string")
                .add("description", "Property containing one or more referenced object IDs"))
            .add("targetMapProperty", Json.createObjectBuilder()
                .add("type", "string")
                .add("description", "Sibling property containing the referenced map name"))
            .add("targetMapValue", Json.createObjectBuilder()
                .add("type", "string")
                .add("description", "Expected target map name; defaults to the active map name")))
        .add("required", Json.createArrayBuilder().add("idProperty").add("targetMapProperty"))
        .build();
    reassignIdsParams.add(
        "projectReferences",
        createArrayParam(
            "Explicit custom cross-map reference declarations to update project-wide",
            false,
            projectReferenceItem));
    toolsArr.add(createToolDef("reassign-map-ids", "Reassign all map object IDs sequentially on the active map", reassignIdsParams.build()));

    toolsArr.add(createToolDef("export-map-snapshot", "Export a PNG snapshot rendering all visible tile and object layers", null));
    toolsArr.add(createToolDef("get-canvas-snapshot", "Capture the current editor canvas view including all debug features, grid, selection handles, and overlays", null));
    toolsArr.add(createToolDef("open-snapshot-folder", "Open the screenshots directory in system file explorer", null));

    toolsArr.add(createToolDef("validate-map", "Run comprehensive diagnostic validation on current map (duplicate IDs, trigger targets, collision overlap, asset references)", null));

    JsonObjectBuilder getLogsParams = Json.createObjectBuilder();
    getLogsParams.add("level", createParam("string", "Severity level filter ('all', 'warning', 'error')", false));
    getLogsParams.add("limit", createParam("number", "Maximum number of recent entries to return (default 50)", false));
    toolsArr.add(createToolDef("get-logs", "Retrieve recent editor log entries, warning/error counts, and stack traces", getLogsParams.build()));

    JsonObjectBuilder getPropDocsParams = Json.createObjectBuilder();
    getPropDocsParams.add("property", createParam("string", "Filter documentation by property key name", false));
    getPropDocsParams.add("category", createParam("string", "Filter documentation by property category ('Combat', 'Collision', 'Graphics', 'Light', 'Movement', 'Sound', 'Trigger', etc.)", false));
    getPropDocsParams.add("type", createParam("string", "Filter documentation for a specific map object type ('CREATURE', 'PROP', 'LIGHTSOURCE', 'TRIGGER', etc.)", false));
    toolsArr.add(createToolDef("get-property-docs", "Get comprehensive documentation and schema metadata for TMX map object properties and entity types", getPropDocsParams.build()));

    // ---- Geometry Queries & Asset Search ----
    JsonObjectBuilder geomQueryParams = Json.createObjectBuilder();
    geomQueryParams.add("mode", createParam("string", "Query mode: 'overlap', 'containment', 'distance', 'layer-collision'", true));
    geomQueryParams.add("id", createParam("number", "Target entity ID", false));
    geomQueryParams.add("name", createParam("string", "Target entity name", false));
    geomQueryParams.add("x", createParam("number", "X coordinate or region X", false));
    geomQueryParams.add("y", createParam("number", "Y coordinate or region Y", false));
    geomQueryParams.add("width", createParam("number", "Region width", false));
    geomQueryParams.add("height", createParam("number", "Region height", false));
    geomQueryParams.add("x2", createParam("number", "Second point X coordinate", false));
    geomQueryParams.add("y2", createParam("number", "Second point Y coordinate", false));
    geomQueryParams.add("layer", createParam("string", "Layer name for collision/spatial query", false));
    toolsArr.add(createToolDef("query-geometry", "Perform spatial geometry queries (overlap, containment, distance, layer collision)", geomQueryParams.build()));

    JsonObjectBuilder searchAssetParams = Json.createObjectBuilder();
    searchAssetParams.add("query", createParam("string", "Search term or substring", false));
    searchAssetParams.add("type", createParam("string", "Asset type filter ('spritesheet' or 'sound')", false));
    toolsArr.add(createToolDef("search-assets", "Search project asset catalog for spritesheets and sounds", searchAssetParams.build()));

    // ---- Map Properties & Lighting ----
    JsonObjectBuilder setGravityParams = Json.createObjectBuilder();
    setGravityParams.add("gravity", createParam("number", "Map gravity strength", true));
    toolsArr.add(createToolDef("set-gravity", "Set map gravity strength", setGravityParams.build()));

    JsonObjectBuilder setAmbientLightParams = Json.createObjectBuilder();
    setAmbientLightParams.add("color", createParam("string", "Hex color string (e.g. #FF0000)", true));
    setAmbientLightParams.add("alpha", createParam("number", "Alpha opacity (0-255)", false));
    toolsArr.add(createToolDef("set-ambient-light", "Set map ambient light color and opacity", setAmbientLightParams.build()));

    // ---- Entity Management & Listing ----
    JsonObjectBuilder listEntitiesParams = Json.createObjectBuilder();
    listEntitiesParams.add("type", createParam("string", "Filter by entity type", false));
    listEntitiesParams.add("layer", createParam("string", "Filter by layer name", false));
    listEntitiesParams.add("selectedOnly", createParam("boolean", "Filter selected entities only", false));
    toolsArr.add(createToolDef("list-entities", "List entities on active map with full transform and property details", listEntitiesParams.build()));

    // ---- NATIVE ADD TOOLS FOR ALL MAP OBJECT TYPES ----
    JsonObjectBuilder addPropParams = createBaseAddParams();
    addPropParams.add(
        "spritesheetName",
        createParam(
            "string",
            "Prop sprite family or existing variant; defaults to the editor-selected prop sprite",
            false));
    addPropParams.add("material", createParam("string", "Prop material (valid: UNDEFINED, CERAMIC, FLESH, FOLIAGE, PLASTIC, STEEL, STONE, WOOD)", false));
    addPropParams.add("addShadow", createParam("boolean", "Add shadow to prop", false));
    addPropParams.add("indestructible", createParam("boolean", "Indestructible prop state", false));
    addPropParams.add("collision", createParam("boolean", "Enable collision box", false));
    addPropParams.add("collisionType", createParam("string", "Collision type (STATIC, DYNAMIC)", false));
    toolsArr.add(createToolDef("add-prop", "Add a native Prop entity to the map", addPropParams.build()));

    JsonObjectBuilder addCreatureParams = createBaseAddParams();
    addCreatureParams.add(
        "spritesheetName",
        createParam(
            "string",
            "Creature sprite family or existing variant; defaults to the editor-selected creature sprite",
            false));
    addCreatureParams.add("scaleSprite", createParam("boolean", "Scale sprite to bounding box", false));
    addCreatureParams.add("velocity", createParam("number", "Movement velocity", false));
    addCreatureParams.add("hitpoints", createParam("number", "Max hitpoints", false));
    addCreatureParams.add("team", createParam("number", "Team number", false));
    toolsArr.add(createToolDef("add-creature", "Add a native Creature entity to the map", addCreatureParams.build()));

    JsonObjectBuilder addCollisionParams = createBaseAddParams();
    addCollisionParams.add("collisionType", createParam("string", "Collision type (STATIC, DYNAMIC)", false));
    toolsArr.add(createToolDef("add-collisionbox", "Add a native Collision Box entity to the map", addCollisionParams.build()));

    JsonObjectBuilder addTriggerParams = createBaseAddParams();
    addTriggerParams.add("message", createParam("string", "Trigger message string", false));
    addTriggerParams.add("activation", createParam("string", "Activation mode (INTERACT, COLLISION)", false));
    addTriggerParams.add("targets", createParam("string", "Comma-separated target entity IDs (e.g. '101,102') or target entity names", false));
    addTriggerParams.add("cooldown", createParam("number", "Cooldown in ms", false));
    addTriggerParams.add("oneTime", createParam("boolean", "Fire trigger one-time only", false));
    toolsArr.add(createToolDef("add-trigger", "Add a native Trigger entity to the map", addTriggerParams.build()));

    JsonObjectBuilder addSpawnParams = createBaseAddParams();
    addSpawnParams.add("spawnType", createParam("string", "Entity type allowed to spawn", false));
    addSpawnParams.add("direction", createParam("string", "Facing direction (DOWN, LEFT, RIGHT, UP)", false));
    toolsArr.add(createToolDef("add-spawnpoint", "Add a native Spawnpoint entity to the map", addSpawnParams.build()));

    JsonObjectBuilder addAreaParams = createBaseAddParams();
    toolsArr.add(createToolDef("add-area", "Add a native Map Area entity to the map", addAreaParams.build()));

    JsonObjectBuilder addLightParams = createBaseAddParams();
    addLightParams.add("lightColor", createParam("string", "Light color hex string", false));
    addLightParams.add("lightIntensity", createParam("number", "Light intensity (0-255)", false));
    addLightParams.add("lightShape", createParam("string", "Light shape (ELLIPSE, RECTANGLE)", false));
    addLightParams.add("lightActive", createParam("boolean", "Light active state", false));
    toolsArr.add(createToolDef("add-light", "Add a native LightSource entity to the map", addLightParams.build()));

    JsonObjectBuilder addShadowParams = createBaseAddParams();
    addShadowParams.add("shadowType", createParam("string", "Shadow type", false));
    toolsArr.add(createToolDef("add-static-shadow", "Add a native Static Shadow entity to the map", addShadowParams.build()));

    JsonObjectBuilder addEmitterParams = createBaseAddParams();
    addEmitterParams.add("emitterData", createParam("string", "Emitter particle asset or data name", false));
    toolsArr.add(createToolDef("add-emitter", "Add a native Particle Emitter entity to the map", addEmitterParams.build()));

    JsonObjectBuilder addSoundParams = createBaseAddParams();
    addSoundParams.add("soundName", createParam("string", "Sound resource name", false));
    addSoundParams.add("volume", createParam("number", "Sound volume (0.0 to 1.0)", false));
    addSoundParams.add("loop", createParam("boolean", "Loop sound playback", false));
    addSoundParams.add("range", createParam("number", "Sound range in pixels", false));
    toolsArr.add(createToolDef("add-sound-source", "Add a native Sound Source entity to the map", addSoundParams.build()));

    JsonObjectBuilder addItemSpawnParams = createBaseAddParams();
    addItemSpawnParams.add("itemType", createParam("string", "Item asset name or type", true));
    addItemSpawnParams.add("cooldown", createParam("number", "Respawn cooldown in ms", false));
    addItemSpawnParams.add(
        "spritesheetName",
        createParam(
            "string",
            "Prop sprite family or spritesheet resource; defaults to the editor-selected prop sprite",
            false));
    toolsArr.add(createToolDef("add-item-spawn", "Helper tool to author item spawn entities", addItemSpawnParams.build()));

    // ---- Generic Entity Fallback & Batch Operations ----
    JsonObjectBuilder addEntityParams = Json.createObjectBuilder();
    addEntityParams.add("type", createParam("string", "Map object type (PROP, CREATURE, LIGHTSOURCE, TRIGGER, SPAWNPOINT, SOUNDSOURCE, COLLISIONBOX, AREA, EMITTER, STATICSHADOW)", true));
    addEntityParams.add("name", createParam("string", "Entity name", false));
    addEntityParams.add("x", createParam("number", "X position in pixels", true));
    addEntityParams.add("y", createParam("number", "Y position in pixels", true));
    addEntityParams.add("width", createParam("number", "Width in pixels", false));
    addEntityParams.add("height", createParam("number", "Height in pixels", false));
    addEntityParams.add("layer", createParam("string", "Target layer name", false));
    addEntityParams.add("shapeType", createParam("string", "Shape geometry type: rectangle, polyline, polygon, point, ellipse", false));
    addEntityParams.add("points", createParam("array", "Polyline or polygon points", false));
    addEntityParams.add(
        "spritesheetName",
        createParam(
            "string",
            "Prop or creature sprite family, or an existing family spritesheet resource",
            false));
    addEntityParams.add("material", createParam("string", "Registered prop material", false));
    addEntityParams.add("addShadow", createParam("boolean", "Add shadow to prop", false));
    addEntityParams.add("indestructible", createParam("boolean", "Indestructible prop state", false));
    addEntityParams.add("collision", createParam("boolean", "Enable collision", false));
    addEntityParams.add("scaleSprite", createParam("boolean", "Scale creature sprite", false));
    addEntityParams.add("velocity", createParam("number", "Creature movement velocity", false));
    addEntityParams.add("hitpoints", createParam("number", "Creature maximum hitpoints", false));
    addEntityParams.add("team", createParam("number", "Creature team number", false));
    addEntityParams.add("message", createParam("string", "Trigger message", false));
    addEntityParams.add("activation", createParam("string", "Trigger activation mode", false));
    addEntityParams.add("targets", createParam("string", "Trigger targets", false));
    addEntityParams.add("cooldown", createParam("number", "Cooldown in milliseconds", false));
    addEntityParams.add("oneTime", createParam("boolean", "One-time trigger", false));
    addEntityParams.add("spawnType", createParam("string", "Spawn information", false));
    addEntityParams.add("direction", createParam("string", "Spawn direction", false));
    addEntityParams.add("soundName", createParam("string", "Existing project sound resource", false));
    addEntityParams.add("volume", createParam("number", "Sound volume (0.0 to 1.0)", false));
    addEntityParams.add("loop", createParam("boolean", "Loop sound playback", false));
    addEntityParams.add("range", createParam("number", "Sound range in pixels", false));
    addEntityParams.add("lightColor", createParam("string", "Light color hex string", false));
    addEntityParams.add("lightIntensity", createParam("number", "Light intensity (0-255)", false));
    addEntityParams.add("lightShape", createParam("string", "Light shape (ELLIPSE, RECTANGLE)", false));
    addEntityParams.add("lightActive", createParam("boolean", "Light active state", false));
    addEntityParams.add("collisionType", createParam("string", "Collision type (STATIC, DYNAMIC)", false));
    addEntityParams.add("shadowType", createParam("string", "Static shadow type", false));
    addEntityParams.add("emitterData", createParam("string", "Emitter color or particle data", false));
    addEntityParams.add("itemType", createParam("string", "Item spawn type", false));
    addEntityParams.add(
        "properties",
        Json.createObjectBuilder()
            .add("type", "object")
            .add("description", "Additional scalar TMX properties")
            .add("additionalProperties", true)
            .add("required", false)
            .build());
    toolsArr.add(createToolDef("add-entity", "Create and place a generic entity on the current map", addEntityParams.build()));

    JsonObjectBuilder batchAddParams = Json.createObjectBuilder();
    batchAddParams.add("entities", createParam("array", "Array of entity objects to place in a single transaction", true));
    toolsArr.add(createToolDef("batch-add-entities", "Batch-place multiple entities onto the current map", batchAddParams.build()));

    JsonObjectBuilder batchEditParams = Json.createObjectBuilder();
    batchEditParams.add("operations", createParam("array", "Array of batch operation objects: action ('create'/'update'/'delete'), target entity, and properties", true));
    toolsArr.add(createToolDef("batch-edit-entities", "Execute atomic batch create/update/delete operations with rollback", batchEditParams.build()));

    JsonObjectBuilder moveEntityParams = Json.createObjectBuilder();
    moveEntityParams.add("id", createParam("number", "Target entity ID", false));
    moveEntityParams.add("name", createParam("string", "Target entity name", false));
    moveEntityParams.add("x", createParam("number", "Absolute X coordinate", false));
    moveEntityParams.add("y", createParam("number", "Absolute Y coordinate", false));
    moveEntityParams.add("dx", createParam("number", "Relative X offset", false));
    moveEntityParams.add("dy", createParam("number", "Relative Y offset", false));
    toolsArr.add(createToolDef("move-entity", "Move entity to absolute coordinates or by relative delta offsets", moveEntityParams.build()));

    JsonObjectBuilder resizeEntityParams = Json.createObjectBuilder();
    resizeEntityParams.add("id", createParam("number", "Target entity ID", false));
    resizeEntityParams.add("name", createParam("string", "Target entity name", false));
    resizeEntityParams.add("width", createParam("number", "New width in pixels", true));
    resizeEntityParams.add("height", createParam("number", "New height in pixels", true));
    toolsArr.add(createToolDef("resize-entity", "Resize entity width and height dimensions", resizeEntityParams.build()));

    JsonObjectBuilder setEntityLayerParams = Json.createObjectBuilder();
    setEntityLayerParams.add("id", createParam("number", "Target entity ID", false));
    setEntityLayerParams.add("name", createParam("string", "Target entity name", false));
    setEntityLayerParams.add("layer", createParam("string", "Target layer name", true));
    toolsArr.add(createToolDef("set-entity-layer", "Reassign an entity to a target object layer", setEntityLayerParams.build()));

    JsonObjectBuilder removeEntityParams = Json.createObjectBuilder();
    removeEntityParams.add("id", createParam("number", "Target entity ID", false));
    removeEntityParams.add("name", createParam("string", "Target entity name", false));
    toolsArr.add(createToolDef("remove-entity", "Remove an entity from the current map by ID or name", removeEntityParams.build()));

    JsonObjectBuilder getEntityInfoParams = Json.createObjectBuilder();
    getEntityInfoParams.add("id", createParam("number", "Target entity ID", false));
    getEntityInfoParams.add("name", createParam("string", "Target entity name", false));
    toolsArr.add(createToolDef("get-entity-info", "Get detailed info and custom properties of an entity", getEntityInfoParams.build()));

    JsonObjectBuilder setEntityPropParams = Json.createObjectBuilder();
    setEntityPropParams.add("id", createParam("number", "Target entity ID", false));
    setEntityPropParams.add("name", createParam("string", "Target entity name", false));
    setEntityPropParams.add("property", createParam("string", "Property key name", true));
    setEntityPropParams.add("value", createParam("string", "Property value string", true));
    toolsArr.add(createToolDef("set-entity-property", "Set any custom or built-in property on an entity", setEntityPropParams.build()));

    JsonObjectBuilder copyEntityParams = Json.createObjectBuilder();
    copyEntityParams.add("id", createParam("number", "Target entity ID to copy", false));
    copyEntityParams.add("name", createParam("string", "Target entity name to copy", false));
    toolsArr.add(createToolDef("copy-entity", "Copy an entity to editor clipboard", copyEntityParams.build()));

    toolsArr.add(createToolDef("paste-entity", "Paste copied entity blueprint onto active map", null));
    toolsArr.add(createToolDef("select-all-entities", "Select all entities on active map", null));
    toolsArr.add(createToolDef("deselect-entities", "Deselect all selected entities on map", null));

    // ---- Entity Component Configuration ----
    JsonObjectBuilder configCreatureParams = Json.createObjectBuilder();
    configCreatureParams.add("id", createParam("number", "Creature entity ID", false));
    configCreatureParams.add("name", createParam("string", "Creature entity name", false));
    configCreatureParams.add("spritesheetName", createParam("string", "Spritesheet name", false));
    configCreatureParams.add("scaling", createParam("boolean", "Scale sprite to bounding box", false));
    toolsArr.add(createToolDef("configure-creature", "Configure Creature entity attributes", configCreatureParams.build()));

    JsonObjectBuilder configPropParams = Json.createObjectBuilder();
    configPropParams.add("id", createParam("number", "Prop entity ID", false));
    configPropParams.add("name", createParam("string", "Prop entity name", false));
    configPropParams.add("spritesheetName", createParam("string", "Spritesheet name", false));
    configPropParams.add("material", createParam("string", "Prop material (valid: UNDEFINED, CERAMIC, FLESH, FOLIAGE, PLASTIC, STEEL, STONE, WOOD)", false));
    configPropParams.add("addShadow", createParam("boolean", "Add shadow to prop", false));
    toolsArr.add(createToolDef("configure-prop", "Configure Prop entity attributes", configPropParams.build()));

    JsonObjectBuilder configTriggerParams = Json.createObjectBuilder();
    configTriggerParams.add("id", createParam("number", "Trigger entity ID", false));
    configTriggerParams.add("name", createParam("string", "Trigger entity name", false));
    configTriggerParams.add("message", createParam("string", "Trigger message string", false));
    configTriggerParams.add("activation", createParam("string", "Activation mode (INTERACT, COLLISION)", false));
    configTriggerParams.add("targets", createParam("string", "Comma-separated target entity IDs (e.g. '101,102') or target entity names", false));
    configTriggerParams.add("cooldown", createParam("number", "Cooldown in ms", false));
    configTriggerParams.add("oneTime", createParam("boolean", "Fire trigger one-time only", false));
    toolsArr.add(createToolDef("configure-trigger", "Configure Trigger entity attributes", configTriggerParams.build()));

    JsonObjectBuilder configLightParams = Json.createObjectBuilder();
    configLightParams.add("id", createParam("number", "Light entity ID", false));
    configLightParams.add("name", createParam("string", "Light entity name", false));
    configLightParams.add("lightColor", createParam("string", "Light color hex", false));
    configLightParams.add("lightIntensity", createParam("number", "Light intensity (0-255)", false));
    configLightParams.add("lightShape", createParam("string", "Light shape (ELLIPSE, RECTANGLE)", false));
    configLightParams.add("lightActive", createParam("boolean", "Light active state", false));
    toolsArr.add(createToolDef("configure-light", "Configure LightSource entity attributes", configLightParams.build()));

    JsonObjectBuilder configSoundParams = Json.createObjectBuilder();
    configSoundParams.add("id", createParam("number", "Sound entity ID", false));
    configSoundParams.add("name", createParam("string", "Sound entity name", false));
    configSoundParams.add("soundName", createParam("string", "Sound resource name", false));
    configSoundParams.add("soundVolume", createParam("number", "Sound volume (0.0-1.0)", false));
    configSoundParams.add("soundLoop", createParam("boolean", "Loop sound playback", false));
    configSoundParams.add("soundRange", createParam("number", "Sound range in pixels", false));
    toolsArr.add(createToolDef("configure-sound-source", "Configure SoundSource entity attributes", configSoundParams.build()));

    JsonObjectBuilder configCollisionParams = Json.createObjectBuilder();
    configCollisionParams.add("id", createParam("number", "Entity ID", false));
    configCollisionParams.add("name", createParam("string", "Entity name", false));
    configCollisionParams.add("collision", createParam("boolean", "Collision enabled", false));
    configCollisionParams.add("collisionType", createParam("string", "Collision type (STATIC, DYNAMIC)", false));
    configCollisionParams.add("collisionboxWidth", createParam("number", "Collision box width", false));
    configCollisionParams.add("collisionboxHeight", createParam("number", "Collision box height", false));
    toolsArr.add(createToolDef("configure-collision", "Configure Collision attributes for an entity", configCollisionParams.build()));

    JsonObjectBuilder configCombatParams = Json.createObjectBuilder();
    configCombatParams.add("id", createParam("number", "Combat entity ID", false));
    configCombatParams.add("name", createParam("string", "Combat entity name", false));
    configCombatParams.add("hitpoints", createParam("number", "Max hitpoints", false));
    configCombatParams.add("team", createParam("number", "Team number", false));
    configCombatParams.add("indestructible", createParam("boolean", "Indestructible state", false));
    toolsArr.add(createToolDef("configure-combat", "Configure Combat attributes for an entity", configCombatParams.build()));

    JsonObjectBuilder configMovementParams = Json.createObjectBuilder();
    configMovementParams.add("id", createParam("number", "Mobile entity ID", false));
    configMovementParams.add("name", createParam("string", "Mobile entity name", false));
    configMovementParams.add("velocity", createParam("number", "Velocity (pixels/sec)", false));
    configMovementParams.add("acceleration", createParam("number", "Acceleration in ms", false));
    configMovementParams.add("deceleration", createParam("number", "Deceleration in ms", false));
    configMovementParams.add("turnOnMove", createParam("boolean", "Turn on movement", false));
    toolsArr.add(createToolDef("configure-movement", "Configure Movement attributes for an entity", configMovementParams.build()));

    // ---- Asset Import ----
    JsonObjectBuilder importSpriteParams = Json.createObjectBuilder();
    importSpriteParams.add("path", createParam("string", "Absolute path to image file or directory", true));
    toolsArr.add(createToolDef("import-spritesheet", "Import a spritesheet image asset into project", importSpriteParams.build()));

    JsonObjectBuilder importSoundParams = Json.createObjectBuilder();
    importSoundParams.add("path", createParam("string", "Absolute path to sound file or directory", true));
    toolsArr.add(createToolDef("import-sound", "Import a sound asset into project", importSoundParams.build()));

    // ---- View & Camera Commands ----
    JsonObjectBuilder zoomParams = Json.createObjectBuilder();
    zoomParams.add("zoom", createParam("number", "Zoom level float (e.g. 1.0 = 100%, 2.0 = 200%)", true));
    toolsArr.add(createToolDef("set-zoom", "Set editor camera zoom level", zoomParams.build()));

    JsonObjectBuilder centerCameraParams = Json.createObjectBuilder();
    centerCameraParams.add("target", createParam("string", "'map' to center on map, or 'focus' to center on selected entity", false));
    toolsArr.add(createToolDef("center-camera", "Center camera viewport on map or selected entity", centerCameraParams.build()));

    JsonObjectBuilder configViewParams = Json.createObjectBuilder();
    configViewParams.add("showGrid", createParam("boolean", "Show grid overlay", false));
    configViewParams.add("showCollision", createParam("boolean", "Show collision bounding boxes", false));
    configViewParams.add("showCustomObjects", createParam("boolean", "Show custom map objects", false));
    configViewParams.add("showMapIds", createParam("boolean", "Show map object IDs", false));
    configViewParams.add("showNames", createParam("boolean", "Show entity names", false));
    toolsArr.add(createToolDef("configure-view", "Configure editor viewport display options", configViewParams.build()));

    // ---- Layer Editing & Tile Editing Tools ----
    toolsArr.add(createToolDef("get-layers", "Get list of all layers on the active map", null));

    JsonObjectBuilder addLayerParams = Json.createObjectBuilder();
    addLayerParams.add("name", createParam("string", "Layer name", true));
    addLayerParams.add("type", createParam("string", "Layer type: 'tile' or 'object' or 'group'", true));
    addLayerParams.add("index", createParam("number", "Optional insertion index", false));
    toolsArr.add(createToolDef("add-layer", "Add a new tile, object, or group layer to the map", addLayerParams.build()));

    JsonObjectBuilder removeLayerParams = Json.createObjectBuilder();
    removeLayerParams.add("name", createParam("string", "Target layer name", true));
    toolsArr.add(createToolDef("remove-layer", "Remove a layer from the map by name", removeLayerParams.build()));

    JsonObjectBuilder configLayerParams = Json.createObjectBuilder();
    configLayerParams.add("name", createParam("string", "Target layer name", true));
    configLayerParams.add("newName", createParam("string", "Updated layer name", false));
    configLayerParams.add("visible", createParam("boolean", "Visibility state", false));
    configLayerParams.add("opacity", createParam("number", "Opacity float (0.0 to 1.0)", false));
    toolsArr.add(createToolDef("configure-layer", "Configure layer properties (name, visibility, opacity)", configLayerParams.build()));

    JsonObjectBuilder setTileParams = Json.createObjectBuilder();
    setTileParams.add("layer", createParam("string", "Target tile layer name", true));
    setTileParams.add("x", createParam("number", "Grid X coordinate", true));
    setTileParams.add("y", createParam("number", "Grid Y coordinate", true));
    setTileParams.add("gid", createParam("number", "Global tile ID (0 = clear, >0 = tileset tile GID)", true));
    toolsArr.add(createToolDef("set-tile", "Set a single tile GID at grid position (x, y) on a tile layer", setTileParams.build()));

    JsonObjectBuilder setTilesParams = Json.createObjectBuilder();
    setTilesParams.add("layer", createParam("string", "Target tile layer name", true));
    setTilesParams.add("x", createParam("integer", "Dense region starting grid X", false));
    setTilesParams.add("y", createParam("integer", "Dense region starting grid Y", false));
    setTilesParams.add("width", createParam("integer", "Dense region width in tiles", false));
    setTilesParams.add("height", createParam("integer", "Dense region height in tiles", false));
    setTilesParams.add(
        "gids",
        createArrayParam(
            "Dense row-major GIDs; may contain up to width*height values",
            false,
            Json.createObjectBuilder().add("type", "integer").add("minimum", 0).build()));
    JsonObject sparseTileSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("x", Json.createObjectBuilder().add("type", "integer"))
            .add("y", Json.createObjectBuilder().add("type", "integer"))
            .add("gid", Json.createObjectBuilder().add("type", "integer").add("minimum", 0)))
        .add("required", Json.createArrayBuilder().add("x").add("y").add("gid"))
        .build();
    JsonObject coordinateSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("x", Json.createObjectBuilder().add("type", "integer"))
            .add("y", Json.createObjectBuilder().add("type", "integer")))
        .add("required", Json.createArrayBuilder().add("x").add("y"))
        .build();
    JsonObject placementSchema = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false)
        .add("properties", Json.createObjectBuilder()
            .add("gid", Json.createObjectBuilder().add("type", "integer").add("minimum", 0))
            .add(
                "cells",
                Json.createObjectBuilder()
                    .add("type", "array")
                    .add("items", coordinateSchema)))
        .add("required", Json.createArrayBuilder().add("gid").add("cells"))
        .build();
    setTilesParams.add(
        "tiles",
        createArrayParam(
            "Sparse tile edits with an explicit GID per coordinate", false, sparseTileSchema));
    setTilesParams.add(
        "placements",
        createArrayParam(
            "Grouped sparse edits: one GID and many coordinates per group",
            false,
            placementSchema));
    toolsArr.add(
        createToolDef(
            "set-tiles",
            "Apply many tile edits in one undoable call. Use gids with x/y/width/height for dense row-major regions, tiles for mixed sparse edits, or placements to reuse one GID across many disconnected cells.",
            setTilesParams.build()));

    JsonObjectBuilder fillTilesParams = Json.createObjectBuilder();
    fillTilesParams.add("layer", createParam("string", "Target tile layer name", false));
    fillTilesParams.add("x", createParam("number", "Starting grid X coordinate", false));
    fillTilesParams.add("y", createParam("number", "Starting grid Y coordinate", false));
    fillTilesParams.add("width", createParam("number", "Region width in tiles", false));
    fillTilesParams.add("height", createParam("number", "Region height in tiles", false));
    fillTilesParams.add("gid", createParam("number", "Tile GID to fill", false));
    fillTilesParams.add(
        "regions",
        Json.createObjectBuilder()
            .add("type", "array")
            .add("description", "Fill multiple rectangular regions across tile layers in ONE call")
            .add("items", Json.createObjectBuilder()
                .add("type", "object")
                .add("properties", Json.createObjectBuilder()
                    .add("layer", Json.createObjectBuilder().add("type", "string"))
                    .add("x", Json.createObjectBuilder().add("type", "integer"))
                    .add("y", Json.createObjectBuilder().add("type", "integer"))
                    .add("width", Json.createObjectBuilder().add("type", "integer"))
                    .add("height", Json.createObjectBuilder().add("type", "integer"))
                    .add("gid", Json.createObjectBuilder().add("type", "integer")))
                .add("required", Json.createArrayBuilder().add("x").add("y").add("width").add("height").add("gid")))
            .add("required", false));
    toolsArr.add(createToolDef("fill-tiles", "Fill single or multiple rectangular regions across tile layers in ONE call. PREFER passing 'regions' array or using 'set-tiles' when filling multiple regions.", fillTilesParams.build()));

    JsonObjectBuilder getTileInfoParams = Json.createObjectBuilder();
    getTileInfoParams.add("layer", createParam("string", "Target tile layer name", false));
    getTileInfoParams.add("x", createParam("number", "Grid X coordinate", true));
    getTileInfoParams.add("y", createParam("number", "Grid Y coordinate", true));
    toolsArr.add(createToolDef("get-tile-info", "Get tile GID info at specified grid position (x, y)", getTileInfoParams.build()));

    JsonObject tileInfoQuerySchema =
        Json.createObjectBuilder()
            .add("type", "object")
            .add("additionalProperties", false)
            .add(
                "properties",
                Json.createObjectBuilder()
                    .add("layer", Json.createObjectBuilder().add("type", "string"))
                    .add("x", Json.createObjectBuilder().add("type", "integer"))
                    .add("y", Json.createObjectBuilder().add("type", "integer")))
            .add(
                "required",
                Json.createArrayBuilder().add("layer").add("x").add("y"))
            .build();
    JsonObjectBuilder getTilesInfoParams = Json.createObjectBuilder();
    getTilesInfoParams.add(
        "queries",
        Json.createObjectBuilder()
            .add("type", "array")
            .add(
                "description",
                "Tile samples to inspect in request order; each query identifies one layer and grid coordinate")
            .add("items", tileInfoQuerySchema)
            .add("minItems", 1)
            .add("maxItems", 512)
            .add("required", true));
    toolsArr.add(
        createToolDef(
            "get-tiles-info",
            "Inspect up to 512 tile layer/coordinate samples in one read-only call. Results preserve request order and report per-query errors.",
            getTilesInfoParams.build()));

    McpAssetHandler.addToolDefinitions(toolsArr);
    McpAnimationHandler.addToolDefinitions(toolsArr);
    McpTilesetHandler.addToolDefinitions(toolsArr);

    return Json.createObjectBuilder().add("tools", toolsArr).build();
  }

  static boolean isKnownTool(String name) {
    if (name == null) {
      return false;
    }
    return getToolsList().getJsonArray("tools").stream()
        .map(JsonValue::asJsonObject)
        .anyMatch(tool -> name.equals(tool.getString("name")));
  }

  private static JsonObjectBuilder createBaseAddParams() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("name", createParam("string", "Entity name", false));
    builder.add("x", createParam("number", "X position in pixels", true));
    builder.add("y", createParam("number", "Y position in pixels", true));
    builder.add("width", createParam("number", "Width in pixels", false));
    builder.add("height", createParam("number", "Height in pixels", false));
    builder.add("layer", createParam("string", "Target layer name", false));
    return builder;
  }

  static JsonObject createToolDef(String name, String description, JsonObject params) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("name", name);
    builder.add("description", description);
    JsonObjectBuilder schemaBuilder = Json.createObjectBuilder()
        .add("type", "object")
        .add("additionalProperties", false);
    JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder();
    JsonArrayBuilder requiredArr = Json.createArrayBuilder();
    if (params != null) {
      for (Map.Entry<String, JsonValue> entry : params.entrySet()) {
        if (entry.getValue() instanceof JsonObject paramObj) {
          if (paramObj.getBoolean("required", false)) {
            requiredArr.add(entry.getKey());
          }
          JsonObjectBuilder propertyBuilder = Json.createObjectBuilder();
          for (Map.Entry<String, JsonValue> property : paramObj.entrySet()) {
            if (!"required".equals(property.getKey())) {
              propertyBuilder.add(property.getKey(), property.getValue());
            }
          }
          propertiesBuilder.add(entry.getKey(), propertyBuilder);
        } else {
          propertiesBuilder.add(entry.getKey(), entry.getValue());
        }
      }
    }
    schemaBuilder.add("properties", propertiesBuilder);
    schemaBuilder.add("required", requiredArr);
    builder.add("inputSchema", schemaBuilder.build());
    builder.add("outputSchema", Json.createObjectBuilder()
        .add("type", "object")
        .add("properties", Json.createObjectBuilder()
            .add("success", Json.createObjectBuilder().add("type", "boolean"))
            .add("message", Json.createObjectBuilder().add("type", "string"))
            .add("error", Json.createObjectBuilder().add("type", "string")))
        .add("additionalProperties", true));
    builder.add("annotations", McpToolGroups.annotationsFor(name));
    return builder.build();
  }

  static JsonObject createParam(String type, String description, boolean required) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("type", type);
    builder.add("description", description);
    builder.add("required", required);
    return builder.build();
  }

  static JsonObject createArrayParam(String description, boolean required, JsonObject items) {
    return Json.createObjectBuilder()
        .add("type", "array")
        .add("description", description)
        .add("items", items)
        .add("required", required)
        .build();
  }


  public static JsonObject handleCallTool(String toolName, JsonObject arguments) {
    AtomicReference<JsonObject> resultRef = new AtomicReference<>();
    Runnable task = () -> {
      Thread thread = Thread.currentThread();
      ClassLoader previousClassLoader = thread.getContextClassLoader();
      ClassLoader appClassLoader = McpToolHandler.class.getClassLoader();
      if (appClassLoader == null) {
        appClassLoader = ClassLoader.getSystemClassLoader();
      }
      try {
        thread.setContextClassLoader(appClassLoader);
        resultRef.set(executeTool(toolName, arguments));
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Error executing MCP tool '" + toolName + "'.", e);
        resultRef.set(Json.createObjectBuilder()
            .add("success", false)
            .add("error", "Error executing tool '" + toolName + "': " + exceptionMessage(e))
            .build());
      } finally {
        thread.setContextClassLoader(previousClassLoader);
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(task);
      } catch (Exception e) {
        log.log(Level.SEVERE, "Swing dispatch failed for MCP tool '" + toolName + "'.", e);
        return Json.createObjectBuilder()
            .add("success", false)
            .add("error", "Swing dispatch error: " + exceptionMessage(e))
            .build();
      }
    }

    return resultRef.get();
  }

  private static JsonObject executeTool(String name, JsonObject args) {
    if (McpAssetHandler.handles(name)) {
      return McpAssetHandler.handle(name, args);
    }
    if (McpAnimationHandler.handles(name)) {
      return McpAnimationHandler.handle(name, args);
    }
    if (McpTilesetHandler.handles(name)) {
      return McpTilesetHandler.handle(name, args);
    }

    switch (name) {
      case "save-project":
        Editor.instance().save(false);
        return Json.createObjectBuilder().add("success", true).add("message", "Project saved").build();

      case "load-project":
        String loadPath = getString(args, "path", null);
        if (loadPath != null) {
          Editor.instance().load(Path.of(loadPath), true);
          return Json.createObjectBuilder().add("success", true).add("message", "Loaded project: " + loadPath).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Missing 'path' argument").build();

      case "undo":
        if (UndoManager.instance().canUndo()) {
          UndoManager.instance().undo();
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Undo successful").build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Nothing to undo").build();

      case "redo":
        if (UndoManager.instance().canRedo()) {
          UndoManager.instance().redo();
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Redo successful").build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Nothing to redo").build();

      case "select-map":
        String selectMapName = getString(args, "name", null);
        if (selectMapName != null) {
          if (Editor.instance().getGameFile() != null) {
            for (IMap map : Editor.instance().getGameFile().getMaps()) {
              if (map instanceof TmxMap tmxMap && selectMapName.equalsIgnoreCase(tmxMap.getName())) {
                Editor.instance().getMapComponent().loadEnvironment(tmxMap);
                refreshInspectorUI(null);
                return Json.createObjectBuilder().add("success", true).add("message", "Switched active map to: " + selectMapName).build();
              }
            }
          }
          return Json.createObjectBuilder().add("success", false).add("error", "Map not found: " + selectMapName).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Missing 'name' argument").build();

      case "create-map":
        return createMap(args);

      case "delete-map":
        String deleteMapName = getString(args, "name", null);
        if (deleteMapName == null) {
          return Json.createObjectBuilder().add("success", false).add("error", "Missing 'name' argument").build();
        }
        TmxMap mapToDelete = null;
        for (TmxMap map : Editor.instance().getMapComponent().getMaps()) {
          if (deleteMapName.equalsIgnoreCase(map.getName())) {
            mapToDelete = map;
            break;
          }
        }
        if (mapToDelete == null) {
          return Json.createObjectBuilder().add("success", false).add("error", "Map not found: " + deleteMapName).build();
        }
        if (!Editor.instance().getMapComponent().deleteMap(mapToDelete)) {
          return Json.createObjectBuilder().add("success", false).add("error", "Map deletion was cancelled").build();
        }
        refreshInspectorUI(null);
        return Json.createObjectBuilder().add("success", true).add("message", "Deleted map: " + deleteMapName).build();

      case "reassign-map-ids":
        TmxMap activeMap = null;
        if (Game.world().environment() != null && Game.world().environment().getMap() instanceof TmxMap tmx) {
          activeMap = tmx;
        } else if (!Editor.instance().getMapComponent().getMaps().isEmpty()) {
          activeMap = Editor.instance().getMapComponent().getMaps().get(0);
        }
        if (activeMap != null) {
          return reassignMapIds(args, activeMap);
        }
        return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();

      case "export-map-snapshot":
        return exportMapSnapshot();

      case "get-canvas-snapshot":
        return getCanvasSnapshot();

      case "open-snapshot-folder":
        return openSnapshotFolder();

      case "validate-map":
        return validateMap();

      case "get-logs":
        return getLogs(args);

      case "get-property-docs":
        return getPropertyDocs(args);

      case "query-geometry":
        return queryGeometry(args);

      case "search-assets":
        return searchAssets(args);

      case "list-entities":
        return listEntities(args);

      case "move-entity":
        return moveEntity(args);

      case "resize-entity":
        return resizeEntity(args);

      case "set-entity-layer":
        return setEntityLayer(args);

      case "copy-entity":
        if (args != null) {
          IMapObject target = findEntity(args);
          if (target != null) {
            refreshInspectorUI(target);
          }
        }
        Editor.instance().getMapComponent().copy();
        return Json.createObjectBuilder().add("success", true).add("message", "Copied entity to clipboard").build();

      case "paste-entity":
        Editor.instance().getMapComponent().paste();
        refreshInspectorUI(Editor.instance().getMapComponent().getFocusedMapObject());
        return Json.createObjectBuilder().add("success", true).add("message", "Pasted entity blueprint onto map").build();

      case "select-all-entities":
        Editor.instance().getMapComponent().selectAll();
        refreshInspectorUI(Editor.instance().getMapComponent().getFocusedMapObject());
        return Json.createObjectBuilder().add("success", true).add("message", "Selected all entities").build();

      case "deselect-entities":
        Editor.instance().getMapComponent().deselect();
        refreshInspectorUI(null);
        return Json.createObjectBuilder().add("success", true).add("message", "Deselected all entities").build();

      // NATIVE MAPOBJECT ADD HANDLERS
      case "add-prop":
        return addProp(args);
      case "add-creature":
        return addCreature(args);
      case "add-collisionbox":
        return addCollisionbox(args);
      case "add-trigger":
        return addTrigger(args);
      case "add-spawnpoint":
        return addSpawnpoint(args);
      case "add-area":
        return addArea(args);
      case "add-light":
        return addLight(args);
      case "add-static-shadow":
        return addStaticShadow(args);
      case "add-emitter":
        return addEmitter(args);
      case "add-sound-source":
        return addSoundSource(args);
      case "add-item-spawn":
        return addItemSpawn(args);

      case "batch-add-entities":
        return batchAddEntities(args);

      case "batch-edit-entities":
        return batchEditEntities(args);

      case "fill-tiles":
        return fillTiles(args);

      case "set-zoom":
        Float zoomVal = getFloat(args, "zoom", null);
        if (zoomVal != null) {
          Zoom.set(zoomVal);
          return Json.createObjectBuilder().add("success", true).add("message", "Zoom set to " + zoomVal).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Missing 'zoom' argument").build();

      case "center-camera":
        String targetStr = getString(args, "target", "map").toLowerCase();
        if ("focus".equals(targetStr)) {
          Editor.instance().getMapComponent().centerCameraOnFocus();
        } else {
          Editor.instance().getMapComponent().centerCameraOnMap();
        }
        return Json.createObjectBuilder().add("success", true).add("message", "Centered camera on " + targetStr).build();

      case "configure-view":
        if (args != null) {
          if (args.containsKey("showGrid")) {
            Editor.preferences().setShowGrid(getBoolean(args, "showGrid", true));
          }
          if (args.containsKey("showCollision")) {
            Editor.preferences().setRenderBoundingBoxes(getBoolean(args, "showCollision", true));
          }
          if (args.containsKey("showCustomObjects")) {
            Editor.preferences().setRenderCustomMapObjects(getBoolean(args, "showCustomObjects", true));
          }
          if (args.containsKey("showMapIds")) {
            Editor.preferences().setRenderMapIds(getBoolean(args, "showMapIds", true));
          }
          if (args.containsKey("showNames")) {
            Editor.preferences().setRenderNames(getBoolean(args, "showNames", true));
          }
        }
        return Json.createObjectBuilder().add("success", true).add("message", "Updated viewport display settings").build();

      case "set-gravity":
        if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
          IMap map = Game.world().environment().getMap();
          Integer gravity = getInt(args, "gravity", null);
          if (gravity == null) {
            return Json.createObjectBuilder().add("success", false).add("error", "Missing 'gravity' argument").build();
          }
          UndoManager.forMap(map).mapChanging(map);
          map.setValue(MapProperty.GRAVITY, gravity);
          Game.world().environment().setGravity(gravity);
          UndoManager.forMap(map).mapChanged(map);
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Gravity set to " + gravity).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();

      case "set-ambient-light":
        if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
          IMap map = Game.world().environment().getMap();
          String lightColor = getString(args, "color", null);
          Integer alpha = getInt(args, "alpha", null);
          Color color = ColorHelper.decode(lightColor);
          if (color == null) {
            return Json.createObjectBuilder().add("success", false).add("error", "Invalid or missing 'color' argument").build();
          }
          if (alpha != null) {
            color = ColorHelper.getTransparentVariant(color, alpha);
          }
          UndoManager.forMap(map).mapChanging(map);
          map.setValue(MapProperty.AMBIENTCOLOR, ColorHelper.encode(color));
          Game.world().environment().getAmbientLight().setColor(color);
          UndoManager.forMap(map).mapChanged(map);
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Ambient light updated").build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();

      case "add-entity":
        return addEntity(args);

      case "remove-entity":
        return removeEntity(args);

      case "get-entity-info":
        return getEntityInfo(args);

      case "set-entity-property":
        return setEntityProperty(args);

      case "configure-creature":
        return setProperties(args, MapObjectProperty.SPRITESHEETNAME, MapObjectProperty.SCALE_SPRITE);

      case "configure-prop":
        return setProperties(args, MapObjectProperty.SPRITESHEETNAME, MapObjectProperty.PROP_MATERIAL, MapObjectProperty.PROP_ADDSHADOW);

      case "configure-trigger":
        return setProperties(args,
            Map.entry("message", MapObjectProperty.TRIGGER_MESSAGE),
            Map.entry("activation", MapObjectProperty.TRIGGER_ACTIVATION),
            Map.entry("targets", MapObjectProperty.TRIGGER_TARGETS),
            Map.entry("cooldown", MapObjectProperty.TRIGGER_COOLDOWN),
            Map.entry("oneTime", MapObjectProperty.TRIGGER_ONETIME));

      case "configure-light":
        return setProperties(args, MapObjectProperty.LIGHT_COLOR, MapObjectProperty.LIGHT_INTENSITY, MapObjectProperty.LIGHT_SHAPE, MapObjectProperty.LIGHT_ACTIVE);

      case "configure-sound-source":
        return setProperties(args, MapObjectProperty.SOUND_NAME, MapObjectProperty.SOUND_VOLUME, MapObjectProperty.SOUND_LOOP, MapObjectProperty.SOUND_RANGE);

      case "configure-collision":
        return setProperties(args, MapObjectProperty.COLLISION, MapObjectProperty.COLLISION_TYPE, MapObjectProperty.COLLISIONBOX_WIDTH, MapObjectProperty.COLLISIONBOX_HEIGHT);

      case "configure-combat":
        return setProperties(args, MapObjectProperty.COMBAT_HITPOINTS, MapObjectProperty.COMBAT_TEAM, MapObjectProperty.COMBAT_INDESTRUCTIBLE);

      case "configure-movement":
        return setProperties(args, MapObjectProperty.MOVEMENT_VELOCITY, MapObjectProperty.MOVEMENT_ACCELERATION, MapObjectProperty.MOVEMENT_DECELERATION, MapObjectProperty.MOVEMENT_TURNONMOVE);

      case "import-spritesheet":
        String spritePath = getString(args, "path", null);
        if (spritePath != null) {
          Editor.instance().importSpriteSheets(Path.of(spritePath));
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Imported spritesheet: " + spritePath).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Missing 'path' argument").build();

      case "import-sound":
        String soundPath = getString(args, "path", null);
        if (soundPath != null) {
          Editor.instance().importSounds(Path.of(soundPath));
          refreshInspectorUI(null);
          return Json.createObjectBuilder().add("success", true).add("message", "Imported sound: " + soundPath).build();
        }
        return Json.createObjectBuilder().add("success", false).add("error", "Missing 'path' argument").build();

      case "get-layers":
        return getLayers();

      case "add-layer":
        return addLayer(args);

      case "remove-layer":
        return removeLayer(args);

      case "configure-layer":
        return configureLayer(args);

      case "set-tile":
        return setTile(args);

      case "set-tiles":
        return setTiles(args);

      case "get-tile-info":
        return getTileInfo(args);

      case "get-tiles-info":
        return getTilesInfo(args);

      default:
        return Json.createObjectBuilder().add("success", false).add("error", "Unknown tool: " + name).build();
    }
  }

  private static JsonObject createMap(JsonObject args) {
    String requestedName = getString(args, "name", "map");
    String name = requestedName == null ? "" : requestedName.trim();
    if (name.isEmpty()) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Map name must not be blank")
          .build();
    }

    int width = getInt(args, "width", 30);
    int height = getInt(args, "height", 20);
    int tileWidth = getInt(args, "tileWidth", 32);
    int tileHeight = getInt(args, "tileHeight", 32);
    if (width < 1 || width > 9999 || height < 1 || height > 9999) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Map width and height must be between 1 and 9999 tiles")
          .build();
    }
    if (tileWidth < 1 || tileWidth > 256 || tileHeight < 1 || tileHeight > 256) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Tile width and height must be between 1 and 256 pixels")
          .build();
    }

    String orientationName =
        getString(args, "orientation", MapOrientations.ORTHOGONAL.getName());
    IMapOrientation orientation = parseMapOrientation(orientationName);
    if (orientation == null) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add(
              "error",
              "Unknown map orientation: "
                  + orientationName
                  + ". Expected orthogonal, isometric, staggered, or hexagonal")
          .build();
    }
    if (orientation != MapOrientations.ORTHOGONAL
        && (tileWidth % 2 != 0 || tileHeight % 2 != 0)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add(
              "error",
              "Isometric, staggered, and hexagonal maps require even tile dimensions")
          .build();
    }

    StaggerAxis staggerAxis;
    StaggerIndex staggerIndex;
    try {
      staggerAxis =
          StaggerAxis.valueOf(
              getString(args, "staggerAxis", StaggerAxis.Y.name())
                  .toUpperCase(java.util.Locale.ROOT));
      staggerIndex =
          StaggerIndex.valueOf(
              getString(args, "staggerIndex", StaggerIndex.ODD.name())
                  .toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "staggerAxis must be X or Y and staggerIndex must be ODD or EVEN")
          .build();
    }

    int hexSideLength = getInt(args, "hexSideLength", 0);
    if (hexSideLength < 0 || hexSideLength > 256 || hexSideLength % 2 != 0) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "hexSideLength must be an even number between 0 and 256")
          .build();
    }

    boolean overwrite = getBoolean(args, "overwrite", false) || getBoolean(args, "replace", false);
    MapComponent mapComponent = Editor.instance().getMapComponent();
    TmxMap mapToDelete = null;
    for (TmxMap existing : mapComponent.getMaps()) {
      if (existing.getName() != null && existing.getName().equalsIgnoreCase(name)) {
        mapToDelete = existing;
        break;
      }
    }
    if (mapToDelete != null) {
      if (overwrite) {
        mapComponent.deleteMap(mapToDelete);
      } else {
        return Json.createObjectBuilder()
            .add("success", false)
            .add("error", "A map named '" + name + "' already exists. Set 'overwrite': true to replace it in ONE call.")
            .build();
      }
    }

    List<Tileset> projectTilesets = resolveProjectTilesets(args);
    if (projectTilesets == null) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add(
              "error",
              "Every 'tilesets' entry must be the unique name of an existing project tileset")
          .build();
    }

    TmxMap map =
        mapComponent.createMap(
            name,
            orientation,
            width,
            height,
            tileWidth,
            tileHeight,
            staggerAxis,
            staggerIndex,
            hexSideLength,
            projectTilesets);
    if (map == null) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Could not create map")
          .build();
    }

    JsonArray initialLayers = args.getJsonArray("initialLayers");
    if (initialLayers == null) {
      initialLayers = args.getJsonArray("layers");
    }
    if (initialLayers != null) {
      for (int i = 0; i < initialLayers.size(); i++) {
        if (initialLayers.get(i) instanceof JsonObject layerObj) {
          String layerName = getString(layerObj, "name", "Layer " + (i + 1));
          String layerType = getString(layerObj, "type", "tile").toLowerCase();
          ILayer layer;
          if ("tile".equals(layerType)) {
            layer = new TileLayer(map.getWidth(), map.getHeight());
          } else if ("object".equals(layerType)) {
            layer = new MapObjectLayer();
          } else if ("group".equals(layerType)) {
            layer = new GroupLayer();
          } else {
            continue;
          }
          layer.setName(layerName);
          map.addLayer(layer);
        }
      }
    }

    String refreshWarning = refreshInspectorAfterMutation(null);
    JsonArrayBuilder attachedTilesets = Json.createArrayBuilder();
    for (ITileset tileset : map.getTilesets()) {
      attachedTilesets.add(
          Json.createObjectBuilder()
              .add("name", tileset.getName())
              .add("firstGridId", tileset.getFirstGridId())
              .add("tileCount", tileset.getTileCount()));
    }
    JsonObjectBuilder result =
        Json.createObjectBuilder()
            .add("success", true)
            .add("name", map.getName())
            .add("orientation", map.getOrientation().getName())
            .add("width", map.getWidth())
            .add("height", map.getHeight())
            .add("tileWidth", map.getTileWidth())
            .add("tileHeight", map.getTileHeight())
            .add("tilesets", attachedTilesets)
            .add("message", "Created and selected map: " + map.getName());
    if (refreshWarning != null) {
      result.add("warnings", Json.createArrayBuilder().add(refreshWarning));
    }
    return result.build();
  }

  private static List<Tileset> resolveProjectTilesets(JsonObject args) {
    if (!args.containsKey("tilesets") || args.isNull("tilesets")) {
      return List.of();
    }
    JsonValue value = args.get("tilesets");
    if (value.getValueType() != JsonValue.ValueType.ARRAY) {
      return null;
    }

    List<Tileset> available = Editor.instance().getGameFile().getTilesets();
    List<Tileset> resolved = new ArrayList<>();
    Set<String> names = new HashSet<>();
    for (JsonValue item : value.asJsonArray()) {
      if (!(item instanceof JsonString jsonName)) {
        return null;
      }
      String requestedName = jsonName.getString().trim();
      String normalizedName = requestedName.toLowerCase(java.util.Locale.ROOT);
      if (requestedName.isEmpty() || !names.add(normalizedName)) {
        return null;
      }
      Tileset tileset =
          available.stream()
              .filter(
                  candidate ->
                      candidate.getName() != null
                          && candidate.getName().equalsIgnoreCase(requestedName))
              .findFirst()
              .orElse(null);
      if (tileset == null) {
        return null;
      }
      resolved.add(tileset);
    }
    return resolved;
  }

  private static IMapOrientation parseMapOrientation(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
    if ("isometric_staggered".equals(normalized)
        || "isometric-staggered".equals(normalized)) {
      normalized = "staggered";
    }
    return MapOrientations.forName(normalized);
  }

  static void refreshInspectorUI(IMapObject target) {
    var mapComponent = Editor.instance().getMapComponent();
    if (target != null
        && mapComponent != null
        && UI.getInspector() != null
        && UI.getEntityController() != null) {
      mapComponent.setFocus(target, true);
    }
    if (mapComponent != null && UI.getInspector() != null) {
      mapComponent.refreshInspector();
      UI.getInspector().refresh();
    }
    if (UI.getLayerController() != null) {
      UI.getLayerController().refresh();
    }
    if (UI.getEntityController() != null) {
      UI.getEntityController().refresh();
    }
  }

  private static JsonObject reassignMapIds(JsonObject args, TmxMap map) {
    int minId = getInt(args, "minId", 1);
    if (minId < 1) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "'minId' must be greater than zero")
          .build();
    }

    final List<ProjectReferenceSpec> projectReferences;
    try {
      projectReferences = parseProjectReferenceSpecs(args, map.getName());
    } catch (IllegalArgumentException ex) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", ex.getMessage())
          .build();
    }

    MapComponent.IdReassignmentResult reassignment =
        Editor.instance().getMapComponent().reassignIds(map, minId);
    List<String> warnings = new ArrayList<>(reassignment.warnings());
    int updatedReferences = reassignment.updatedReferences();

    if (projectReferences.isEmpty()) {
      warnings.add(
          "Custom and cross-map reference properties were not updated; pass projectReferences to declare them.");
    } else {
      try {
        ProjectReferenceUpdateResult projectUpdate =
            updateProjectReferences(projectReferences, reassignment);
        updatedReferences += projectUpdate.updatedReferences();
        warnings.addAll(projectUpdate.warnings());
        warnings.add(
            "Only custom cross-map reference properties declared in projectReferences were updated.");
      } catch (RuntimeException ex) {
        log.log(
            Level.WARNING,
            "Project reference update failed after MCP map ID reassignment.",
            ex);
        warnings.add(
            "IDs were reassigned, but custom project references could not be fully updated: "
                + exceptionMessage(ex));
      }
    }

    String refreshWarning = refreshInspectorAfterMutation(null);
    if (refreshWarning != null) {
      warnings.add(refreshWarning);
    }

    JsonArrayBuilder idMapping = Json.createArrayBuilder();
    for (MapComponent.IdChange change : reassignment.changes()) {
      idMapping.add(
          Json.createObjectBuilder()
              .add("oldId", change.oldId())
              .add("newId", change.newId())
              .add("name", change.name() == null ? "" : change.name()));
    }

    JsonArrayBuilder ambiguousIds = Json.createArrayBuilder();
    reassignment.ambiguousOldIds().forEach(ambiguousIds::add);

    JsonArrayBuilder warningArray = Json.createArrayBuilder();
    new LinkedHashSet<>(warnings).forEach(warningArray::add);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("map", map.getName() == null ? "" : map.getName())
        .add("idMapping", idMapping)
        .add("ambiguousOldIds", ambiguousIds)
        .add("updatedReferences", updatedReferences)
        .add("warnings", warningArray)
        .add("message", "Reassigned map object IDs starting from " + minId)
        .build();
  }

  private static List<ProjectReferenceSpec> parseProjectReferenceSpecs(
      JsonObject args, String activeMapName) {
    if (args == null || !args.containsKey("projectReferences") || args.isNull("projectReferences")) {
      return List.of();
    }

    JsonArray declarations;
    try {
      declarations = args.getJsonArray("projectReferences");
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException("'projectReferences' must be an array", ex);
    }
    if (declarations == null) {
      throw new IllegalArgumentException("'projectReferences' must be an array");
    }

    List<ProjectReferenceSpec> result = new ArrayList<>(declarations.size());
    for (int index = 0; index < declarations.size(); index++) {
      JsonValue declaration = declarations.get(index);
      if (declaration.getValueType() != JsonValue.ValueType.OBJECT) {
        throw new IllegalArgumentException(
            "'projectReferences[" + index + "]' must be an object");
      }

      JsonObject object = declaration.asJsonObject();
      String idProperty = getString(object, "idProperty", null);
      String targetMapProperty = getString(object, "targetMapProperty", null);
      String targetMapValue = getString(object, "targetMapValue", activeMapName);
      if (idProperty == null || idProperty.isBlank()) {
        throw new IllegalArgumentException(
            "'projectReferences[" + index + "].idProperty' is required");
      }
      if (targetMapProperty == null || targetMapProperty.isBlank()) {
        throw new IllegalArgumentException(
            "'projectReferences[" + index + "].targetMapProperty' is required");
      }
      if (targetMapValue == null || targetMapValue.isBlank()) {
        throw new IllegalArgumentException(
            "'projectReferences["
                + index
                + "].targetMapValue' is required when the active map has no name");
      }

      result.add(
          new ProjectReferenceSpec(idProperty, targetMapProperty, targetMapValue));
    }
    return List.copyOf(result);
  }

  private static ProjectReferenceUpdateResult updateProjectReferences(
      List<ProjectReferenceSpec> specifications,
      MapComponent.IdReassignmentResult reassignment) {
    int updatedReferences = 0;
    Set<String> warnings = new LinkedHashSet<>();
    List<TmxMap> maps = new ArrayList<>(Editor.instance().getMapComponent().getMaps());
    if (Game.world().environment() != null
        && Game.world().environment().getMap() instanceof TmxMap activeMap
        && !maps.contains(activeMap)) {
      maps.add(activeMap);
    }

    for (TmxMap sourceMap : maps) {
      UndoManager undoManager = null;
      try {
        for (IMapObject mapObject : sourceMap.getMapObjects()) {
          for (ProjectReferenceSpec specification : specifications) {
            ICustomProperty mapProperty =
                mapObject.getProperty(specification.targetMapProperty());
            if (mapProperty == null
                || !specification.targetMapValue().equalsIgnoreCase(mapProperty.getAsString())) {
              continue;
            }

            ICustomProperty idProperty = mapObject.getProperty(specification.idProperty());
            if (idProperty == null) {
              continue;
            }

            MapComponent.IdReferenceRemap remap =
                MapComponent.remapIdReferences(
                    idProperty.getAsString(),
                    reassignment.unambiguousMapping(),
                    reassignment.ambiguousOldIds());
            for (int ambiguousId : remap.ambiguousReferences()) {
              warnings.add(
                  "Custom reference "
                      + specification.idProperty()
                      + " on map "
                      + sourceMap.getName()
                      + " still points to duplicate old ID "
                      + ambiguousId
                      + ".");
            }
            if (remap.replacements() == 0) {
              continue;
            }

            if (undoManager == null) {
              undoManager = UndoManager.forMap(sourceMap);
              undoManager.beginOperation();
            }
            undoManager.mapObjectChanging(mapObject);
            idProperty.setValue(remap.value());
            undoManager.mapObjectChanged(mapObject);
            updatedReferences += remap.replacements();
          }
        }
      } finally {
        if (undoManager != null) {
          undoManager.endOperation();
        }
      }
    }

    return new ProjectReferenceUpdateResult(updatedReferences, List.copyOf(warnings));
  }

  static String refreshInspectorAfterMutation(IMapObject target) {
    return runPostMutationRefresh(() -> refreshInspectorUI(target));
  }

  static String runPostMutationRefresh(Runnable refresh) {
    try {
      refresh.run();
      return null;
    } catch (RuntimeException ex) {
      log.log(Level.WARNING, "Inspector refresh failed after MCP mutation.", ex);
      return "Mutation succeeded, but the inspector could not be refreshed: "
          + exceptionMessage(ex);
    }
  }

  private static String exceptionMessage(Throwable exception) {
    Throwable cause = exception;
    while (cause != null && (cause instanceof java.lang.reflect.InvocationTargetException || cause.getMessage() == null || cause.getMessage().isBlank())) {
      if (cause.getCause() != null && cause.getCause() != cause) {
        cause = cause.getCause();
      } else {
        break;
      }
    }
    if (cause == null) {
      return "Unknown exception";
    }
    String msg = cause.getMessage();
    return msg == null || msg.isBlank() ? cause.getClass().getSimpleName() : cause.getClass().getSimpleName() + ": " + msg;
  }

  private record ProjectReferenceSpec(
      String idProperty, String targetMapProperty, String targetMapValue) {}

  private record ProjectReferenceUpdateResult(
      int updatedReferences, List<String> warnings) {}

  // GEOMETRY QUERY TOOL IMPLEMENTATION
  private static JsonObject queryGeometry(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    IMap map = Game.world().environment().getMap();
    String mode = getString(args, "mode", "overlap").toLowerCase();

    switch (mode) {
      case "overlap": {
        Rectangle2D queryBounds;
        IMapObject target = findEntity(args);
        if (target != null) {
          queryBounds = target.getBoundingBox();
        } else {
          Float qx = getFloat(args, "x", 0f);
          Float qy = getFloat(args, "y", 0f);
          Float qw = getFloat(args, "width", 16f);
          Float qh = getFloat(args, "height", 16f);
          queryBounds = new Rectangle2D.Float(qx, qy, qw, qh);
        }

        JsonArrayBuilder matches = Json.createArrayBuilder();
        for (IMapObject obj : map.getMapObjects()) {
          if (obj == null || (target != null && obj.getId() == target.getId())) continue;
          if (queryBounds.intersects(obj.getBoundingBox())) {
            matches.add(Json.createObjectBuilder()
                .add("id", obj.getId())
                .add("name", obj.getName() != null ? obj.getName() : "")
                .add("type", obj.getType() != null ? obj.getType() : ""));
          }
        }
        return Json.createObjectBuilder().add("success", true).add("mode", mode).add("matches", matches).build();
      }

      case "containment": {
        Float qx = getFloat(args, "x", 0f);
        Float qy = getFloat(args, "y", 0f);
        Float qw = getFloat(args, "width", 16f);
        Float qh = getFloat(args, "height", 16f);
        Rectangle2D bounds = new Rectangle2D.Float(qx, qy, qw, qh);

        JsonArrayBuilder matches = Json.createArrayBuilder();
        for (IMapObject obj : map.getMapObjects()) {
          if (obj == null) continue;
          if (bounds.contains(obj.getBoundingBox())) {
            matches.add(Json.createObjectBuilder()
                .add("id", obj.getId())
                .add("name", obj.getName() != null ? obj.getName() : "")
                .add("type", obj.getType() != null ? obj.getType() : ""));
          }
        }
        return Json.createObjectBuilder().add("success", true).add("mode", mode).add("matches", matches).build();
      }

      case "distance": {
        Point2D p1 = null;
        Point2D p2 = null;

        IMapObject obj1 = findEntity(args);
        if (obj1 != null) {
          p1 = obj1.getLocation();
        } else {
          Float x1 = getFloat(args, "x", null);
          Float y1 = getFloat(args, "y", null);
          if (x1 != null && y1 != null) p1 = new Point2D.Float(x1, y1);
        }

        Float x2 = getFloat(args, "x2", null);
        Float y2 = getFloat(args, "y2", null);
        if (x2 != null && y2 != null) {
          p2 = new Point2D.Float(x2, y2);
        }

        if (p1 == null || p2 == null) {
          return Json.createObjectBuilder().add("success", false).add("error", "Must provide two valid points or entities for distance query").build();
        }

        double distancePx = p1.distance(p2);
        double distanceTiles = map.getTileWidth() > 0 ? distancePx / map.getTileWidth() : 0.0;

        return Json.createObjectBuilder()
            .add("success", true)
            .add("mode", mode)
            .add("distancePixels", distancePx)
            .add("distanceTiles", distanceTiles)
            .build();
      }

      default:
        return Json.createObjectBuilder().add("success", false).add("error", "Unknown query mode: " + mode).build();
    }
  }

  // SEARCH ASSET CATALOG TOOL IMPLEMENTATION
  private static JsonObject searchAssets(JsonObject args) {
    String query = getString(args, "query", "").toLowerCase();
    String typeFilter = getString(args, "type", "all").toLowerCase();

    JsonArrayBuilder matches = Json.createArrayBuilder();

    if (Editor.instance().getGameFile() != null) {
      if ("all".equals(typeFilter) || "spritesheet".equals(typeFilter)) {
        for (SpritesheetResource spr : Editor.instance().getGameFile().getSpriteSheets()) {
          if (spr == null) continue;
          String sprName = spr.getName() != null ? spr.getName() : "";
          if (query.isEmpty() || sprName.toLowerCase().contains(query)) {
            matches.add(Json.createObjectBuilder()
                .add("assetType", "spritesheet")
                .add("name", sprName)
                .add("width", spr.getWidth())
                .add("height", spr.getHeight()));
          }
        }
      }

      if ("all".equals(typeFilter) || "sound".equals(typeFilter)) {
        for (SoundResource snd : Editor.instance().getGameFile().getSounds()) {
          if (snd == null) continue;
          String sndName = snd.getName() != null ? snd.getName() : "";
          if (query.isEmpty() || sndName.toLowerCase().contains(query)) {
            matches.add(Json.createObjectBuilder()
                .add("assetType", "sound")
                .add("name", sndName)
                .add("format", snd.getFormat() != null ? snd.getFormat().toString() : ""));
          }
        }
      }
    }

    return Json.createObjectBuilder().add("success", true).add("matches", matches).build();
  }

  // BATCH EDIT ENTITIES TRANSACTIONAL IMPLEMENTATION WITH ROLLBACK
  private static JsonObject batchEditEntities(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    if (args == null || !args.containsKey("operations")) {
      return Json.createObjectBuilder().add("success", false).add("error", "Missing 'operations' array").build();
    }

    IMap map = Game.world().environment().getMap();
    JsonArray operations = args.getJsonArray("operations");
    JsonArrayBuilder outcomes = Json.createArrayBuilder();

    UndoManager.forMap(map).mapChanging(map);
    boolean errorEncountered = false;
    String firstError = "";

    for (int i = 0; i < operations.size(); i++) {
      if (!(operations.get(i) instanceof JsonObject opObj)) {
        errorEncountered = true;
        firstError = "Batch operation at index " + i + " must be an object";
        outcomes.add(
            Json.createObjectBuilder()
                .add("index", i)
                .add("status", "FAILED")
                .add("error", firstError));
        break;
      }
      String action = getString(opObj, "action", "create").toLowerCase();

      switch (action) {
        case "create": {
          String typeName = getString(opObj, "type", "");
          MapObjectType type =
              typeName == null ? null : MapObjectType.get(typeName.toUpperCase());
          if (type == null) {
            errorEncountered = true;
            firstError = "Unknown map object type at batch operation index " + i + ": " + typeName;
            outcomes.add(
                Json.createObjectBuilder()
                    .add("index", i)
                    .add("action", "create")
                    .add("status", "FAILED")
                    .add("error", firstError));
            break;
          }
          MapObject mo = createNativeBaseObject(type, opObj, 16f, 16f);
          applyCreationArguments(mo, opObj);
          JsonObject shapeError = applyShapeArguments(mo, opObj);
          List<String> validationErrors =
              McpEntityValidator.validateForCreation(mo, opObj);
          IMapObjectLayer targetLayer = resolveTargetObjectLayer(opObj);
          if (shapeError != null || !validationErrors.isEmpty() || targetLayer == null) {
            errorEncountered = true;
            if (shapeError != null) {
              firstError = shapeError.getString("error");
            } else if (!validationErrors.isEmpty()) {
              firstError = validationErrors.getFirst();
            } else {
              firstError =
                  "Target object layer not found: "
                      + getString(opObj, "layer", "<current or first object layer>");
            }
            firstError += " at batch operation index " + i;
            outcomes.add(
                Json.createObjectBuilder()
                    .add("index", i)
                    .add("action", "create")
                    .add("status", "FAILED")
                    .add("error", firstError));
            break;
          }
          assignNextMapId(mo);
          addCreatedMapObject(mo, targetLayer);

          outcomes.add(Json.createObjectBuilder()
              .add("index", i)
              .add("action", "create")
              .add("status", "SUCCESS")
              .add("id", mo.getId()));
          break;
        }

        case "update": {
          IMapObject target = findEntity(opObj);
          if (target == null) {
            errorEncountered = true;
            firstError = "Entity not found at batch operation index " + i;
            outcomes.add(Json.createObjectBuilder()
                .add("index", i)
                .add("action", "update")
                .add("status", "FAILED")
                .add("error", firstError));
            break;
          }

          Float x = getFloat(opObj, "x", null);
          Float y = getFloat(opObj, "y", null);
          Float w = getFloat(opObj, "width", null);
          Float h = getFloat(opObj, "height", null);
          if (hasInvalidFiniteFloat(opObj, "x", x)
              || hasInvalidFiniteFloat(opObj, "y", y)
              || hasInvalidFiniteFloat(opObj, "width", w)
              || hasInvalidFiniteFloat(opObj, "height", h)
              || w != null && w <= 0
              || h != null && h <= 0) {
            errorEncountered = true;
            firstError = "Invalid transform values at batch operation index " + i;
            outcomes.add(
                Json.createObjectBuilder()
                    .add("index", i)
                    .add("action", "update")
                    .add("status", "FAILED")
                    .add("error", firstError));
            break;
          }
          if (x != null) target.setX(x);
          if (y != null) target.setY(y);
          if (w != null) target.setWidth(w);
          if (h != null) target.setHeight(h);
          reloadLiveEntity(target);

          outcomes.add(Json.createObjectBuilder()
              .add("index", i)
              .add("action", "update")
              .add("status", "SUCCESS")
              .add("id", target.getId()));
          break;
        }

        case "delete": {
          IMapObject target = findEntity(opObj);
          if (target == null) {
            errorEncountered = true;
            firstError = "Entity not found at batch operation index " + i;
            outcomes.add(Json.createObjectBuilder()
                .add("index", i)
                .add("action", "delete")
                .add("status", "FAILED")
                .add("error", firstError));
            break;
          }

          Editor.instance().getMapComponent().delete(target);
          outcomes.add(Json.createObjectBuilder()
              .add("index", i)
              .add("action", "delete")
              .add("status", "SUCCESS")
              .add("id", target.getId()));
          break;
        }

        default: {
          errorEncountered = true;
          firstError = "Unknown action '" + action + "' at batch index " + i;
          outcomes.add(Json.createObjectBuilder()
              .add("index", i)
              .add("action", action)
              .add("status", "FAILED")
              .add("error", firstError));
        }
      }

      if (errorEncountered) break;
    }

    if (errorEncountered) {
      if (UndoManager.forMap(map).canUndo()) {
        UndoManager.forMap(map).undo();
      }
      refreshInspectorUI(null);
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Batch operation failed and was rolled back: " + firstError)
          .add("outcomes", outcomes)
          .build();
    }

    UndoManager.forMap(map).mapChanged(map);
    refreshInspectorUI(Editor.instance().getMapComponent().getFocusedMapObject());

    return Json.createObjectBuilder()
        .add("success", true)
        .add("operationCount", operations.size())
        .add("outcomes", outcomes)
        .build();
  }

  // NATIVE MAP OBJECT CREATION IMPLEMENTATIONS
  private static JsonObject addProp(JsonObject args) {
    return createAndCommitEntity(MapObjectType.PROP, args, 16f, 16f);
  }

  private static JsonObject addCreature(JsonObject args) {
    return createAndCommitEntity(MapObjectType.CREATURE, args, 16f, 16f);
  }

  private static JsonObject addCollisionbox(JsonObject args) {
    return createAndCommitEntity(MapObjectType.COLLISIONBOX, args, 16f, 16f);
  }

  private static JsonObject addTrigger(JsonObject args) {
    return createAndCommitEntity(MapObjectType.TRIGGER, args, 16f, 16f);
  }

  private static JsonObject addSpawnpoint(JsonObject args) {
    return createAndCommitEntity(MapObjectType.SPAWNPOINT, args, 16f, 16f);
  }

  private static JsonObject addArea(JsonObject args) {
    return createAndCommitEntity(MapObjectType.AREA, args, 32f, 32f);
  }

  private static JsonObject addLight(JsonObject args) {
    return createAndCommitEntity(MapObjectType.LIGHTSOURCE, args, 32f, 32f);
  }

  private static JsonObject addStaticShadow(JsonObject args) {
    return createAndCommitEntity(MapObjectType.STATICSHADOW, args, 16f, 16f);
  }

  private static JsonObject addEmitter(JsonObject args) {
    return createAndCommitEntity(MapObjectType.EMITTER, args, 16f, 16f);
  }

  private static JsonObject addSoundSource(JsonObject args) {
    return createAndCommitEntity(MapObjectType.SOUNDSOURCE, args, 16f, 16f);
  }

  private static JsonObject addItemSpawn(JsonObject args) {
    return createAndCommitEntity(MapObjectType.PROP, args, 16f, 16f);
  }

  private static JsonObject createAndCommitEntity(
      MapObjectType type, JsonObject args, float defaultWidth, float defaultHeight) {
    MapObject mo = createNativeBaseObject(type, args, defaultWidth, defaultHeight);
    applyCreationArguments(mo, args);
    JsonObject shapeError = applyShapeArguments(mo, args);
    if (shapeError != null) {
      return shapeError;
    }
    return commitAddedObject(mo, args);
  }

  private static MapObject createNativeBaseObject(MapObjectType type, JsonObject args, float defaultW, float defaultH) {
    float x = getFloat(args, "x", 0f);
    float y = getFloat(args, "y", 0f);
    float width = getFloat(args, "width", defaultW);
    float height = getFloat(args, "height", defaultH);
    MapObject mo =
        MapComponent.createMapObjectWithEditorDefaults(type, x, y, width, height);
    String name = getString(args, "name", null);
    if (name != null) {
      mo.setName(name);
    }
    return mo;
  }

  private static void applyCreationArguments(MapObject mo, JsonObject args) {
    if (mo == null || args == null) {
      return;
    }
    MapObjectType type = MapObjectType.get(mo.getType());
    if (type == null) {
      return;
    }

    switch (type) {
      case PROP:
        if (args.containsKey("spritesheetName")) {
          mo.setValue(
              MapObjectProperty.SPRITESHEETNAME,
              McpEntityValidator.normalizeSpriteReference(
                  type, getString(args, "spritesheetName", "")));
        }
        if (args.containsKey("material"))
          mo.setValue(MapObjectProperty.PROP_MATERIAL, getString(args, "material", ""));
        if (args.containsKey("addShadow"))
          mo.setValue(
              MapObjectProperty.PROP_ADDSHADOW,
              String.valueOf(getBoolean(args, "addShadow", false)));
        if (args.containsKey("indestructible"))
          mo.setValue(
              MapObjectProperty.COMBAT_INDESTRUCTIBLE,
              String.valueOf(getBoolean(args, "indestructible", false)));
        if (args.containsKey("collision"))
          mo.setValue(
              MapObjectProperty.COLLISION,
              String.valueOf(getBoolean(args, "collision", false)));
        if (args.containsKey("itemType"))
          mo.setValue("itemType", getString(args, "itemType", ""));
        if (args.containsKey("cooldown"))
          mo.setValue("cooldown", String.valueOf(getInt(args, "cooldown", 0)));
        break;
      case CREATURE:
        if (args.containsKey("spritesheetName")) {
          mo.setValue(
              MapObjectProperty.SPRITESHEETNAME,
              McpEntityValidator.normalizeSpriteReference(
                  type, getString(args, "spritesheetName", "")));
        }
        if (args.containsKey("scaleSprite"))
          mo.setValue(
              MapObjectProperty.SCALE_SPRITE,
              String.valueOf(getBoolean(args, "scaleSprite", false)));
        if (args.containsKey("velocity"))
          mo.setValue(
              MapObjectProperty.MOVEMENT_VELOCITY,
              String.valueOf(getFloat(args, "velocity", 100f)));
        if (args.containsKey("hitpoints"))
          mo.setValue(
              MapObjectProperty.COMBAT_HITPOINTS,
              String.valueOf(getInt(args, "hitpoints", 100)));
        if (args.containsKey("team"))
          mo.setValue(
              MapObjectProperty.COMBAT_TEAM, String.valueOf(getInt(args, "team", 0)));
        break;
      case COLLISIONBOX:
        mo.setValue(MapObjectProperty.COLLISION, true);
        break;
      case TRIGGER:
        if (args.containsKey("message"))
          mo.setValue(
              MapObjectProperty.TRIGGER_MESSAGE, getString(args, "message", ""));
        if (args.containsKey("activation"))
          mo.setValue(
              MapObjectProperty.TRIGGER_ACTIVATION,
              getString(args, "activation", "INTERACT"));
        if (args.containsKey("targets"))
          mo.setValue(
              MapObjectProperty.TRIGGER_TARGETS, resolveTriggerTargets(getString(args, "targets", "")));
        if (args.containsKey("cooldown"))
          mo.setValue(
              MapObjectProperty.TRIGGER_COOLDOWN,
              String.valueOf(getInt(args, "cooldown", 0)));
        if (args.containsKey("oneTime"))
          mo.setValue(
              MapObjectProperty.TRIGGER_ONETIME,
              String.valueOf(getBoolean(args, "oneTime", false)));
        break;
      case SPAWNPOINT:
        if (args.containsKey("spawnType"))
          mo.setValue(MapObjectProperty.SPAWN_INFO, getString(args, "spawnType", ""));
        if (args.containsKey("direction"))
          mo.setValue(
              MapObjectProperty.SPAWN_DIRECTION, getString(args, "direction", "DOWN"));
        break;
      case LIGHTSOURCE:
        if (args.containsKey("lightColor"))
          mo.setValue(
              MapObjectProperty.LIGHT_COLOR, getString(args, "lightColor", "#ffffff"));
        if (args.containsKey("lightIntensity"))
          mo.setValue(
              MapObjectProperty.LIGHT_INTENSITY,
              String.valueOf(getInt(args, "lightIntensity", 100)));
        if (args.containsKey("lightShape"))
          mo.setValue(
              MapObjectProperty.LIGHT_SHAPE,
              getString(args, "lightShape", "ELLIPSE"));
        if (args.containsKey("lightActive"))
          mo.setValue(
              MapObjectProperty.LIGHT_ACTIVE,
              String.valueOf(getBoolean(args, "lightActive", true)));
        break;
      case STATICSHADOW:
        if (args.containsKey("shadowType"))
          mo.setValue(
              MapObjectProperty.SHADOW_TYPE, getString(args, "shadowType", ""));
        break;
      case EMITTER:
        if (args.containsKey("emitterData"))
          mo.setValue(
              MapObjectProperty.Emitter.COLORS, getString(args, "emitterData", ""));
        break;
      case SOUNDSOURCE:
        if (args.containsKey("soundName"))
          mo.setValue(MapObjectProperty.SOUND_NAME, getString(args, "soundName", ""));
        if (args.containsKey("volume"))
          mo.setValue(
              MapObjectProperty.SOUND_VOLUME,
              String.valueOf(getFloat(args, "volume", 1.0f)));
        if (args.containsKey("loop"))
          mo.setValue(
              MapObjectProperty.SOUND_LOOP,
              String.valueOf(getBoolean(args, "loop", false)));
        if (args.containsKey("range"))
          mo.setValue(
              MapObjectProperty.SOUND_RANGE,
              String.valueOf(getInt(args, "range", 100)));
        break;
      case AREA:
        break;
    }

    if (args.containsKey("collisionType")) {
      mo.setValue(
          MapObjectProperty.COLLISION_TYPE,
          getString(args, "collisionType", "STATIC"));
    }
    applyAdditionalProperties(
        mo,
        args.get("properties") instanceof JsonObject properties ? properties : null);
  }

  private static void applyAdditionalProperties(MapObject mo, JsonObject properties) {
    if (properties == null) {
      return;
    }
    for (Map.Entry<String, JsonValue> entry : properties.entrySet()) {
      JsonValue value = entry.getValue();
      switch (value.getValueType()) {
        case STRING -> mo.setValue(entry.getKey(), ((JsonString) value).getString());
        case NUMBER, TRUE, FALSE -> mo.setValue(entry.getKey(), value.toString());
        case ARRAY, OBJECT, NULL -> {
          // The validator reports unsupported non-scalar values before map mutation.
        }
      }
    }
  }

  private static String resolveTriggerTargets(String rawTargets) {
    if (rawTargets == null || rawTargets.isBlank()) {
      return "";
    }
    List<String> resolvedIds = new ArrayList<>();
    de.gurkenlabs.litiengine.environment.tilemap.IMap map =
        de.gurkenlabs.litiengine.Game.world().environment() != null
            ? de.gurkenlabs.litiengine.Game.world().environment().getMap()
            : null;

    for (String token : rawTargets.split(",")) {
      String trimmed = token.trim();
      if (trimmed.isEmpty()) continue;
      try {
        int id = Integer.parseInt(trimmed);
        resolvedIds.add(String.valueOf(id));
      } catch (NumberFormatException _) {
        boolean resolved = false;
        if (map != null) {
          for (de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer layer : map.getMapObjectLayers()) {
            if (layer == null) continue;
            for (de.gurkenlabs.litiengine.environment.tilemap.IMapObject obj : layer.getMapObjects()) {
              if (obj != null && trimmed.equalsIgnoreCase(obj.getName())) {
                resolvedIds.add(String.valueOf(obj.getId()));
                resolved = true;
                break;
              }
            }
            if (resolved) break;
          }
        }
        if (!resolved) {
          resolvedIds.add(trimmed);
        }
      }
    }
    return String.join(",", resolvedIds);
  }

  private static JsonObject applyShapeArguments(MapObject mo, JsonObject args) {
    if (args == null || !args.containsKey("shapeType")) {
      return null;
    }
    String shape = getString(args, "shapeType", "").toLowerCase();
    if (!Set.of("rectangle", "polyline", "polygon", "point", "ellipse").contains(shape)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Unknown shapeType: " + shape)
          .build();
    }
    if (!"polyline".equals(shape) && !"polygon".equals(shape)) {
      return null;
    }
    if (!args.containsKey("points") || !(args.get("points") instanceof JsonArray points)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", shape + " requires a points array")
          .build();
    }
    if (points.size() < ("polygon".equals(shape) ? 3 : 2)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add(
              "error",
              shape
                  + " requires at least "
                  + ("polygon".equals(shape) ? 3 : 2)
                  + " points")
          .build();
    }

    PolyShape polyShape = new PolyShape();
    for (JsonValue value : points) {
      if (!(value instanceof JsonObject point)
          || getDouble(point, "x", null) == null
          || getDouble(point, "y", null) == null
          || !Double.isFinite(getDouble(point, "x", null))
          || !Double.isFinite(getDouble(point, "y", null))) {
        return Json.createObjectBuilder()
            .add("success", false)
            .add("error", "Each point must contain finite numeric x and y values")
            .build();
      }
      polyShape
          .getPoints()
          .add(
              new Point2D.Double(
                  getDouble(point, "x", 0.0), getDouble(point, "y", 0.0)));
    }
    if ("polyline".equals(shape)) {
      mo.setPolyline(polyShape);
    } else {
      mo.setPolygon(polyShape);
    }
    return null;
  }

  private static JsonObject commitAddedObject(MapObject mo, JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    List<String> validationErrors =
        McpEntityValidator.validateForCreation(mo, args);
    if (!validationErrors.isEmpty()) {
      return entityValidationError(validationErrors);
    }

    IMapObjectLayer targetLayer = resolveTargetObjectLayer(args);
    if (targetLayer == null) {
      String requestedLayer =
          args != null && args.containsKey("layer")
              ? getString(args, "layer", "")
              : "<current or first object layer>";
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Target object layer not found: " + requestedLayer)
          .build();
    }

    assignNextMapId(mo);
    addCreatedMapObject(mo, targetLayer);

    String refreshWarning = refreshInspectorAfterMutation(mo);

    IMap map = Game.world().environment().getMap();
    String mapPath = map.getPath() != null ? map.getPath().toString() : "";
    String mapName = map.getName() != null && !map.getName().isEmpty() ? map.getName() : mapPath;

    JsonObjectBuilder result = Json.createObjectBuilder()
        .add("success", true)
        .add("id", mo.getId())
        .add("type", mo.getType())
        .add("activeMapName", map.getName() != null ? map.getName() : "")
        .add("activeMapFile", mapPath)
        .add("message", "Added native " + mo.getType() + " ID " + mo.getId() + " on map '" + (mapName != null ? mapName : "map") + "'");
    if (refreshWarning != null) {
      result.add("warnings", Json.createArrayBuilder().add(refreshWarning));
    }
    return result.build();
  }

  private static JsonObject entityValidationError(
      List<String> validationErrors) {
    JsonArrayBuilder errors = Json.createArrayBuilder();
    validationErrors.forEach(errors::add);
    return Json.createObjectBuilder()
        .add("success", false)
        .add("error", "Entity validation failed: " + validationErrors.getFirst())
        .add("validationErrors", errors)
        .build();
  }

  private static IMapObjectLayer resolveTargetObjectLayer(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }
    IMap map = Game.world().environment().getMap();
    if (args != null && args.containsKey("layer")) {
      String layerName = getString(args, "layer", "");
      for (IMapObjectLayer layer : map.getMapObjectLayers()) {
        if (layer != null && layerName.equalsIgnoreCase(layer.getName())) {
          return layer;
        }
      }
      return null;
    }
    try {
      IMapObjectLayer currentLayer = UI.getLayerController().getCurrentLayer();
      if (currentLayer != null) {
        return currentLayer;
      }
    } catch (RuntimeException _) {
      // Headless MCP calls have no selected editor layer.
    }
    return map.getMapObjectLayers().stream().filter(java.util.Objects::nonNull).findFirst().orElse(null);
  }

  private static void addCreatedMapObject(
      MapObject mapObject, IMapObjectLayer targetLayer) {
    if (UI.getLayerController() == null) {
      targetLayer.addMapObject(mapObject);
      Game.world().environment().loadFromMap(mapObject.getId());
    } else {
      Editor.instance().getMapComponent().add(mapObject, targetLayer);
    }
    UndoManager.instance().mapObjectAdded(mapObject);
  }

  private static JsonObject exportMapSnapshot() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    try {
      IMap currentMap = Game.world().environment().getMap();
      Dimension size = currentMap.getSizeInPixels();
      BufferedImage img = new BufferedImage(Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = img.createGraphics();
      de.gurkenlabs.litiengine.environment.tilemap.MapRenderer.render(
          g2d, currentMap, currentMap.getBounds(), Game.world().environment());
      g2d.dispose();

      Path screenshotsDir = Path.of("screenshots");
      if (Files.notExists(screenshotsDir)) {
        Files.createDirectories(screenshotsDir);
      }
      String mapName = currentMap.getName() != null ? currentMap.getName() : "map";
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      Path filePath = screenshotsDir.resolve(mapName + "_" + timeStamp + ".png").toAbsolutePath();
      ImageIO.write(img, "png", filePath.toFile());

      return Json.createObjectBuilder()
          .add("success", true)
          .add("message", "Map snapshot saved successfully")
          .add("filePath", filePath.toString())
          .add("fileUri", filePath.toUri().toString())
          .add("width", size.width)
          .add("height", size.height)
          .build();
    } catch (Exception e) {
      return Json.createObjectBuilder().add("success", false).add("error", "Failed to export snapshot: " + e.getMessage()).build();
    }
  }

  private static JsonObject getCanvasSnapshot() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    try {
      var mapComp = Editor.instance().getMapComponent();
      Dimension renderComponentSize =
          Game.window() != null && Game.window().getRenderComponent() != null
              ? Game.window().getRenderComponent().getSize()
              : null;
      Dimension mapComponentSize =
          new Dimension(
              (int) Math.round(mapComp.getWidth()),
              (int) Math.round(mapComp.getHeight()));
      Dimension snapshotSize =
          selectCanvasSnapshotSize(
              renderComponentSize,
              mapComponentSize,
              Game.world().environment().getMap().getSizeInPixels());
      int w = snapshotSize.width;
      int h = snapshotSize.height;

      BufferedImage img = renderCanvasSnapshot(w, h);

      Path screenshotsDir = Path.of("screenshots");
      if (Files.notExists(screenshotsDir)) {
        Files.createDirectories(screenshotsDir);
      }
      IMap currentMap = Game.world().environment().getMap();
      String mapName = currentMap.getName() != null ? currentMap.getName() : "map";
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      Path filePath = screenshotsDir.resolve("canvas_" + mapName + "_" + timeStamp + ".png").toAbsolutePath();
      boolean written = ImageIO.write(img, "png", filePath.toFile());

      if (!written) {
        return Json.createObjectBuilder().add("success", false).add("error", "Failed to write canvas snapshot image to disk").build();
      }

      return Json.createObjectBuilder()
          .add("success", true)
          .add("message", "Canvas snapshot saved successfully (including debug overlays and UI)")
          .add("filePath", filePath.toString())
          .add("fileUri", filePath.toUri().toString())
          .add("width", w)
          .add("height", h)
          .build();
    } catch (Exception e) {
      return Json.createObjectBuilder().add("success", false).add("error", "Failed to capture canvas snapshot: " + e.getMessage()).build();
    }
  }

  static Dimension selectCanvasSnapshotSize(
      Dimension renderComponentSize,
      Dimension mapComponentSize,
      Dimension mapSize) {
    if (hasPositiveSize(renderComponentSize)) {
      return new Dimension(renderComponentSize);
    }
    if (hasPositiveSize(mapComponentSize)) {
      return new Dimension(mapComponentSize);
    }
    if (hasPositiveSize(mapSize)) {
      return new Dimension(mapSize);
    }
    return new Dimension(1, 1);
  }

  private static boolean hasPositiveSize(Dimension size) {
    return size != null && size.width > 0 && size.height > 0;
  }

  static BufferedImage renderCanvasSnapshot(int width, int height) {
    BufferedImage image =
        new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      graphics.setClip(0, 0, image.getWidth(), image.getHeight());
      Editor.instance().renderCanvas(graphics);
    } finally {
      graphics.dispose();
    }
    return image;
  }

  private static JsonObject openSnapshotFolder() {
    try {
      Path screenshotsDir = Path.of("screenshots").toAbsolutePath();
      if (Files.notExists(screenshotsDir)) {
        Files.createDirectories(screenshotsDir);
      }
      if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
        java.awt.Desktop.getDesktop().open(screenshotsDir.toFile());
        return Json.createObjectBuilder()
            .add("success", true)
            .add("message", "Opened screenshots directory in file explorer")
            .add("folderPath", screenshotsDir.toString())
            .add("folderUri", screenshotsDir.toUri().toString())
            .build();
      }
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Desktop API is not supported on this environment")
          .add("folderPath", screenshotsDir.toString())
          .add("folderUri", screenshotsDir.toUri().toString())
          .build();
    } catch (Exception e) {
      return Json.createObjectBuilder().add("success", false).add("error", "Failed to open folder: " + e.getMessage()).build();
    }
  }

  private static JsonObject getLogs(JsonObject args) {
    String levelFilter = getString(args, "level", "all").toLowerCase();
    int limit = getInt(args, "limit", 50);

    LogHandler logHandler = UI.getConsole() != null ? UI.getConsole().getLogHandler() : null;
    if (logHandler == null) {
      return Json.createObjectBuilder()
          .add("success", true)
          .add("warningCount", 0)
          .add("errorCount", 0)
          .add("logs", Json.createArrayBuilder().build())
          .build();
    }

    List<LogHandler.LogEntry> entries = logHandler.getRecentLogs();
    JsonArrayBuilder logsArr = Json.createArrayBuilder();
    int count = 0;

    for (int i = entries.size() - 1; i >= 0 && count < limit; i--) {
      LogHandler.LogEntry entry = entries.get(i);
      String entryLvl = entry.level() != null ? entry.level().toLowerCase() : "";
      if ("all".equals(levelFilter)
          || ("warning".equals(levelFilter) && ("warning".equals(entryLvl) || "severe".equals(entryLvl)))
          || ("error".equals(levelFilter) && "severe".equals(entryLvl))) {
        logsArr.add(Json.createObjectBuilder()
            .add("level", entry.level())
            .add("message", entry.message())
            .add("timestamp", entry.timestamp()));
        count++;
      }
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("warningCount", logHandler.getWarningCount())
        .add("errorCount", logHandler.getErrorCount())
        .add("latestErrorStack", logHandler.getLatestErrorStack() != null ? logHandler.getLatestErrorStack() : "")
        .add("logs", logsArr)
        .build();
  }

  private static JsonObject validateMap() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    IMap map = Game.world().environment().getMap();
    JsonArrayBuilder issues = Json.createArrayBuilder();
    int issueCount = 0;

    Set<Integer> seenIds = new HashSet<>();
    Set<String> validNamesAndIds = new HashSet<>();
    Set<String> projectSprites = new HashSet<>();

    if (Editor.instance().getGameFile() != null) {
      for (SpritesheetResource spr : Editor.instance().getGameFile().getSpriteSheets()) {
        if (spr != null && spr.getName() != null) {
          projectSprites.add(spr.getName());
        }
      }
    }

    for (IMapObject mo : map.getMapObjects()) {
      if (mo == null) continue;
      validNamesAndIds.add(String.valueOf(mo.getId()));
      if (mo.getName() != null && !mo.getName().isBlank()) {
        validNamesAndIds.add(mo.getName());
      }
    }

    List<IMapObject> objects = new ArrayList<>(map.getMapObjects());
    for (int i = 0; i < objects.size(); i++) {
      IMapObject mo = objects.get(i);
      if (mo == null) continue;

      if (!seenIds.add(mo.getId())) {
        issueCount++;
        issues.add(Json.createObjectBuilder()
            .add("severity", "ERROR")
            .add("type", "DUPLICATE_ID")
            .add("entityId", mo.getId())
            .add("message", "Duplicate entity ID found: " + mo.getId()));
      }

      if (mo.getX() < 0 || mo.getY() < 0 || mo.getX() > map.getSizeInPixels().width || mo.getY() > map.getSizeInPixels().height) {
        issueCount++;
        issues.add(Json.createObjectBuilder()
            .add("severity", "WARNING")
            .add("type", "OUT_OF_BOUNDS")
            .add("entityId", mo.getId())
            .add("message", "Entity is placed outside map boundaries at (" + mo.getX() + ", " + mo.getY() + ")"));
      }

      ICustomProperty targetsProp = mo.getProperty(MapObjectProperty.TRIGGER_TARGETS);
      if (targetsProp != null && targetsProp.getAsString() != null && !targetsProp.getAsString().isBlank()) {
        for (String target : targetsProp.getAsString().split(",")) {
          String trimmed = target.trim();
          if (!trimmed.isEmpty() && !validNamesAndIds.contains(trimmed)) {
            issueCount++;
            issues.add(Json.createObjectBuilder()
                .add("severity", "WARNING")
                .add("type", "MISSING_TRIGGER_TARGET")
                .add("entityId", mo.getId())
                .add("message", "Trigger target '" + trimmed + "' does not exist on map"));
          }
        }
      }

      ICustomProperty sprProp = mo.getProperty(MapObjectProperty.SPRITESHEETNAME);
      if (sprProp != null && sprProp.getAsString() != null && !sprProp.getAsString().isBlank()) {
        String sprName = sprProp.getAsString();
        if (!isSpriteAvailable(sprName, projectSprites)) {
          issueCount++;
          issues.add(Json.createObjectBuilder()
              .add("severity", "WARNING")
              .add("type", "UNMAPPED_SPRITE")
              .add("entityId", mo.getId())
              .add("message", "Referenced spritesheet '" + sprName + "' is missing from project assets"));
        }
      }

      ICustomProperty colProp = mo.getProperty(MapObjectProperty.COLLISION);
      if (colProp != null && colProp.getAsBool()) {
        for (int j = i + 1; j < objects.size(); j++) {
          IMapObject other = objects.get(j);
          if (other == null) continue;
          ICustomProperty otherColProp = other.getProperty(MapObjectProperty.COLLISION);
          if (otherColProp != null && otherColProp.getAsBool()) {
            if (mo.getBoundingBox().intersects(other.getBoundingBox())) {
              issueCount++;
              issues.add(Json.createObjectBuilder()
                  .add("severity", "INFO")
                  .add("type", "OVERLAPPING_COLLISION")
                  .add("entityId", mo.getId())
                  .add("message", "Collision box intersects with entity ID " + other.getId()));
            }
          }
        }
      }
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("map", map.getName() != null ? map.getName() : "")
        .add("issueCount", issueCount)
        .add("issues", issues)
        .build();
  }

  private static boolean isSpriteAvailable(String sprName, Set<String> projectSprites) {
    if (sprName == null || sprName.isBlank()) {
      return true;
    }
    String name = sprName.trim();
    if (projectSprites.contains(name)) {
      return true;
    }
    if (de.gurkenlabs.litiengine.resources.Resources.spritesheets().contains(name)
        || de.gurkenlabs.litiengine.resources.Resources.spritesheets().get(name) != null) {
      return true;
    }
    String cleanName = de.gurkenlabs.litiengine.util.io.FileUtilities.getFileName(name);
    if (projectSprites.contains(cleanName)
        || de.gurkenlabs.litiengine.resources.Resources.spritesheets().contains(cleanName)
        || de.gurkenlabs.litiengine.resources.Resources.spritesheets().get(cleanName) != null) {
      return true;
    }
    for (String known : projectSprites) {
      if (known.equalsIgnoreCase(name) || known.equalsIgnoreCase(cleanName)) {
        return true;
      }
      if (known.startsWith(name + "-") || known.startsWith(name + "_")
          || name.startsWith(known + "-") || name.startsWith(known + "_")
          || known.startsWith(cleanName + "-") || known.startsWith(cleanName + "_")
          || cleanName.startsWith(known + "-") || cleanName.startsWith(known + "_")) {
        return true;
      }
    }
    if (de.gurkenlabs.litiengine.resources.Resources.animations().contains(name)
        || de.gurkenlabs.litiengine.resources.Resources.animations().contains(cleanName)
        || de.gurkenlabs.litiengine.resources.Resources.images().contains(name)
        || de.gurkenlabs.litiengine.resources.Resources.images().contains(cleanName)) {
      return true;
    }
    return false;
  }

  private static JsonObject listEntities(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    IMap map = Game.world().environment().getMap();

    String filterType = getString(args, "type", null);
    String filterLayer = getString(args, "layer", null);
    boolean selectedOnly = getBoolean(args, "selectedOnly", false);

    Set<IMapObject> selectedSet = new HashSet<>(Editor.instance().getMapComponent().getSelectedMapObjects());
    JsonArrayBuilder entitiesArr = Json.createArrayBuilder();

    for (IMapObject obj : map.getMapObjects()) {
      if (obj == null) continue;
      if (selectedOnly && !selectedSet.contains(obj)) continue;
      if (filterType != null && !filterType.equalsIgnoreCase(obj.getType())) continue;
      String layerName = obj.getLayer() != null && obj.getLayer().getName() != null ? obj.getLayer().getName() : "";
      if (filterLayer != null && !filterLayer.equalsIgnoreCase(layerName)) continue;

      JsonObjectBuilder objBuilder = Json.createObjectBuilder();
      objBuilder.add("id", obj.getId());
      objBuilder.add("name", obj.getName() != null ? obj.getName() : "");
      objBuilder.add("type", obj.getType() != null ? obj.getType() : "");
      objBuilder.add("x", obj.getX());
      objBuilder.add("y", obj.getY());
      objBuilder.add("width", obj.getWidth());
      objBuilder.add("height", obj.getHeight());
      objBuilder.add("layer", layerName);

      JsonObjectBuilder propsBuilder = Json.createObjectBuilder();
      for (Map.Entry<String, ICustomProperty> entry : obj.getProperties().entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null) {
          propsBuilder.add(entry.getKey(), entry.getValue().getAsString() != null ? entry.getValue().getAsString() : "");
        }
      }
      objBuilder.add("properties", propsBuilder);
      entitiesArr.add(objBuilder);
    }

    return Json.createObjectBuilder().add("entities", entitiesArr).build();
  }

  private static JsonObject moveEntity(JsonObject args) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }

    Float x = getFloat(args, "x", null);
    Float y = getFloat(args, "y", null);
    Float dx = getFloat(args, "dx", null);
    Float dy = getFloat(args, "dy", null);
    if (hasInvalidFiniteFloat(args, "x", x)
        || hasInvalidFiniteFloat(args, "y", y)
        || hasInvalidFiniteFloat(args, "dx", dx)
        || hasInvalidFiniteFloat(args, "dy", dy)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Coordinates and offsets must be finite numbers")
          .build();
    }
    float finalX = x != null ? x : target.getX();
    float finalY = y != null ? y : target.getY();
    if (dx != null) finalX += dx;
    if (dy != null) finalY += dy;
    if (!Float.isFinite(finalX) || !Float.isFinite(finalY)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Resulting coordinates must be finite numbers")
          .build();
    }

    UndoManager.instance().mapObjectChanging(target);
    target.setX(finalX);
    target.setY(finalY);

    UndoManager.instance().mapObjectChanged(target);
    refreshInspectorUI(target);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Moved entity " + target.getId() + " to (" + target.getX() + ", " + target.getY() + ")")
        .build();
  }

  private static JsonObject resizeEntity(JsonObject args) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }

    Float w = getFloat(args, "width", null);
    Float h = getFloat(args, "height", null);
    if (hasInvalidFiniteFloat(args, "width", w)
        || hasInvalidFiniteFloat(args, "height", h)
        || w == null
        || h == null
        || w <= 0
        || h <= 0) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "width and height must be finite numbers greater than zero")
          .build();
    }

    UndoManager.instance().mapObjectChanging(target);
    target.setWidth(w);
    target.setHeight(h);
    UndoManager.instance().mapObjectChanged(target);
    refreshInspectorUI(target);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Resized entity " + target.getId() + " to " + target.getWidth() + "x" + target.getHeight())
        .build();
  }

  private static JsonObject setEntityLayer(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }
    String layerName = getString(args, "layer", null);
    if (layerName == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Missing 'layer' argument").build();
    }

    IMap map = Game.world().environment().getMap();

    IMapObjectLayer targetLayer = null;
    for (IMapObjectLayer l : map.getMapObjectLayers()) {
      if (l != null && layerName.equalsIgnoreCase(l.getName())) {
        targetLayer = l;
        break;
      }
    }

    if (targetLayer == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Target object layer not found: " + layerName).build();
    }

    UndoManager.instance().mapObjectChanging(target);
    if (target.getLayer() != null) {
      target.getLayer().removeMapObject(target);
    }
    targetLayer.addMapObject(target);
    UndoManager.instance().mapObjectChanged(target);
    refreshInspectorUI(target);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Reassigned entity " + target.getId() + " to layer '" + layerName + "'")
        .build();
  }

  private static JsonObject batchAddEntities(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    if (args == null || !args.containsKey("entities")) {
      return Json.createObjectBuilder().add("success", false).add("error", "Missing 'entities' array").build();
    }

    JsonArray entities = args.getJsonArray("entities");
    JsonArrayBuilder addedIds = Json.createArrayBuilder();
    List<PendingEntityCreation> pending = new ArrayList<>();

    for (int i = 0; i < entities.size(); i++) {
      JsonValue value = entities.get(i);
      if (!(value instanceof JsonObject entity)) {
        return batchCreationError(i, "Each entities entry must be an object");
      }
      String typeName = getString(entity, "type", "");
      MapObjectType type =
          typeName == null ? null : MapObjectType.get(typeName.toUpperCase());
      if (type == null) {
        return batchCreationError(i, "Unknown map object type: " + typeName);
      }
      MapObject mapObject = createNativeBaseObject(type, entity, 16f, 16f);
      applyCreationArguments(mapObject, entity);
      JsonObject shapeError = applyShapeArguments(mapObject, entity);
      if (shapeError != null) {
        return batchCreationError(i, shapeError.getString("error"));
      }
      List<String> validationErrors =
          McpEntityValidator.validateForCreation(mapObject, entity);
      if (!validationErrors.isEmpty()) {
        return batchCreationError(i, validationErrors.getFirst());
      }
      IMapObjectLayer layer = resolveTargetObjectLayer(entity);
      if (layer == null) {
        return batchCreationError(
            i,
            "Target object layer not found: "
                + getString(entity, "layer", "<current or first object layer>"));
      }
      pending.add(new PendingEntityCreation(mapObject, layer));
    }

    for (PendingEntityCreation creation : pending) {
      assignNextMapId(creation.mapObject());
      addCreatedMapObject(creation.mapObject(), creation.layer());
      addedIds.add(creation.mapObject().getId());
    }

    refreshInspectorUI(Editor.instance().getMapComponent().getFocusedMapObject());

    return Json.createObjectBuilder()
        .add("success", true)
        .add("addedCount", entities.size())
        .add("ids", addedIds)
        .build();
  }

  private static boolean hasInvalidFiniteFloat(
      JsonObject args, String property, Float parsedValue) {
    return args != null
        && args.containsKey(property)
        && (parsedValue == null || !Float.isFinite(parsedValue));
  }

  private static JsonObject batchCreationError(int index, String error) {
    return Json.createObjectBuilder()
        .add("success", false)
        .add("error", "Entity at batch index " + index + " is invalid: " + error)
        .add("failedIndex", index)
        .build();
  }

  private record PendingEntityCreation(
      MapObject mapObject, IMapObjectLayer layer) {}

  private static JsonObject fillTiles(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    IMap map = Game.world().environment().getMap();

    JsonArray regions = args.getJsonArray("regions");
    if (regions != null && !regions.isEmpty()) {
      int totalTiles = 0;
      int regionCount = 0;
      for (int i = 0; i < regions.size(); i++) {
        if (regions.get(i) instanceof JsonObject region) {
          String layerName = getString(region, "layer", getString(args, "layer", ""));
          int startX = getInt(region, "x", 0);
          int startY = getInt(region, "y", 0);
          int width = getInt(region, "width", 1);
          int height = getInt(region, "height", 1);
          int gid = getInt(region, "gid", 0);

          ITileLayer target = null;
          for (ITileLayer l : map.getTileLayers()) {
            if (l != null && (layerName.isEmpty() || layerName.equalsIgnoreCase(l.getName()))) {
              target = l;
              break;
            }
          }
          if (target == null) {
            continue;
          }
          UndoManager.forMap(map).layerChanging(target);
          for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
              target.setTile(startX + x, startY + y, gid);
            }
          }
          UndoManager.forMap(map).layerChanged(target);
          totalTiles += (width * height);
          regionCount++;
        }
      }
      refreshInspectorUI(null);
      return Json.createObjectBuilder()
          .add("success", true)
          .add("message", "Filled " + totalTiles + " tiles across " + regionCount + " regions")
          .build();
    }

    String layerName = getString(args, "layer", "");
    int startX = getInt(args, "x", 0);
    int startY = getInt(args, "y", 0);
    int width = getInt(args, "width", 1);
    int height = getInt(args, "height", 1);
    int gid = getInt(args, "gid", 0);

    ITileLayer target = null;
    for (ITileLayer l : map.getTileLayers()) {
      if (l != null && (layerName.isEmpty() || layerName.equalsIgnoreCase(l.getName()))) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Tile layer not found: " + layerName).build();
    }

    UndoManager.forMap(map).layerChanging(target);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        target.setTile(startX + x, startY + y, gid);
      }
    }
    UndoManager.forMap(map).layerChanged(target);
    refreshInspectorUI(null);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Filled " + (width * height) + " tiles in region (" + startX + ", " + startY + ", " + width + "x" + height + ") with GID " + gid)
        .build();
  }

  private static JsonObject getLayers() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    return McpResourceHandler.getMapLayers("current");
  }

  private static JsonObject addLayer(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    String name = getString(args, "name", "New Layer");
    String type = getString(args, "type", "tile").toLowerCase();
    IMap map = Game.world().environment().getMap();

    ILayer layer;
    if ("tile".equals(type)) {
      layer = new TileLayer(map.getWidth(), map.getHeight());
    } else if ("object".equals(type)) {
      layer = new MapObjectLayer();
    } else if ("group".equals(type)) {
      layer = new GroupLayer();
    } else {
      return Json.createObjectBuilder().add("success", false).add("error", "Invalid layer type: " + type).build();
    }

    layer.setName(name);

    UndoManager.forMap(map).layerStructureChanging(map);
    Integer idx = getInt(args, "index", null);
    if (idx != null) {
      map.addLayer(Math.min(idx, map.getRenderLayers().size()), layer);
    } else {
      map.addLayer(layer);
    }
    UndoManager.forMap(map).layerStructureChanged(map);

    refreshInspectorUI(null);

    return Json.createObjectBuilder().add("success", true).add("message", "Added layer: " + name).build();
  }

  private static JsonObject removeLayer(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    String name = getString(args, "name", "");
    IMap map = Game.world().environment().getMap();

    ILayer target = null;
    for (ILayer l : map.getRenderLayers()) {
      if (l != null && name.equalsIgnoreCase(l.getName())) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Layer not found: " + name).build();
    }

    UndoManager.forMap(map).layerStructureChanging(map);
    map.removeLayer(target);
    UndoManager.forMap(map).layerStructureChanged(map);

    refreshInspectorUI(null);

    return Json.createObjectBuilder().add("success", true).add("message", "Removed layer: " + name).build();
  }

  private static JsonObject configureLayer(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }
    String name = getString(args, "name", "");
    IMap map = Game.world().environment().getMap();

    ILayer target = null;
    for (ILayer l : map.getRenderLayers()) {
      if (l != null && name.equalsIgnoreCase(l.getName())) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Layer not found: " + name).build();
    }

    UndoManager.forMap(map).layerChanging(target);

    String newName = getString(args, "newName", null);
    Boolean visible = getBoolean(args, "visible", null);
    Float opacity = getFloat(args, "opacity", null);

    if (newName != null) target.setName(newName);
    if (visible != null) target.setVisible(visible);
    if (opacity != null) target.setOpacity(opacity);

    UndoManager.forMap(map).layerChanged(target);

    refreshInspectorUI(null);

    return Json.createObjectBuilder().add("success", true).add("message", "Configured layer: " + target.getName()).build();
  }

  private static JsonObject setTile(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    String layerName = getString(args, "layer", "");
    int x = getInt(args, "x", 0);
    int y = getInt(args, "y", 0);
    int gid = getInt(args, "gid", 0);
    IMap map = Game.world().environment().getMap();

    ITileLayer target = null;
    for (ITileLayer l : map.getTileLayers()) {
      if (l != null && (layerName.isEmpty() || layerName.equalsIgnoreCase(l.getName()))) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Tile layer not found: " + layerName).build();
    }

    UndoManager.forMap(map).layerChanging(target);
    target.setTile(x, y, gid);
    UndoManager.forMap(map).layerChanged(target);

    refreshInspectorUI(null);

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Set tile (" + x + ", " + y + ") = GID " + gid + " on layer " + target.getName())
        .build();
  }

  private static JsonObject setTiles(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    String layerName = getString(args, "layer", "");
    IMap map = Game.world().environment().getMap();

    ITileLayer target = null;
    for (ITileLayer l : map.getTileLayers()) {
      if (l != null && (layerName.isEmpty() || layerName.equalsIgnoreCase(l.getName()))) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Tile layer not found: " + layerName).build();
    }

    final Map<TileCoordinate, Integer> edits;
    try {
      edits = parseTileEdits(args);
    } catch (IllegalArgumentException | ClassCastException ex) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", ex.getMessage())
          .build();
    }
    if (edits.isEmpty()) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add(
              "error",
              "Provide a dense gids region, sparse tiles, or grouped placements")
          .build();
    }
    for (TileCoordinate coordinate : edits.keySet()) {
      if (coordinate.x() < 0
          || coordinate.y() < 0
          || coordinate.x() >= map.getWidth()
          || coordinate.y() >= map.getHeight()) {
        return Json.createObjectBuilder()
            .add("success", false)
            .add(
                "error",
                "Tile coordinate ("
                    + coordinate.x()
                    + ", "
                    + coordinate.y()
                    + ") is outside the active map")
            .build();
      }
    }

    UndoManager.forMap(map).layerChanging(target);
    int changed = 0;
    for (Map.Entry<TileCoordinate, Integer> edit : edits.entrySet()) {
      TileCoordinate coordinate = edit.getKey();
      ITile current = target.getTile(coordinate.x(), coordinate.y());
      if (current == null || current.getGridId() != edit.getValue()) {
        target.setTile(coordinate.x(), coordinate.y(), edit.getValue());
        changed++;
      }
    }
    UndoManager.forMap(map).layerChanged(target);

    String refreshWarning = refreshInspectorAfterMutation(null);

    JsonObjectBuilder result = Json.createObjectBuilder()
        .add("success", true)
        .add("requestedTiles", edits.size())
        .add("changedTiles", changed)
        .add(
            "message",
            "Applied "
                + edits.size()
                + " tile edits on layer "
                + target.getName());
    if (refreshWarning != null) {
      result.add("warnings", Json.createArrayBuilder().add(refreshWarning));
    }
    return result.build();
  }

  private static Map<TileCoordinate, Integer> parseTileEdits(JsonObject args) {
    if (args == null) {
      return Map.of();
    }

    Map<TileCoordinate, Integer> edits = new LinkedHashMap<>();
    boolean hasDenseArgument =
        args.containsKey("x")
            || args.containsKey("y")
            || args.containsKey("width")
            || args.containsKey("height")
            || args.containsKey("gids");
    if (hasDenseArgument) {
      if (!args.containsKey("x")
          || !args.containsKey("y")
          || !args.containsKey("width")
          || !args.containsKey("height")
          || !args.containsKey("gids")) {
        throw new IllegalArgumentException(
            "Dense editing requires x, y, width, height, and gids");
      }
      int startX = args.getInt("x");
      int startY = args.getInt("y");
      int width = args.getInt("width");
      int height = args.getInt("height");
      if (width < 1 || height < 1) {
        throw new IllegalArgumentException("Dense region width and height must be positive");
      }
      JsonArray gids = args.getJsonArray("gids");
      if (gids == null || gids.isEmpty()) {
        throw new IllegalArgumentException("'gids' must contain at least one GID");
      }
      if (gids.size() > width * height) {
        throw new IllegalArgumentException(
            "'gids' contains more values than the dense region can hold");
      }
      for (int index = 0; index < gids.size(); index++) {
        int x = startX + index % width;
        int y = startY + index / width;
        putTileEdit(edits, x, y, gids.getInt(index));
      }
    }

    if (args.containsKey("tiles")) {
      JsonArray tiles = args.getJsonArray("tiles");
      if (tiles == null) {
        throw new IllegalArgumentException("'tiles' must be an array");
      }
      for (int index = 0; index < tiles.size(); index++) {
        JsonObject tile = tiles.getJsonObject(index);
        if (!tile.containsKey("x") || !tile.containsKey("y") || !tile.containsKey("gid")) {
          throw new IllegalArgumentException("'tiles[" + index + "]' requires x, y, and gid");
        }
        putTileEdit(edits, tile.getInt("x"), tile.getInt("y"), tile.getInt("gid"));
      }
    }

    if (args.containsKey("placements")) {
      JsonArray placements = args.getJsonArray("placements");
      if (placements == null) {
        throw new IllegalArgumentException("'placements' must be an array");
      }
      for (int placementIndex = 0; placementIndex < placements.size(); placementIndex++) {
        JsonObject placement = placements.getJsonObject(placementIndex);
        if (!placement.containsKey("gid") || !placement.containsKey("cells")) {
          throw new IllegalArgumentException(
              "'placements[" + placementIndex + "]' requires gid and cells");
        }
        int gid = placement.getInt("gid");
        JsonArray cells = placement.getJsonArray("cells");
        if (cells == null || cells.isEmpty()) {
          throw new IllegalArgumentException(
              "'placements[" + placementIndex + "].cells' must not be empty");
        }
        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
          JsonObject cell = cells.getJsonObject(cellIndex);
          if (!cell.containsKey("x") || !cell.containsKey("y")) {
            throw new IllegalArgumentException(
                "'placements["
                    + placementIndex
                    + "].cells["
                    + cellIndex
                    + "]' requires x and y");
          }
          putTileEdit(edits, cell.getInt("x"), cell.getInt("y"), gid);
        }
      }
    }
    return edits;
  }

  private static void putTileEdit(
      Map<TileCoordinate, Integer> edits, int x, int y, int gid) {
    if (gid < 0) {
      throw new IllegalArgumentException("Tile GIDs must not be negative");
    }
    edits.put(new TileCoordinate(x, y), gid);
  }

  private record TileCoordinate(int x, int y) {}

  private static JsonObject getTileInfo(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    int x = getInt(args, "x", 0);
    int y = getInt(args, "y", 0);
    String layerName = getString(args, "layer", "");
    IMap map = Game.world().environment().getMap();

    ITileLayer target = null;
    for (ITileLayer l : map.getTileLayers()) {
      if (l != null && (layerName.isEmpty() || layerName.equalsIgnoreCase(l.getName()))) {
        target = l;
        break;
      }
    }

    if (target == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Tile layer not found").build();
    }

    ITile tile = target.getTile(x, y);
    int gid = tile != null ? tile.getGridId() : 0;

    return Json.createObjectBuilder()
        .add("layer", target.getName())
        .add("x", x)
        .add("y", y)
        .add("gid", gid)
        .build();
  }

  private static JsonObject getTilesInfo(JsonObject args) {
    if (Game.world().environment() == null
        || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "No active map loaded")
          .build();
    }

    JsonArray queries;
    try {
      queries = args == null ? null : args.getJsonArray("queries");
    } catch (ClassCastException ex) {
      queries = null;
    }
    if (queries == null || queries.isEmpty()) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "'queries' must contain at least one tile query")
          .build();
    }
    if (queries.size() > 512) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "'queries' must not contain more than 512 tile queries")
          .build();
    }

    IMap map = Game.world().environment().getMap();
    Map<String, ITileLayer> layersByName = new LinkedHashMap<>();
    for (ITileLayer layer : map.getTileLayers()) {
      if (layer != null && layer.getName() != null) {
        layersByName.putIfAbsent(
            layer.getName().toLowerCase(java.util.Locale.ROOT), layer);
      }
    }

    JsonArrayBuilder results = Json.createArrayBuilder();
    int errorCount = 0;
    for (int index = 0; index < queries.size(); index++) {
      JsonValue value = queries.get(index);
      if (!(value instanceof JsonObject query)) {
        results.add(tileQueryError(index, value, "Query must be an object"));
        errorCount++;
        continue;
      }

      String layerName = getString(query, "layer", null);
      Integer x = getInt(query, "x", null);
      Integer y = getInt(query, "y", null);
      if (layerName == null || layerName.isBlank() || x == null || y == null) {
        results.add(
            tileQueryError(
                index, query, "Query requires a non-empty layer and integer x and y"));
        errorCount++;
        continue;
      }

      ITileLayer layer =
          layersByName.get(layerName.toLowerCase(java.util.Locale.ROOT));
      if (layer == null) {
        results.add(
            tileQueryError(index, query, "Tile layer not found: " + layerName));
        errorCount++;
        continue;
      }
      if (x < 0 || y < 0 || x >= layer.getWidth() || y >= layer.getHeight()) {
        results.add(
            tileQueryError(
                index,
                query,
                "Tile coordinate ("
                    + x
                    + ", "
                    + y
                    + ") is outside layer "
                    + layer.getName()));
        errorCount++;
        continue;
      }

      ITile tile = layer.getTile(x, y);
      results.add(
          Json.createObjectBuilder()
              .add("success", true)
              .add("index", index)
              .add("layer", layer.getName())
              .add("x", x)
              .add("y", y)
              .add("gid", tile != null ? tile.getGridId() : 0));
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("requestedQueries", queries.size())
        .add("errorCount", errorCount)
        .add("results", results)
        .build();
  }

  private static JsonObject tileQueryError(
      int index, JsonValue query, String message) {
    return Json.createObjectBuilder()
        .add("success", false)
        .add("index", index)
        .add("query", query == null ? JsonValue.NULL : query)
        .add("error", message)
        .build();
  }

  private static JsonObject addEntity(JsonObject args) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "No active map loaded").build();
    }

    String typeName = getString(args, "type", "");
    MapObjectType type =
        typeName == null ? null : MapObjectType.get(typeName.toUpperCase());
    if (type == null) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Unknown map object type: " + typeName)
          .build();
    }
    return createAndCommitEntity(type, args, 16f, 16f);
  }

  private static JsonObject removeEntity(JsonObject args) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }
    Editor.instance().getMapComponent().delete(target);
    refreshInspectorUI(null);
    return Json.createObjectBuilder().add("success", true).add("message", "Removed entity ID " + target.getId()).build();
  }

  private static JsonObject getEntityInfo(JsonObject args) {
    EntityLocationResult loc = findEntityWithLocation(args);
    if (loc == null || loc.object() == null) {
      return entityNotFoundError(args);
    }

    IMapObject target = loc.object();
    IMap map = loc.map();

    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("success", true);
    if (map != null && map.getName() != null) {
      builder.add("map", map.getName());
    }
    builder.add("id", target.getId());
    builder.add("name", target.getName() != null ? target.getName() : "");
    builder.add("type", target.getType() != null ? target.getType() : "");
    builder.add("x", target.getX());
    builder.add("y", target.getY());
    builder.add("width", target.getWidth());
    builder.add("height", target.getHeight());
    builder.add("isPolyline", target.isPolyline());
    builder.add("isPolygon", target.isPolygon());
    builder.add("isPoint", target.isPoint());

    JsonObjectBuilder propsBuilder = Json.createObjectBuilder();
    for (Map.Entry<String, ICustomProperty> entry : target.getProperties().entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null) {
        propsBuilder.add(entry.getKey(), entry.getValue().getAsString() != null ? entry.getValue().getAsString() : "");
      }
    }
    builder.add("properties", propsBuilder);
    return builder.build();
  }

  private static JsonObject setEntityProperty(JsonObject args) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }
    String prop = getString(args, "property", null);
    String val = getString(args, "value", null);
    if (prop == null || val == null) {
      return Json.createObjectBuilder().add("success", false).add("error", "Missing 'property' or 'value'").build();
    }

    String normalizedValue = normalizePropertyValue(target, prop, val);
    JsonObject validationError =
        validateAndApplyPropertyChanges(
            target, Map.of(prop, normalizedValue));
    if (validationError != null) {
      return validationError;
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Set property '" + prop + "' = '" + normalizedValue + "' on entity " + target.getId())
        .build();
  }

  private static JsonObject setProperties(JsonObject args, String... targetProperties) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }

    Map<String, String> changes = new LinkedHashMap<>();
    for (String prop : targetProperties) {
      if (args != null && args.containsKey(prop)) {
        changes.put(
            prop,
            normalizePropertyValue(target, prop, getString(args, prop, "")));
      }
    }
    JsonObject validationError = validateAndApplyPropertyChanges(target, changes);
    if (validationError != null) {
      return validationError;
    }

    return Json.createObjectBuilder().add("success", true).add("message", "Updated properties on entity " + target.getId()).build();
  }

  static int assignNextMapId(MapObject mapObject) {
    int id = Game.world().environment().getNextMapId();
    mapObject.setId(id);
    return id;
  }

  @SafeVarargs
  private static JsonObject setProperties(JsonObject args, Map.Entry<String, String>... propertyMappings) {
    IMapObject target = findEntity(args);
    if (target == null) {
      return entityNotFoundError(args);
    }

    Map<String, String> changes = new LinkedHashMap<>();
    for (Map.Entry<String, String> mapping : propertyMappings) {
      if (args != null && args.containsKey(mapping.getKey())) {
        changes.put(
            mapping.getValue(),
            normalizePropertyValue(
                target,
                mapping.getValue(),
                getString(args, mapping.getKey(), "")));
      }
    }
    JsonObject validationError = validateAndApplyPropertyChanges(target, changes);
    if (validationError != null) {
      return validationError;
    }

    return Json.createObjectBuilder().add("success", true).add("message", "Updated properties on entity " + target.getId()).build();
  }

  private static String normalizePropertyValue(
      IMapObject target, String property, String value) {
    if (!MapObjectProperty.SPRITESHEETNAME.equals(property)) {
      return value;
    }
    MapObjectType type = MapObjectType.get(target.getType());
    if (type != MapObjectType.PROP && type != MapObjectType.CREATURE) {
      return value;
    }
    return McpEntityValidator.normalizeSpriteReference(type, value);
  }

  private static JsonObject validateAndApplyPropertyChanges(
      IMapObject target, Map<String, String> changes) {
    if (changes.isEmpty()) {
      return null;
    }
    if (!(target instanceof MapObject original)) {
      return Json.createObjectBuilder()
          .add("success", false)
          .add("error", "Entity type does not support validated property updates")
          .build();
    }

    MapObject candidate = new MapObject(original, true);
    changes.forEach(candidate::setValue);
    List<String> validationErrors =
        McpEntityValidator.validateForCreation(candidate, null);
    if (!validationErrors.isEmpty()) {
      return entityValidationError(validationErrors);
    }

    UndoManager.instance().mapObjectChanging(target);
    changes.forEach(target::setValue);
    UndoManager.instance().mapObjectChanged(target);
    reloadLiveEntity(target);
    refreshInspectorAfterMutation(target);
    return null;
  }

  private static void reloadLiveEntity(IMapObject target) {
    if (target != null && Game.world().environment() != null) {
      Game.world().environment().reloadFromMap(target.getId());
    }
  }

  private static IMap findMapByName(String name) {
    if (name == null || name.isBlank()) return null;
    for (TmxMap map : Editor.instance().getMapComponent().getMaps()) {
      if (map != null && map.getName() != null && map.getName().equalsIgnoreCase(name)) {
        return map;
      }
    }
    if (Editor.instance().getGameFile() != null) {
      for (IMap map : Editor.instance().getGameFile().getMaps()) {
        if (map != null && map.getName() != null && map.getName().equalsIgnoreCase(name)) {
          return map;
        }
      }
    }
    return null;
  }

  private static IMap getActiveMap() {
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      return Game.world().environment().getMap();
    }
    if (!Editor.instance().getMapComponent().getMaps().isEmpty()) {
      return Editor.instance().getMapComponent().getMaps().get(0);
    }
    return null;
  }

  private static List<IMap> getAllProjectMaps() {
    List<IMap> result = new ArrayList<>(Editor.instance().getMapComponent().getMaps());
    if (Editor.instance().getGameFile() != null) {
      for (IMap m : Editor.instance().getGameFile().getMaps()) {
        if (m != null && !result.contains(m)) {
          result.add(m);
        }
      }
    }
    return result;
  }

  private record EntityLocationResult(IMap map, IMapObject object, boolean fromExplicitMap) {}

  private static EntityLocationResult findEntityWithLocation(JsonObject args) {
    if (args == null) {
      return null;
    }

    String specifiedMapName = getString(args, "mapId", getString(args, "map", getString(args, "mapName", null)));
    IMap primaryMap = null;

    if (specifiedMapName != null && !specifiedMapName.isBlank()) {
      primaryMap = findMapByName(specifiedMapName);
    }
    if (primaryMap == null) {
      primaryMap = getActiveMap();
    }

    if (primaryMap != null) {
      IMapObject obj = findEntityOnMap(primaryMap, args);
      if (obj != null) {
        return new EntityLocationResult(primaryMap, obj, specifiedMapName != null);
      }
    }

    // If not found on primary map and no explicit map was requested, search across all project maps
    if (specifiedMapName == null) {
      for (IMap map : getAllProjectMaps()) {
        if (map == null || map == primaryMap) continue;
        IMapObject obj = findEntityOnMap(map, args);
        if (obj != null) {
          return new EntityLocationResult(map, obj, false);
        }
      }
    }

    return null;
  }

  private static IMapObject findEntityOnMap(IMap map, JsonObject args) {
    if (map == null || args == null) return null;

    // 1. Try numeric or string ID from "id" or "entityId"
    for (String idProp : new String[] { "id", "entityId" }) {
      if (args.containsKey(idProp) && !args.isNull(idProp)) {
        JsonValue val = args.get(idProp);
        if (val.getValueType() == JsonValue.ValueType.NUMBER) {
          int numericId = ((JsonNumber) val).intValue();
          IMapObject obj = map.getMapObject(numericId);
          if (obj != null) return obj;
        } else if (val.getValueType() == JsonValue.ValueType.STRING) {
          String strVal = ((JsonString) val).getString().trim();
          try {
            int numericId = Integer.parseInt(strVal);
            IMapObject obj = map.getMapObject(numericId);
            if (obj != null) return obj;
          } catch (NumberFormatException _) {
            for (IMapObject obj : map.getMapObjects()) {
              if (obj != null && strVal.equalsIgnoreCase(obj.getName())) {
                return obj;
              }
            }
          }
        }
      }
    }

    // 2. Try string name from "name", "entity", "target", "entityName"
    for (String nameProp : new String[] { "name", "entity", "target", "entityName" }) {
      String name = getString(args, nameProp, null);
      if (name != null && !name.isBlank()) {
        for (IMapObject obj : map.getMapObjects()) {
          if (obj != null && name.equalsIgnoreCase(obj.getName())) {
            return obj;
          }
        }
      }
    }

    return null;
  }

  private static IMapObject findEntity(JsonObject args) {
    EntityLocationResult loc = findEntityWithLocation(args);
    if (loc == null || loc.object() == null) {
      return null;
    }
    if (!loc.fromExplicitMap() && loc.map() != getActiveMap()) {
      return null;
    }
    return loc.object();
  }

  private static JsonObject entityNotFoundError(JsonObject args) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("success", false);

    Integer id = getInt(args, "id", null);
    if (id == null) {
      String idStr = getString(args, "id", null);
      if (idStr != null) {
        try {
          id = Integer.parseInt(idStr);
        } catch (Exception _) {}
      }
    }
    String name = getString(args, "name", null);
    String entityRef = id != null ? "ID " + id : (name != null ? "name '" + name + "'" : "specified");

    String activeMapName = "";
    String activeMapFile = "";
    IMap activeMap = getActiveMap();
    if (activeMap != null) {
      activeMapName = activeMap.getName() != null ? activeMap.getName() : "";
      activeMapFile = activeMap.getPath() != null ? activeMap.getPath().toString() : "";
    }

    builder.add("activeMapName", activeMapName);
    builder.add("activeMapFile", activeMapFile);

    String foundOnMap = null;
    for (IMap map : getAllProjectMaps()) {
      if (map != null && map != activeMap && findEntityOnMap(map, args) != null) {
        foundOnMap = map.getName();
        break;
      }
    }

    if (foundOnMap != null) {
      builder.add("foundOnMap", foundOnMap);
      builder.add("error", "Entity " + entityRef + " not found on active map '" + activeMapName + "' (found on map '" + foundOnMap + "'). Pass map='" + foundOnMap + "' or switch active map using select-map name='" + foundOnMap + "'.");
    } else {
      builder.add("error", "Entity " + entityRef + " not found on active map '" + activeMapName + "'");
    }

    return builder.build();
  }

  private static JsonObject getPropertyDocs(JsonObject args) {
    String propFilter = getString(args, "property", null);
    String catFilter = getString(args, "category", null);
    String typeFilter = getString(args, "type", null);

    if (propFilter != null) {
      TmxPropertyMetadataRegistry.PropertyMetadata p = TmxPropertyMetadataRegistry.getProperty(propFilter);
      if (p != null) {
        return Json.createObjectBuilder()
            .add("success", true)
            .add("property", Json.createObjectBuilder()
                .add("name", p.name())
                .add("description", p.description())
                .add("category", p.category())
                .add("type", p.type())
                .add("defaultValue", p.defaultValue()))
            .build();
      } else {
        return Json.createObjectBuilder().add("success", false).add("error", "Property not found: " + propFilter).build();
      }
    }

    JsonObjectBuilder resultBuilder = Json.createObjectBuilder();
    resultBuilder.add("success", true);

    if (typeFilter != null) {
      TmxPropertyMetadataRegistry.TypeMetadata t = TmxPropertyMetadataRegistry.getType(typeFilter.toUpperCase());
      if (t != null) {
        resultBuilder.add("typeInfo", Json.createObjectBuilder()
            .add("type", t.typeName())
            .add("name", t.displayName())
            .add("description", t.description()));
      }
    }

    List<TmxPropertyMetadataRegistry.PropertyMetadata> propList;
    if (catFilter != null) {
      propList = TmxPropertyMetadataRegistry.getPropertiesByCategory(catFilter);
    } else {
      propList = TmxPropertyMetadataRegistry.getAllProperties();
    }

    JsonArrayBuilder propsArr = Json.createArrayBuilder();
    for (TmxPropertyMetadataRegistry.PropertyMetadata p : propList) {
      propsArr.add(Json.createObjectBuilder()
          .add("name", p.name())
          .add("description", p.description())
          .add("category", p.category())
          .add("type", p.type())
          .add("defaultValue", p.defaultValue()));
    }
    resultBuilder.add("properties", propsArr);

    return resultBuilder.build();
  }
}
