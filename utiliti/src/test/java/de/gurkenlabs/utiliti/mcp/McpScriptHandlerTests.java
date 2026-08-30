package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.*;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class McpScriptHandlerTests {

  @TempDir
  Path tempDir;

  private ResourceBundle previousGameFile;
  private Path previousProjectPath;
  private TmxMap map;

  @BeforeEach
  void setUp() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    Path projectFile = this.tempDir.resolve("test.litidata");
    ResourceBundle project = new ResourceBundle();

    Editor editor = Editor.instance();
    Field projectPathField = Editor.class.getDeclaredField("projectPath");
    projectPathField.setAccessible(true);
    this.previousProjectPath = (Path) projectPathField.get(editor);
    projectPathField.set(editor, projectFile);

    Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    this.previousGameFile = (ResourceBundle) gameFileField.get(editor);
    gameFileField.set(editor, project);

    this.map = new TmxMap(de.gurkenlabs.litiengine.environment.tilemap.MapOrientations.ORTHOGONAL);
    this.map.setName("test-map");
    this.map.setWidth(10);
    this.map.setHeight(10);
    this.map.setTileWidth(16);
    this.map.setTileHeight(16);
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName("objects");
    MapObject mob = new MapObject();
    mob.setId(101);
    mob.setName("Goblin");
    layer.addMapObject(mob);
    this.map.addLayer(layer);

    project.getMaps().add(this.map);
    Game.world().loadEnvironment(this.map);
  }

  @AfterEach
  void tearDown() throws Exception {
    Editor editor = Editor.instance();
    Field projectPathField = Editor.class.getDeclaredField("projectPath");
    projectPathField.setAccessible(true);
    projectPathField.set(editor, this.previousProjectPath);

    Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    gameFileField.set(editor, this.previousGameFile);

    Game.scripts().detachAll();
    UndoManager.clearAll();
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void testToolDefinitionsRegistered() {
    JsonArrayBuilder toolsArr = Json.createArrayBuilder();
    McpScriptHandler.addToolDefinitions(toolsArr);
    JsonArray tools = toolsArr.build();

    assertTrue(tools.stream().anyMatch(v -> "list-scripts".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "get-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "create-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "update-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "delete-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "get-script-diagnostics".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "bind-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "unbind-script".equals(v.asJsonObject().getString("name"))));
    assertTrue(tools.stream().anyMatch(v -> "get-script-bindings".equals(v.asJsonObject().getString("name"))));
  }

  @Test
  void testCreateAndGetAndListScript() {
    JsonObject createArgs = Json.createObjectBuilder()
        .add("name", "TestMonsterAI")
        .add("host", "ENTITY")
        .add("targetType", "Creature")
        .build();

    JsonObject createResult = McpScriptHandler.handle("create-script", createArgs);
    assertTrue(createResult.getBoolean("success", false));
    assertEquals("TestMonsterAI", createResult.getString("id"));

    // List scripts
    JsonObject listResult = McpScriptHandler.handle("list-scripts", JsonValue.EMPTY_JSON_OBJECT);
    JsonArray scripts = listResult.getJsonArray("scripts");
    assertNotNull(scripts);
    assertEquals(1, scripts.size());
    JsonObject scriptItem = scripts.getJsonObject(0);
    assertEquals("TestMonsterAI", scriptItem.getString("id"));
    assertEquals("ENTITY", scriptItem.getString("host"));

    // Get script
    JsonObject getResult = McpScriptHandler.handle("get-script", Json.createObjectBuilder().add("id", "TestMonsterAI").build());
    assertEquals("TestMonsterAI", getResult.getString("id"));
    assertTrue(getResult.getString("content").contains("public class TestMonsterAI extends CreatureScript"));
  }

  @Test
  void testUpdateScript() {
    JsonObject createArgs = Json.createObjectBuilder()
        .add("name", "UpdatableScript")
        .add("host", "GAME")
        .build();
    McpScriptHandler.handle("create-script", createArgs);

    String newContent = "// updated custom script\npublic class UpdatableScript extends GameScript {}";
    JsonObject updateArgs = Json.createObjectBuilder()
        .add("id", "UpdatableScript")
        .add("content", newContent)
        .build();

    JsonObject updateResult = McpScriptHandler.handle("update-script", updateArgs);
    assertTrue(updateResult.getBoolean("success", false));

    JsonObject getResult = McpScriptHandler.handle("get-script", Json.createObjectBuilder().add("id", "UpdatableScript").build());
    assertEquals(newContent, getResult.getString("content"));
  }

  @Test
  void testDeleteScript() {
    JsonObject createArgs = Json.createObjectBuilder()
        .add("name", "DeletableScript")
        .add("host", "ENVIRONMENT")
        .build();
    McpScriptHandler.handle("create-script", createArgs);

    JsonObject deleteArgs = Json.createObjectBuilder()
        .add("id", "DeletableScript")
        .add("deleteFile", true)
        .build();
    JsonObject deleteResult = McpScriptHandler.handle("delete-script", deleteArgs);
    assertTrue(deleteResult.getBoolean("success", false));

    JsonObject listResult = McpScriptHandler.handle("list-scripts", JsonValue.EMPTY_JSON_OBJECT);
    assertEquals(0, listResult.getJsonArray("scripts").size());
  }

  @Test
  void testBindAndUnbindEntityScript() {
    JsonObject bindArgs = Json.createObjectBuilder()
        .add("script", "GoblinAI")
        .add("targetType", "entity")
        .add("targetId", "101")
        .add("enabled", true)
        .add("parameters", Json.createObjectBuilder().add("aggroRange", "150").build())
        .build();

    JsonObject bindResult = McpScriptHandler.handle("bind-script", bindArgs);
    assertTrue(bindResult.getBoolean("success", false));
    JsonArray bindings = bindResult.getJsonArray("bindings");
    assertEquals(1, bindings.size());
    assertEquals("GoblinAI", bindings.getJsonObject(0).getString("script"));
    assertEquals("150", bindings.getJsonObject(0).getJsonObject("parameters").getString("aggroRange"));

    // Verify on map object
    IMapObject mob = this.map.getMapObject(101);
    assertNotNull(mob);
    List<ScriptBinding> decoded = ScriptBindingCodec.decode(mob.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null));
    assertEquals(1, decoded.size());
    assertEquals("GoblinAI", decoded.get(0).getScript());

    // Unbind
    JsonObject unbindArgs = Json.createObjectBuilder()
        .add("script", "GoblinAI")
        .add("targetType", "entity")
        .add("targetId", "101")
        .build();
    JsonObject unbindResult = McpScriptHandler.handle("unbind-script", unbindArgs);
    assertTrue(unbindResult.getBoolean("success", false));
    assertEquals(0, unbindResult.getJsonArray("bindings").size());
  }

  @Test
  void testBindAndUnbindMapScript() {
    JsonObject bindArgs = Json.createObjectBuilder()
        .add("script", "DungeonWaves")
        .add("targetType", "map")
        .add("enabled", true)
        .build();

    JsonObject bindResult = McpScriptHandler.handle("bind-script", bindArgs);
    assertTrue(bindResult.getBoolean("success", false));

    JsonObject getBindingsArgs = Json.createObjectBuilder()
        .add("targetType", "map")
        .build();
    JsonObject getResult = McpScriptHandler.handle("get-script-bindings", getBindingsArgs);
    assertEquals(1, getResult.getJsonArray("bindings").size());
    assertEquals("DungeonWaves", getResult.getJsonArray("bindings").getJsonObject(0).getString("script"));

    JsonObject unbindArgs = Json.createObjectBuilder()
        .add("script", "DungeonWaves")
        .add("targetType", "map")
        .build();
    JsonObject unbindResult = McpScriptHandler.handle("unbind-script", unbindArgs);
    assertTrue(unbindResult.getBoolean("success", false));
    assertEquals(0, unbindResult.getJsonArray("bindings").size());
  }

  @Test
  void testResourcesAndPrompts() {
    // Resources
    JsonObject scriptsResource = McpResourceHandler.handleReadResource("uti://project/scripts");
    assertNotNull(scriptsResource);
    assertTrue(scriptsResource.containsKey("scripts"));

    JsonObject diagResource = McpResourceHandler.handleReadResource("uti://project/scripts/diagnostics");
    assertNotNull(diagResource);
    assertTrue(diagResource.containsKey("diagnostics"));

    JsonObject gameBindingsResource = McpResourceHandler.handleReadResource("uti://project/scripts/game-bindings");
    assertNotNull(gameBindingsResource);
    assertTrue(gameBindingsResource.containsKey("bindings"));

    // Prompts
    JsonObject createPrompt = McpPromptHandler.handleGetPrompt("create_litiengine_script");
    assertNotNull(createPrompt);
    assertTrue(createPrompt.containsKey("messages"));

    JsonObject debugPrompt = McpPromptHandler.handleGetPrompt("debug_litiengine_script");
    assertNotNull(debugPrompt);
    assertTrue(debugPrompt.containsKey("messages"));
  }
}
