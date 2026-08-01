package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.utiliti.controller.Editor;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

class McpSemanticToolsTest {

  @Test
  void getSemanticToolsListContainsAll33Tools() {
    JsonObject toolsList = McpSemanticToolRegistry.getSemanticToolsList();
    assertNotNull(toolsList);
    JsonArray tools = toolsList.getJsonArray("tools");
    assertNotNull(tools);
    assertEquals(33, tools.size(), "Level A API must define 33 semantic tools");
  }

  @Test
  void semanticToolsSchemasAreValid() {
    JsonObject toolsList = McpSemanticToolRegistry.getSemanticToolsList();
    JsonArray tools = toolsList.getJsonArray("tools");

    for (int i = 0; i < tools.size(); i++) {
      JsonObject tool = tools.getJsonObject(i);
      assertTrue(tool.containsKey("name"), "Tool must have a name");
      assertTrue(tool.containsKey("description"), "Tool must have a description");
      assertTrue(tool.containsKey("inputSchema"), "Tool must have an inputSchema");
      assertTrue(tool.containsKey("annotations"), "Tool must have annotations");

      JsonObject schema = tool.getJsonObject("inputSchema");
      assertEquals("object", schema.getString("type"));
      assertTrue(schema.containsKey("properties"));
      assertTrue(schema.containsKey("required"));

      // Verify required is an array and properties do not contain 'required' inside
      JsonObject properties = schema.getJsonObject("properties");
      for (String propName : properties.keySet()) {
        JsonObject prop = properties.getJsonObject(propName);
        assertFalse(prop.containsKey("required"),
            "Property " + propName + " in tool " + tool.getString("name") + " should not contain inner 'required'");
      }
    }
  }

  @Test
  void getProjectContextReturnsValidJson() {
    JsonObject response = McpSemanticHandler.handleSemanticTool("get_project_context", null);
    assertNotNull(response);
    assertTrue(response.getBoolean("success", false));
    assertTrue(response.containsKey("maps"));
    assertTrue(response.containsKey("tilesets"));
    assertTrue(response.containsKey("blueprints"));
    assertTrue(response.containsKey("materials"));
    assertTrue(response.containsKey("spritesheets"));
  }

  @Test
  void handlesUnknownToolGracefully() {
    JsonObject response = McpSemanticHandler.handleSemanticTool("unknown_semantic_tool", null);
    assertNotNull(response);
    assertFalse(response.getBoolean("success", true));
    assertTrue(response.containsKey("error"));
    JsonObject error = response.getJsonObject("error");
    assertEquals("UNKNOWN_TOOL", error.getString("code", ""));
  }

  @Test
  void handlesUnloadedMapGracefully() {
    JsonObject args = Json.createObjectBuilder()
        .add("mapId", "non_existent_map_12345")
        .build();
    JsonObject response = McpSemanticHandler.handleSemanticTool("get_map", args);
    assertNotNull(response);
    assertFalse(response.getBoolean("success", true));
    assertTrue(response.containsKey("error"));
    JsonObject error = response.getJsonObject("error");
    assertEquals("MAP_NOT_FOUND", error.getString("code", ""));
  }

  @Test
  void explicitUnknownMapIdDoesNotFallBackToTheActiveMap() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("known-map");
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool("fill_regions",
          Json.createObjectBuilder().add("mapId", "typo-map").add("regions", Json.createArrayBuilder()).build());
      assertFalse(response.getBoolean("success", true));
      assertEquals("MAP_NOT_FOUND", response.getJsonObject("error").getString("code"));
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void applyChangesReportsThatOperationsAreNotImplemented() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("known-map");
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool("apply_changes",
          Json.createObjectBuilder().add("mapId", "known-map").add("operations", Json.createArrayBuilder()).build());
      assertFalse(response.getBoolean("success", true));
      assertEquals("NOT_IMPLEMENTED", response.getJsonObject("error").getString("code"));
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void responseFactoryCreatesStandardizedError() {
    JsonObject res = McpResponseFactory.createError(
        "REVISION_CONFLICT", "Revision mismatch", true, null);
    assertNotNull(res);
    assertFalse(res.getBoolean("success", true));
    assertTrue(res.containsKey("error"));
    JsonObject errObj = res.getJsonObject("error");
    assertEquals("REVISION_CONFLICT", errObj.getString("code"));
    assertEquals("Revision mismatch", errObj.getString("message"));
    assertTrue(errObj.getBoolean("recoverable"));
  }

