package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

class McpSemanticToolsTest {

  @Test
  void getSemanticToolsListContainsAll25Tools() {
    JsonObject toolsList = McpSemanticToolRegistry.getSemanticToolsList();
    assertNotNull(toolsList);
    JsonArray tools = toolsList.getJsonArray("tools");
    assertNotNull(tools);
    assertEquals(25, tools.size(), "Level A API must define 25 semantic tools");
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
