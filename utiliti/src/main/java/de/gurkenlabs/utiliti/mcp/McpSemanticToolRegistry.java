package de.gurkenlabs.utiliti.mcp;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public final class McpSemanticToolRegistry {
  private McpSemanticToolRegistry() {}

  public static JsonObject getSemanticToolsList() {
    JsonArrayBuilder tools = Json.createArrayBuilder();

    // 1. get_project_context
    tools.add(createToolDef(
        "get_project_context",
        "Get complete project context including active map names, loaded tilesets, blueprint templates, and server revision numbers. Use this tool first to inspect available assets before making edits.",
        noParams(),
        true, false, true, false));

    // 2. get_map
    JsonObjectBuilder getMapParams = Json.createObjectBuilder();
    getMapParams.add("mapId", McpToolHandler.createParam("string", "Unique map name (e.g. 'hospital')", true));
    tools.add(createToolDef(
        "get_map",
        "Inspect map structure including layers, tile dimensions, pixel dimensions, and revision counter. For spatial querying of entities and tiles within a region, use query_region instead.",
        getMapParams.build(),
        true, false, true, false));

    // 3. query_region
    JsonObjectBuilder queryRegionParams = Json.createObjectBuilder();
    queryRegionParams.add("mapId", McpToolHandler.createParam("string", "Unique map name", true));
    queryRegionParams.add("x", McpToolHandler.createParam("number", "Upper-left X coordinate in pixels", true));
    queryRegionParams.add("y", McpToolHandler.createParam("number", "Upper-left Y coordinate in pixels", true));
    queryRegionParams.add("width", McpToolHandler.createParam("number", "Bounding box width in pixels", true));
    queryRegionParams.add("height", McpToolHandler.createParam("number", "Bounding box height in pixels", true));
    queryRegionParams.add("includeEntities", McpToolHandler.createParam("boolean", "Include matching map entities (default true)", false));
    queryRegionParams.add("includeTiles", McpToolHandler.createParam("boolean", "Include matching tile grid data (default true)", false));
    queryRegionParams.add("layers", McpToolHandler.createParam("array", "Optional layer name filter list", false));
    tools.add(createToolDef(
        "query_region",
        "Single unified spatial query returning entities, tile GIDs, terrain rules, and collision bounds within a specified rectangular bounding box in pixel world coordinates. Origin (0,0) is at upper-left.",
        queryRegionParams.build(),
        true, false, true, false));

    // 4. search_entities
    JsonObjectBuilder searchEntitiesParams = Json.createObjectBuilder();
    searchEntitiesParams.add("mapId", McpToolHandler.createParam("string", "Unique map name", true));
    searchEntitiesParams.add("query", McpToolHandler.createParam("string", "Text query matching entity name, ID, or custom properties", false));
    searchEntitiesParams.add("type", McpToolHandler.createParam("string", "Entity type filter (PROP, CREATURE, LIGHTSOURCE, TRIGGER, SPAWNPOINT, AREA, COLLISIONBOX)", false));
    searchEntitiesParams.add("layer", McpToolHandler.createParam("string", "Layer name filter", false));
    tools.add(createToolDef(
        "search_entities",
        "Search entities on a specified map by name, type, layer, or custom property text. Returns explicit entity IDs for use in update_entities or delete_entities.",
        searchEntitiesParams.build(),
        true, false, true, false));

    // 5. search_tiles
    JsonObjectBuilder searchTilesParams = Json.createObjectBuilder();
    searchTilesParams.add("query", McpToolHandler.createParam("string", "Semantic text query matching tileset name or tags (e.g. 'hospital floor')", true));
    tools.add(createToolDef(
        "search_tiles",
        "Search project tilesets by semantic tags or name. Returns tile GIDs and tileset metadata for use in edit_tiles or fill_region.",
        searchTilesParams.build(),
        true, false, true, false));

    // 6. search_blueprints
    JsonObjectBuilder searchBlueprintsParams = Json.createObjectBuilder();
    searchBlueprintsParams.add("query", McpToolHandler.createParam("string", "Text query matching blueprint name or tags (e.g. 'bed')", true));
    tools.add(createToolDef(
        "search_blueprints",
        "Search project blueprint templates by name or tags. Returns template names, footprint dimensions, and default properties for use in instantiate_blueprints.",
        searchBlueprintsParams.build(),
        true, false, true, false));

    // 7. create_entities
    JsonObjectBuilder createEntitiesParams = Json.createObjectBuilder();
    createEntitiesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    createEntitiesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision for optimistic concurrency control", false));
    createEntitiesParams.add("entities", McpToolHandler.createParam("array", "Array of entity definition objects to create", true));
    tools.add(createToolDef(
        "create_entities",
        "Batch create new entities (props, creatures, lights, triggers, spawnpoints, collision boxes) on a specified map. For modifying existing entities, use update_entities; for copying existing entities, use duplicate_entities.",
        createEntitiesParams.build(),
        false, false, false, false));

    // 8. update_entities
    JsonObjectBuilder updateEntitiesParams = Json.createObjectBuilder();
    updateEntitiesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    updateEntitiesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision for optimistic concurrency control", false));
    updateEntitiesParams.add("updates", McpToolHandler.createParam("array", "Array of entity update objects containing explicit target entity ID and fields (position, size, rotation, layer, properties) to modify", true));
    tools.add(createToolDef(
        "update_entities",
        "Batch update existing entities on a specified map by explicit entity ID. Modifies position, dimensions, layer assignment, rotation, or custom properties in one operation. For creating new entities, use create_entities.",
        updateEntitiesParams.build(),
        false, false, true, false));

    // 9. duplicate_entities
    JsonObjectBuilder duplicateEntitiesParams = Json.createObjectBuilder();
    duplicateEntitiesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    duplicateEntitiesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    duplicateEntitiesParams.add("entityIds", McpToolHandler.createParam("array", "Array of source entity IDs to duplicate", true));
    duplicateEntitiesParams.add("offset", McpToolHandler.createParam("object", "Optional relative offset object with 'x' and 'y' in pixels", false));
    duplicateEntitiesParams.add("instances", McpToolHandler.createParam("array", "Optional list of target position objects [{'x': 320, 'y': 128}, ...] for array duplication", false));
    tools.add(createToolDef(
        "duplicate_entities",
        "Duplicate existing entities on a specified map. Specify target locations via a relative offset or explicit array of instance target positions. Prefer this tool over manually recreating entity definitions.",
        duplicateEntitiesParams.build(),
        false, false, false, false));

    // 10. delete_entities
    JsonObjectBuilder deleteEntitiesParams = Json.createObjectBuilder();
    deleteEntitiesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    deleteEntitiesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    deleteEntitiesParams.add("entityIds", McpToolHandler.createParam("array", "Array of explicit entity IDs or string names to remove", false));
    deleteEntitiesParams.add("names", McpToolHandler.createParam("array", "Array of entity string names to remove", false));
    deleteEntitiesParams.add("type", McpToolHandler.createParam("string", "Optional entity type filter to delete (e.g. PROP, CREATURE, TRIGGER)", false));
    deleteEntitiesParams.add("layer", McpToolHandler.createParam("string", "Optional object layer name filter to delete", false));
    deleteEntitiesParams.add("all", McpToolHandler.createParam("boolean", "If true, deletes all matching entities on the map or specified layer", false));
    tools.add(createToolDef(
        "delete_entities",
        "Delete entities from a specified map by explicit entity IDs, names, entity type, layer, or bulk clear.",
        deleteEntitiesParams.build(),
        false, true, true, false));

    // 11. instantiate_blueprints
    JsonObjectBuilder instantiateBlueprintsParams = Json.createObjectBuilder();
    instantiateBlueprintsParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    instantiateBlueprintsParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    instantiateBlueprintsParams.add("blueprintName", McpToolHandler.createParam("string", "Name of blueprint template", true));
    instantiateBlueprintsParams.add("instances", McpToolHandler.createParam("array", "Array of target position objects [{'x': 100, 'y': 200}, ...] where blueprint entities should be created", true));
    tools.add(createToolDef(
        "instantiate_blueprints",
        "Instantiate a blueprint template at one or more target positions while preserving blueprint default properties and entity compositions. Do not manually recreate blueprint entities with create_entities.",
        instantiateBlueprintsParams.build(),
        false, false, false, false));

    // 12. edit_tiles
    JsonObjectBuilder editTilesParams = Json.createObjectBuilder();
    editTilesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    editTilesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    editTilesParams.add("layer", McpToolHandler.createParam("string", "Target tile layer name", true));
    editTilesParams.add("tiles", McpToolHandler.createParam("array", "Array of tile edit objects [{'x': column, 'y': row, 'gid': tileGid}, ...]", true));
    tools.add(createToolDef(
        "edit_tiles",
        "Set individual tiles on a tile layer using grid column/row coordinates (0,0 is upper-left tile) and GID values. For filling rectangular areas, use fill_region.",
        editTilesParams.build(),
        false, false, true, false));

    // 13. fill_region
    JsonObjectBuilder fillRegionParams = Json.createObjectBuilder();
    fillRegionParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    fillRegionParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    fillRegionParams.add("layer", McpToolHandler.createParam("string", "Target tile layer name", true));
    fillRegionParams.add("x", McpToolHandler.createParam("integer", "Start column in tile grid units", true));
    fillRegionParams.add("y", McpToolHandler.createParam("integer", "Start row in tile grid units", true));
    fillRegionParams.add("width", McpToolHandler.createParam("integer", "Width in tiles", true));
    fillRegionParams.add("height", McpToolHandler.createParam("integer", "Height in tiles", true));
    fillRegionParams.add("gid", McpToolHandler.createParam("integer", "Tile GID to fill (0 to clear)", true));
    tools.add(createToolDef(
        "fill_region",
        "Fill a rectangular region on a tile layer with a specified GID. Coordinates are in tile grid units (column/row).",
        fillRegionParams.build(),
        false, false, true, false));

    // 14. paint_terrain
    JsonObjectBuilder paintTerrainParams = Json.createObjectBuilder();
    paintTerrainParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    paintTerrainParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision", false));
    paintTerrainParams.add("layer", McpToolHandler.createParam("string", "Target tile layer name", true));
    paintTerrainParams.add("ruleset", McpToolHandler.createParam("string", "Name of auto-tiling terrain ruleset", true));
    paintTerrainParams.add("x", McpToolHandler.createParam("integer", "Start column in tile grid units", true));
    paintTerrainParams.add("y", McpToolHandler.createParam("integer", "Start row in tile grid units", true));
    paintTerrainParams.add("width", McpToolHandler.createParam("integer", "Width in tiles", true));
    paintTerrainParams.add("height", McpToolHandler.createParam("integer", "Height in tiles", true));
    tools.add(createToolDef(
        "paint_terrain",
        "Paint auto-tiling terrain ruleset across a rectangular grid region on a specified tile layer.",
        paintTerrainParams.build(),
        false, false, true, false));

    // 15. render_map
    JsonObjectBuilder renderMapParams = Json.createObjectBuilder();
    renderMapParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    renderMapParams.add("scale", McpToolHandler.createParam("number", "Render scale factor (default 1.0)", false));
    tools.add(createToolDef(
        "render_map",
        "Render the full map to a PNG image. Returns base64 image data and current map revision for visual inspection.",
        renderMapParams.build(),
        true, false, true, false));

    // 16. render_region
    JsonObjectBuilder renderRegionParams = Json.createObjectBuilder();
    renderRegionParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    renderRegionParams.add("x", McpToolHandler.createParam("number", "Upper-left X coordinate in pixels", true));
    renderRegionParams.add("y", McpToolHandler.createParam("number", "Upper-left Y coordinate in pixels", true));
    renderRegionParams.add("width", McpToolHandler.createParam("number", "Region width in pixels", true));
    renderRegionParams.add("height", McpToolHandler.createParam("number", "Region height in pixels", true));
    renderRegionParams.add("scale", McpToolHandler.createParam("number", "Render scale factor (default 1.0)", false));
    tools.add(createToolDef(
        "render_region",
        "Render a cropped rectangular bounding box of the map to PNG. Use this tool after making edits to visually verify the result.",
        renderRegionParams.build(),
        true, false, true, false));

    // 17. analyze_map
    JsonObjectBuilder analyzeMapParams = Json.createObjectBuilder();
    analyzeMapParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    tools.add(createToolDef(
        "analyze_map",
        "Perform automated map validation check detecting duplicate entity IDs, out-of-bounds entities, missing sprite references, and unlinked trigger targets.",
        analyzeMapParams.build(),
        true, false, true, false));

    // 18. analyze_collision
    JsonObjectBuilder analyzeCollisionParams = Json.createObjectBuilder();
    analyzeCollisionParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    tools.add(createToolDef(
        "analyze_collision",
        "Analyze collision layout on a map to detect overlapping collision boxes, isolated collision islands, and inaccessible regions.",
        analyzeCollisionParams.build(),
        true, false, true, false));

    // 19. preview_changes
    JsonObjectBuilder previewChangesParams = Json.createObjectBuilder();
    previewChangesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    previewChangesParams.add("operations", McpToolHandler.createParam("array", "Array of mutation operation objects to dry-run validate", true));
    tools.add(createToolDef(
        "preview_changes",
        "Dry-run validate a changeset before committing. Returns predicted warnings, affected bounding box, and validation status without modifying map state.",
        previewChangesParams.build(),
        true, false, true, false));

    // 20. apply_changes
    JsonObjectBuilder applyChangesParams = Json.createObjectBuilder();
    applyChangesParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    applyChangesParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision for optimistic concurrency control", false));
    applyChangesParams.add("operations", McpToolHandler.createParam("array", "Array of mutation operation objects (createEntity, updateEntity, deleteEntity, setTile, fillRegion) to execute atomically", true));
    applyChangesParams.add("atomic", McpToolHandler.createParam("boolean", "If true, rolls back all operations if any single operation fails (default true)", false));
    tools.add(createToolDef(
        "apply_changes",
        "Execute a multi-operation changeset atomically as a single Undo step. Use this tool for large multi-step modifications to minimize tool calls, latency, and partial edits.",
        applyChangesParams.build(),
        false, false, false, false));

    // 21. set_ambient_light
    JsonObjectBuilder setAmbientLightParams = Json.createObjectBuilder();
    setAmbientLightParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    setAmbientLightParams.add("color", McpToolHandler.createParam("string", "Ambient light hex color string (e.g. '#3c0029')", true));
    setAmbientLightParams.add("alpha", McpToolHandler.createParam("integer", "Alpha opacity intensity (0-255)", false));
    setAmbientLightParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision for optimistic concurrency control", false));
    tools.add(createToolDef(
        "set_ambient_light",
        "Set map-wide ambient light color and opacity to create horror, nighttime, or atmospheric room moods.",
        setAmbientLightParams.build(),
        false, false, true, false));

    // 22. scatter_floor_details
    JsonObjectBuilder scatterFloorParams = Json.createObjectBuilder();
    scatterFloorParams.add("mapId", McpToolHandler.createParam("string", "Target map name", true));
    scatterFloorParams.add("layer", McpToolHandler.createParam("string", "Target tile layer name for detail placement", true));
    scatterFloorParams.add("x", McpToolHandler.createParam("integer", "Region start column in tiles", true));
    scatterFloorParams.add("y", McpToolHandler.createParam("integer", "Region start row in tiles", true));
    scatterFloorParams.add("width", McpToolHandler.createParam("integer", "Region width in tiles", true));
    scatterFloorParams.add("height", McpToolHandler.createParam("integer", "Region height in tiles", true));
    scatterFloorParams.add("gids", McpToolHandler.createParam("array", "Array of detail tile GIDs (rust, blood, dirt, cracks)", true));
    scatterFloorParams.add("density", McpToolHandler.createParam("number", "Scatter coverage density factor (0.01 - 1.0, default 0.15)", false));
    scatterFloorParams.add("expectedRevision", McpToolHandler.createParam("integer", "Expected map revision for optimistic concurrency control", false));
    tools.add(createToolDef(
        "scatter_floor_details",
        "Scatter floor detail tiles (blood stains, rust, grime, cracks) randomly across a region using a target coverage density.",
        scatterFloorParams.build(),
        false, false, false, false));

    // 23. analyze_project
    JsonObjectBuilder analyzeProjectParams = Json.createObjectBuilder();
    analyzeProjectParams.add("mapIds", McpToolHandler.createParam("array", "Optional list of map IDs to analyze", false));
    analyzeProjectParams.add("depth", McpToolHandler.createParam("string", "Analysis depth: quick, standard, or deep", false));
    analyzeProjectParams.add("includeRenders", McpToolHandler.createParam("boolean", "Include map renders (default true)", false));
    analyzeProjectParams.add("includeRecommendations", McpToolHandler.createParam("boolean", "Include level-design recommendations (default true)", false));
    tools.add(createToolDef(
        "analyze_project",
        "Analyzes the existing LITIENGINE project and derives its map conventions, gameplay flow, layer organization, tile vocabulary, entity patterns, collision structure, and level-design issues.",
        analyzeProjectParams.build(),
        true, false, true, false));

    // 24. plan_map_changes
    JsonObjectBuilder planMapParams = Json.createObjectBuilder();
    planMapParams.add("mapId", McpToolHandler.createParam("string", "Target map ID", true));
    planMapParams.add("goal", McpToolHandler.createParam("string", "Level-design objective or map creation goal", true));
    planMapParams.add("constraints", McpToolHandler.createParam("array", "Optional list of design constraints to preserve", false));
    tools.add(createToolDef(
        "plan_map_changes",
        "Generates a declarative, staged level-design plan (Big -> Medium -> Small) for creating or updating a map without mutating editor state.",
        planMapParams.build(),
        true, false, true, false));

    // 25. validate_map_changes
    JsonObjectBuilder validateMapParams = Json.createObjectBuilder();
    validateMapParams.add("mapId", McpToolHandler.createParam("string", "Target map ID", true));
    tools.add(createToolDef(
        "validate_map_changes",
        "Performs comprehensive level-design, structural, collision, and transition diagnostics on a map or proposed change plan.",
        validateMapParams.build(),
        true, false, true, false));

    return Json.createObjectBuilder().add("tools", tools).build();
  }

  private static JsonObject createToolDef(
      String name,
      String description,
      JsonObject parameters,
      boolean readOnly,
      boolean destructive,
      boolean idempotent,
      boolean openWorld) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("name", name);
    builder.add("description", description);

    JsonObjectBuilder schemaBuilder = Json.createObjectBuilder();
    schemaBuilder.add("type", "object");

    JsonObjectBuilder cleanedProps = Json.createObjectBuilder();
    JsonArrayBuilder requiredArr = Json.createArrayBuilder();
    if (parameters != null) {
      for (String key : parameters.keySet()) {
        JsonObject param = parameters.getJsonObject(key);
        if (param != null) {
          if (param.getBoolean("required", false)) {
            requiredArr.add(key);
          }
          JsonObjectBuilder paramBuilder = Json.createObjectBuilder();
          for (java.util.Map.Entry<String, JsonValue> entry : param.entrySet()) {
            if (!"required".equals(entry.getKey())) {
              paramBuilder.add(entry.getKey(), entry.getValue());
            }
          }
          cleanedProps.add(key, paramBuilder.build());
        }
      }
    }

    schemaBuilder.add("properties", cleanedProps.build());
    schemaBuilder.add("required", requiredArr.build());

    builder.add("inputSchema", schemaBuilder.build());

    JsonObjectBuilder hintsBuilder = Json.createObjectBuilder();
    hintsBuilder.add("readOnlyHint", readOnly);
    hintsBuilder.add("destructiveHint", destructive);
    hintsBuilder.add("idempotentHint", idempotent);
    hintsBuilder.add("openWorldHint", openWorld);
    builder.add("annotations", hintsBuilder.build());

    return builder.build();
  }

  private static JsonObject noParams() {
    return Json.createObjectBuilder().build();
  }
}