  @Test
  void revisionConflictReturnsMachineReadableRecoveryDetails() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("revision-map");
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      McpRevisionTracker.incrementRevision(map);
      long actualRevision = McpRevisionTracker.getRevision(map);
      JsonObject response = McpSemanticHandler.handleSemanticTool("set_ambient_light",
          Json.createObjectBuilder().add("mapId", "revision-map").add("color", "#112233")
              .add("expectedRevision", actualRevision - 1).build());
      JsonObject details = response.getJsonObject("error").getJsonObject("details");
      assertEquals(actualRevision - 1, details.getJsonNumber("expectedRevision").longValue());
      assertEquals(actualRevision, details.getJsonNumber("actualRevision").longValue());
      assertTrue(details.getString("recovery").contains("expectedRevision"));
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void deleteEntitiesSchemaSupportsExtendedParameters() {
    JsonObject toolsList = McpSemanticToolRegistry.getSemanticToolsList();
    JsonArray tools = toolsList.getJsonArray("tools");
    JsonObject deleteTool = null;
    for (int i = 0; i < tools.size(); i++) {
      if ("delete_entities".equals(tools.getJsonObject(i).getString("name"))) {
        deleteTool = tools.getJsonObject(i);
        break;
      }
    }
    assertNotNull(deleteTool, "delete_entities tool must be registered");
    JsonObject props = deleteTool.getJsonObject("inputSchema").getJsonObject("properties");
    assertTrue(props.containsKey("mapId"));
    assertTrue(props.containsKey("entityIds"));
    assertTrue(props.containsKey("names"));
    assertTrue(props.containsKey("type"));
    assertTrue(props.containsKey("layer"));
    assertTrue(props.containsKey("all"));
  }

  @Test
  void deleteEntitiesHandlesNonExistentMapGracefully() {
    JsonObject args = Json.createObjectBuilder()
        .add("mapId", "non_existent_map_xyz")
        .add("names", Json.createArrayBuilder().add("test_entity_1"))
        .build();
    JsonObject response = McpSemanticHandler.handleSemanticTool("delete_entities", args);
    assertNotNull(response);
    assertFalse(response.getBoolean("success", true));
    assertTrue(response.containsKey("error"));
    JsonObject error = response.getJsonObject("error");
    assertEquals("MAP_NOT_FOUND", error.getString("code", ""));
  }

  @Test
  void analyzeProjectHandlesToolExecution() {
    JsonObject response = McpSemanticHandler.handleSemanticTool("analyze_project", Json.createObjectBuilder().build());
    assertNotNull(response);
    assertTrue(response.getBoolean("success", false));
  }

  @Test
  void analyzeProjectAcceptsTriggersWithoutOptionalTargetProperties() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("test-map");
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(new MapObject("TRIGGER"));
    map.addLayer(layer);
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool(
          "analyze_project", Json.createObjectBuilder().build());
      assertTrue(response.getBoolean("success", false));
      assertEquals(1, response.getJsonArray("maps").size());
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void createEntitiesRejectsStringEntriesWithoutAnInternalError() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("test-map");
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool("create_entities",
          Json.createObjectBuilder().add("mapId", "test-map")
              .add("entities", Json.createArrayBuilder().add("not an entity object")).build());
      assertFalse(response.getBoolean("success", true));
      assertEquals("INVALID_ARGUMENTS", response.getJsonObject("error").getString("code"));
      assertTrue(response.getJsonObject("error").getString("message").contains("index 0"));
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void createEntitiesParsesJsonEncodedEntityDefinitions() {
    JsonObject definition = McpSemanticHandler.parseEntityDefinition(
        Json.createValue("{\"type\":\"PROP\",\"name\":\"crate\",\"x\":16,\"y\":32}"));

    assertNotNull(definition);
    assertEquals("PROP", definition.getString("type"));
    assertEquals("crate", definition.getString("name"));
    assertEquals(16, definition.getInt("x"));
  }

  @Test
  void createEntitiesPreservesNestedEntityProperties() {
    MapObject entity = new MapObject();
    McpToolHandler.applyAdditionalProperties(entity, Json.createObjectBuilder()
        .add("spritesheetName", "bench1")
        .add("material", "metal")
        .add("customFlag", true)
        .build());

    assertEquals("bench1", entity.getStringValue(MapObjectProperty.SPRITESHEETNAME));
    assertEquals("metal", entity.getStringValue(MapObjectProperty.PROP_MATERIAL));
    assertTrue(entity.getBoolValue("customFlag"));
  }

  @Test
  void fillRegionsAppliesMultipleLayersInOneRevision() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("test-map");
    map.setWidth(8);
    map.setHeight(8);
    TileLayer base = new TileLayer(8, 8);
    base.setName("base");
    TileLayer overlay = new TileLayer(8, 8);
    overlay.setName("overlay");
    map.addLayer(base);
    map.addLayer(overlay);
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool("fill_regions",
          Json.createObjectBuilder().add("mapId", "test-map")
              .add("regions", Json.createArrayBuilder()
                  .add(Json.createObjectBuilder().add("layer", "base").add("x", 0).add("y", 0)
                      .add("width", 2).add("height", 2).add("gid", 3))
                  .add(Json.createObjectBuilder().add("layer", "overlay").add("x", 1).add("y", 1)
                      .add("width", 1).add("height", 2).add("gid", 5)))
              .build());
      assertTrue(response.getBoolean("success", false), response::toString);
      assertEquals(2, response.getInt("regionCount"));
      assertEquals(6, response.getInt("tileCount"));
      assertEquals(3, base.getTile(0, 0).getGridId());
      assertEquals(5, overlay.getTile(1, 2).getGridId());
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void analyzePlayabilityReportsMissingSpawnAsHardFailure() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("test-map");
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpSemanticHandler.handleSemanticTool("analyze_playability",
          Json.createObjectBuilder().add("mapId", "test-map")
              .add("actorProfile", Json.createObjectBuilder().add("width", 16).add("height", 16)).build());
      assertTrue(response.getBoolean("success", false));
      assertEquals("FAIL", response.getString("status"));
      assertEquals("NO_PLAYER_SPAWN", response.getJsonArray("hardFailures")
          .getJsonObject(0).getString("code"));
    } finally {
      gameFileField.set(editor, previous);
    }
  }

  @Test
  void planMapChangesReturnsDeclarativePlan() {
    JsonObject args = Json.createObjectBuilder()
        .add("mapId", "test_map")
        .add("goal", "Create hospital corridor")
        .build();
    JsonObject response = McpSemanticHandler.handleSemanticTool("plan_map_changes", args);
    assertNotNull(response);
    assertTrue(response.getBoolean("success", false));
    assertTrue(response.containsKey("criticalPath"));
    assertTrue(response.containsKey("validationChecks"));
  }

  @Test
  void validateMapChangesHandlesUnloadedMapGracefully() {
    JsonObject args = Json.createObjectBuilder()
        .add("mapId", "non_existent_map_xyz")
        .build();
    JsonObject response = McpSemanticHandler.handleSemanticTool("validate_map_changes", args);
    assertNotNull(response);
    assertFalse(response.getBoolean("success", true));
    assertTrue(response.containsKey("error"));
  }
}
