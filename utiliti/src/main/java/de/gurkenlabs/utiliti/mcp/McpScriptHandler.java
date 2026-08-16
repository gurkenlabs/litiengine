package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptManager;
import de.gurkenlabs.litiengine.scripting.ScriptPropertyMetadata;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ScriptSourcePaths;
import de.gurkenlabs.utiliti.controller.ScriptTemplateFactory;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.view.components.UI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Domain handler for MCP scripting tools.
 */
final class McpScriptHandler {
  private static final Set<String> TOOLS = Set.of(
      "list-scripts",
      "get-script",
      "create-script",
      "update-script",
      "delete-script",
      "get-script-diagnostics",
      "bind-script",
      "unbind-script",
      "get-script-bindings");

  private McpScriptHandler() {
    throw new UnsupportedOperationException();
  }

  static void addToolDefinitions(JsonArrayBuilder tools) {
    // 1. list-scripts
    JsonObject listParams = Json.createObjectBuilder()
        .add("host", McpToolHandler.createParam("string", "Filter by host type: GAME, ENVIRONMENT, or ENTITY", false))
        .add("query", McpToolHandler.createParam("string", "Search query filtering name, id, or target type", false))
        .build();
    tools.add(McpToolHandler.createToolDef("list-scripts", "List all script definitions in the active project", listParams));

    // 2. get-script
    JsonObject getParams = Json.createObjectBuilder()
        .add("id", McpToolHandler.createParam("string", "Script ID or class name", true))
        .build();
    tools.add(McpToolHandler.createToolDef("get-script", "Get script metadata and source code text", getParams));

    // 3. create-script
    JsonObject createParams = Json.createObjectBuilder()
        .add("name", McpToolHandler.createParam("string", "Class name of the script (e.g. BossBehavior)", true))
        .add("host", McpToolHandler.createParam("string", "Host type: GAME, ENVIRONMENT, or ENTITY (default ENTITY)", false))
        .add("targetType", McpToolHandler.createParam("string", "Target entity type (e.g. Creature, Prop; default Creature)", false))
        .add("content", McpToolHandler.createParam("string", "Optional custom source code. If omitted, template code is generated.", false))
        .add("package", McpToolHandler.createParam("string", "Optional Java package name", false))
        .build();
    tools.add(McpToolHandler.createToolDef("create-script", "Create a new script file in the project workspace", createParams));

    // 4. update-script
    JsonObject updateParams = Json.createObjectBuilder()
        .add("id", McpToolHandler.createParam("string", "Script ID or class name to update", true))
        .add("content", McpToolHandler.createParam("string", "New source code content", true))
        .build();
    tools.add(McpToolHandler.createToolDef("update-script", "Update script source code on disk and trigger compilation check", updateParams));

    // 5. delete-script
    JsonObject deleteParams = Json.createObjectBuilder()
        .add("id", McpToolHandler.createParam("string", "Script ID or class name to delete", true))
        .add("deleteFile", McpToolHandler.createParam("boolean", "Whether to delete the source file from disk (default true)", false))
        .build();
    tools.add(McpToolHandler.createToolDef("delete-script", "Delete a script definition and source file from the project", deleteParams));

    // 6. get-script-diagnostics
    JsonObject diagParams = Json.createObjectBuilder()
        .add("id", McpToolHandler.createParam("string", "Optional script ID to filter diagnostics for", false))
        .build();
    tools.add(McpToolHandler.createToolDef("get-script-diagnostics", "Get compiler errors and warnings for project scripts", diagParams));

    // 7. bind-script
    JsonObject bindParams = Json.createObjectBuilder()
        .add("script", McpToolHandler.createParam("string", "Script ID to bind", true))
        .add("targetType", McpToolHandler.createParam("string", "Binding target: 'entity', 'map', or 'game'", true))
        .add("targetId", McpToolHandler.createParam("string", "Entity ID (if targetType='entity') or Map name (if targetType='map')", false))
        .add("enabled", McpToolHandler.createParam("boolean", "Whether the script binding is enabled (default true)", false))
        .add("order", McpToolHandler.createParam("integer", "Execution order / priority (default -1 to append)", false))
        .add("parameters", Json.createObjectBuilder().add("type", "object").add("description", "Key-value map of @ScriptProperty parameter overrides").build())
        .build();
    tools.add(McpToolHandler.createToolDef("bind-script", "Attach a script to an entity, map environment, or game orchestrator", bindParams));

    // 8. unbind-script
    JsonObject unbindParams = Json.createObjectBuilder()
        .add("script", McpToolHandler.createParam("string", "Script ID to unbind", true))
        .add("targetType", McpToolHandler.createParam("string", "Binding target: 'entity', 'map', or 'game'", true))
        .add("targetId", McpToolHandler.createParam("string", "Entity ID or Map name", false))
        .build();
    tools.add(McpToolHandler.createToolDef("unbind-script", "Remove a script binding from an entity, map environment, or game", unbindParams));

    // 9. get-script-bindings
    JsonObject getBindingsParams = Json.createObjectBuilder()
        .add("targetType", McpToolHandler.createParam("string", "Binding target: 'entity', 'map', or 'game'", true))
        .add("targetId", McpToolHandler.createParam("string", "Entity ID or Map name", false))
        .build();
    tools.add(McpToolHandler.createToolDef("get-script-bindings", "List all attached script bindings for an entity, map, or game", getBindingsParams));
  }

  static boolean handles(String toolName) {
    return TOOLS.contains(toolName);
  }

  static JsonObject handle(String name, JsonObject args) {
    return switch (name) {
      case "list-scripts" -> listScripts(args);
      case "get-script" -> getScript(args);
      case "create-script" -> createScript(args);
      case "update-script" -> updateScript(args);
      case "delete-script" -> deleteScript(args);
      case "get-script-diagnostics" -> getScriptDiagnostics(args);
      case "bind-script" -> bindScript(args);
      case "unbind-script" -> unbindScript(args);
      case "get-script-bindings" -> getScriptBindings(args);
      default -> error("Unknown script tool: " + name);
    };
  }

  // ---- TOOL IMPLEMENTATIONS ----

  private static JsonObject listScripts(JsonObject args) {
    if (Editor.instance().getGameFile() == null) {
      return error("No project is currently loaded in utiLITI.");
    }
    String hostFilter = McpToolHandler.getString(args, "host", null);
    String query = McpToolHandler.getString(args, "query", "").toLowerCase(Locale.ROOT).trim();

    JsonArrayBuilder arr = Json.createArrayBuilder();
    for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
      if (hostFilter != null && !hostFilter.isBlank()) {
        if (def.getHost() == null || !def.getHost().name().equalsIgnoreCase(hostFilter)) {
          continue;
        }
      }
      if (!query.isEmpty()) {
        boolean match = (def.getId() != null && def.getId().toLowerCase(Locale.ROOT).contains(query))
            || (def.getName() != null && def.getName().toLowerCase(Locale.ROOT).contains(query))
            || (def.getSource() != null && def.getSource().toLowerCase(Locale.ROOT).contains(query))
            || (def.getTargetType() != null && def.getTargetType().toLowerCase(Locale.ROOT).contains(query));
        if (!match) continue;
      }
      arr.add(scriptToJson(def));
    }
    return Json.createObjectBuilder().add("scripts", arr).build();
  }

  private static JsonObject getScript(JsonObject args) {
    if (Editor.instance().getGameFile() == null) {
      return error("No project is currently loaded.");
    }
    String id = McpToolHandler.getString(args, "id", "").trim();
    if (id.isEmpty()) {
      return error("Missing required 'id' parameter.");
    }
    ScriptDefinition def = findDefinition(id);
    if (def == null) {
      return error("Script not found: " + id);
    }

    String content = readScriptSource(def);
    JsonObjectBuilder builder = Json.createObjectBuilder(scriptToJson(def))
        .add("content", content);
    return builder.build();
  }

  private static JsonObject createScript(JsonObject args) {
    if (Editor.instance().getGameFile() == null) {
      return error("No project is currently loaded.");
    }
    String name = McpToolHandler.getString(args, "name", "").trim();
    if (name.isEmpty() || !name.matches("[A-Za-z_$][\\w$]*")) {
      return error("Invalid script class name: " + name);
    }
    String hostStr = McpToolHandler.getString(args, "host", "ENTITY").toUpperCase(Locale.ROOT);
    ScriptHostType host = parseHostType(hostStr);
    String targetType = McpToolHandler.getString(args, "targetType", null);
    if (targetType == null && host == ScriptHostType.ENTITY) {
      targetType = "de.gurkenlabs.litiengine.entities.Creature";
    }

    String scriptId = name;
    if (findDefinition(scriptId) != null) {
      return error("Script already exists with ID: " + scriptId);
    }

    Path scriptsDir = resolveScriptsDirectory();
    if (scriptsDir == null) {
      return error("Could not resolve project scripts directory.");
    }

    try {
      Files.createDirectories(scriptsDir);
      Path filePath = scriptsDir.resolve(name + ".java");
      String content = McpToolHandler.getString(args, "content", null);
      if (content == null || content.isBlank()) {
        content = generateTemplateCode(name, host, targetType, McpToolHandler.getString(args, "package", null));
      }
      Files.writeString(filePath, content, StandardCharsets.UTF_8);

      Path projectRoot = Editor.instance().getProjectPath().toAbsolutePath().normalize().getParent();
      String relativeSource = projectRoot != null ? projectRoot.relativize(filePath).toString().replace('\\', '/') : filePath.toString();

      ScriptDefinition def = new ScriptDefinition();
      def.setId(scriptId);
      def.setName(name);
      def.setHost(host);
      def.setLanguage("java");
      def.setSource(relativeSource);
      def.setTargetType(targetType);

      Editor.instance().getGameFile().getScripts().add(def);
      UndoManager.instance().recordChanges();

      if (UI.getScriptWorkspacePanel() != null) {
        UI.getScriptWorkspacePanel().refreshScripts();
        UI.openScript(def);
      }

      return Json.createObjectBuilder()
          .add("success", true)
          .add("id", scriptId)
          .add("source", relativeSource)
          .add("message", "Script " + name + " created successfully.")
          .build();
    } catch (IOException error) {
      return error("Failed to create script file: " + error.getMessage());
    }
  }

  private static JsonObject updateScript(JsonObject args) {
    if (Editor.instance().getGameFile() == null) {
      return error("No project is currently loaded.");
    }
    String id = McpToolHandler.getString(args, "id", "").trim();
    String content = McpToolHandler.getString(args, "content", null);
    if (id.isEmpty() || content == null) {
      return error("Missing required 'id' or 'content' parameter.");
    }

    ScriptDefinition def = findDefinition(id);
    if (def == null) {
      return error("Script not found: " + id);
    }

    Path path = resolvePath(def.getSource());
    if (path == null) {
      return error("Source file not found on disk: " + def.getSource());
    }

    try {
      Files.writeString(path, content, StandardCharsets.UTF_8);

      if (UI.getScriptWorkspacePanel() != null) {
        UI.getScriptWorkspacePanel().reloadTab(def);
        UI.getScriptWorkspacePanel().refreshProblemsTable();
      }

      // Compile and reload
      Game.scripts().reload(def.getId());

      JsonArrayBuilder diagArr = Json.createArrayBuilder();
      for (ScriptDiagnostic diag : Game.scripts().getDiagnostics()) {
        if (Objects.equals(diag.scriptId(), def.getId())) {
          diagArr.add(diagnosticToJson(diag));
        }
      }

      return Json.createObjectBuilder()
          .add("success", true)
          .add("id", def.getId())
          .add("diagnostics", diagArr)
          .add("message", "Script updated successfully.")
          .build();
    } catch (IOException error) {
      return error("Failed to write script: " + error.getMessage());
    }
  }

  private static JsonObject deleteScript(JsonObject args) {
    if (Editor.instance().getGameFile() == null) {
      return error("No project is currently loaded.");
    }
    String id = McpToolHandler.getString(args, "id", "").trim();
    if (id.isEmpty()) {
      return error("Missing required 'id' parameter.");
    }

    ScriptDefinition def = findDefinition(id);
    if (def == null) {
      return error("Script not found: " + id);
    }

    boolean deleteFile = McpToolHandler.getBoolean(args, "deleteFile", true);
    if (deleteFile && def.getSource() != null) {
      Path path = resolvePath(def.getSource());
      if (path != null && Files.exists(path)) {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {}
      }
    }

    Editor.instance().getGameFile().getScripts().remove(def);
    UndoManager.instance().recordChanges();

    if (UI.getScriptWorkspacePanel() != null) {
      UI.getScriptWorkspacePanel().closeTab(def);
      UI.getScriptWorkspacePanel().refreshScripts();
    }

    return Json.createObjectBuilder()
        .add("success", true)
        .add("id", id)
        .add("message", "Script deleted successfully.")
        .build();
  }

  private static JsonObject getScriptDiagnostics(JsonObject args) {
    String filterId = McpToolHandler.getString(args, "id", null);
    JsonArrayBuilder arr = Json.createArrayBuilder();
    for (ScriptDiagnostic diag : Game.scripts().getDiagnostics()) {
      if (filterId == null || filterId.isBlank() || Objects.equals(diag.scriptId(), filterId)) {
        arr.add(diagnosticToJson(diag));
      }
    }
    return Json.createObjectBuilder().add("diagnostics", arr).build();
  }

  private static JsonObject bindScript(JsonObject args) {
    String script = McpToolHandler.getString(args, "script", "").trim();
    String targetType = McpToolHandler.getString(args, "targetType", "entity").toLowerCase(Locale.ROOT);
    String targetId = McpToolHandler.getString(args, "targetId", null);
    boolean enabled = McpToolHandler.getBoolean(args, "enabled", true);
    int order = McpToolHandler.getInt(args, "order", -1);
    JsonObject params = args.containsKey("parameters") && args.get("parameters") instanceof JsonObject
        ? args.getJsonObject("parameters") : null;

    if (script.isEmpty()) {
      return error("Missing required 'script' parameter.");
    }

    ScriptBinding binding = new ScriptBinding(script, enabled);
    if (order >= 0) binding.setOrder(order);
    if (params != null) {
      for (String key : params.keySet()) {
        binding.setParameter(key, params.getString(key, ""));
      }
    }

    if ("entity".equals(targetType)) {
      if (targetId == null || targetId.isBlank()) {
        return error("Target ID (entity ID) is required for entity script binding.");
      }
      int entityId;
      try {
        entityId = Integer.parseInt(targetId);
      } catch (NumberFormatException e) {
        return error("Invalid entity ID: " + targetId);
      }
      IMap currentMap = getActiveMap();
      if (currentMap == null) return error("No active map loaded.");
      IMapObject mapObj = currentMap.getMapObject(entityId);
      if (mapObj == null) return error("Entity with ID " + entityId + " not found on active map.");

      List<ScriptBinding> bindings = new ArrayList<>(ScriptBindingCodec.decode(mapObj.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null)));
      bindings.removeIf(b -> b.getScript().equals(script));
      if (order >= 0 && order < bindings.size()) bindings.add(order, binding);
      else bindings.add(binding);

      UndoManager.instance().mapObjectChanging(mapObj);
      mapObj.setValue(MapObjectProperty.SCRIPT_BINDINGS, ScriptBindingCodec.encode(bindings));
      UndoManager.instance().mapObjectChanged(mapObj);

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();

    } else if ("map".equals(targetType) || "environment".equals(targetType)) {
      IMap map = getActiveMap();
      if (map == null) return error("No active map loaded.");

      List<ScriptBinding> bindings = new ArrayList<>(ScriptBindingCodec.decode(map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null)));
      bindings.removeIf(b -> b.getScript().equals(script));
      if (order >= 0 && order < bindings.size()) bindings.add(order, binding);
      else bindings.add(binding);

      UndoManager.instance().mapChanging(map);
      map.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(bindings));
      UndoManager.instance().mapChanged(map);

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();

    } else if ("game".equals(targetType)) {
      if (Editor.instance().getGameFile() == null) return error("No project loaded.");

      List<ScriptBinding> bindings = new ArrayList<>(Game.scripts().getGameBindings());
      bindings.removeIf(b -> b.getScript().equals(script));
      if (order >= 0 && order < bindings.size()) bindings.add(order, binding);
      else bindings.add(binding);

      Game.scripts().setGameBindings(bindings);
      Editor.instance().getGameFile().getGameScripts().clear();
      Editor.instance().getGameFile().getGameScripts().addAll(bindings);
      UndoManager.instance().recordChanges();

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();
    }

    return error("Unsupported targetType: " + targetType);
  }

  private static JsonObject unbindScript(JsonObject args) {
    String script = McpToolHandler.getString(args, "script", "").trim();
    String targetType = McpToolHandler.getString(args, "targetType", "entity").toLowerCase(Locale.ROOT);
    String targetId = McpToolHandler.getString(args, "targetId", null);

    if (script.isEmpty()) return error("Missing required 'script' parameter.");

    if ("entity".equals(targetType)) {
      if (targetId == null) return error("Missing entity targetId.");
      IMap currentMap = getActiveMap();
      if (currentMap == null) return error("No active map loaded.");
      IMapObject mapObj = currentMap.getMapObject(Integer.parseInt(targetId));
      if (mapObj == null) return error("Entity " + targetId + " not found.");

      List<ScriptBinding> bindings = new ArrayList<>(ScriptBindingCodec.decode(mapObj.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null)));
      bindings.removeIf(b -> b.getScript().equals(script));

      UndoManager.instance().mapObjectChanging(mapObj);
      if (bindings.isEmpty()) mapObj.removeProperty(MapObjectProperty.SCRIPT_BINDINGS);
      else mapObj.setValue(MapObjectProperty.SCRIPT_BINDINGS, ScriptBindingCodec.encode(bindings));
      UndoManager.instance().mapObjectChanged(mapObj);

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();

    } else if ("map".equals(targetType) || "environment".equals(targetType)) {
      IMap map = getActiveMap();
      if (map == null) return error("No active map loaded.");

      List<ScriptBinding> bindings = new ArrayList<>(ScriptBindingCodec.decode(map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null)));
      bindings.removeIf(b -> b.getScript().equals(script));

      UndoManager.instance().mapChanging(map);
      if (bindings.isEmpty()) map.removeProperty(ScriptManager.BINDINGS_PROPERTY);
      else map.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(bindings));
      UndoManager.instance().mapChanged(map);

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();

    } else if ("game".equals(targetType)) {
      if (Editor.instance().getGameFile() == null) return error("No project loaded.");

      List<ScriptBinding> bindings = new ArrayList<>(Game.scripts().getGameBindings());
      bindings.removeIf(b -> b.getScript().equals(script));

      Game.scripts().setGameBindings(bindings);
      Editor.instance().getGameFile().getGameScripts().clear();
      Editor.instance().getGameFile().getGameScripts().addAll(bindings);
      UndoManager.instance().recordChanges();

      return Json.createObjectBuilder().add("success", true).add("bindings", bindingsToJson(bindings)).build();
    }

    return error("Unsupported targetType: " + targetType);
  }

  private static JsonObject getScriptBindings(JsonObject args) {
    String targetType = McpToolHandler.getString(args, "targetType", "game").toLowerCase(Locale.ROOT);
    String targetId = McpToolHandler.getString(args, "targetId", null);

    if ("entity".equals(targetType)) {
      if (targetId == null) return error("Missing entity targetId.");
      IMap currentMap = getActiveMap();
      if (currentMap == null) return error("No active map loaded.");
      IMapObject mapObj = currentMap.getMapObject(Integer.parseInt(targetId));
      if (mapObj == null) return error("Entity " + targetId + " not found.");

      List<ScriptBinding> bindings = ScriptBindingCodec.decode(mapObj.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null));
      return Json.createObjectBuilder().add("bindings", bindingsToJson(bindings)).build();

    } else if ("map".equals(targetType) || "environment".equals(targetType)) {
      IMap map = getActiveMap();
      if (map == null) return error("No active map loaded.");

      List<ScriptBinding> bindings = ScriptBindingCodec.decode(map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
      return Json.createObjectBuilder().add("bindings", bindingsToJson(bindings)).build();

    } else if ("game".equals(targetType)) {
      List<ScriptBinding> bindings = Game.scripts().getGameBindings();
      return Json.createObjectBuilder().add("bindings", bindingsToJson(bindings)).build();
    }

    return error("Unsupported targetType: " + targetType);
  }

  // ---- HELPER METHODS ----

  private static JsonObject error(String message) {
    return Json.createObjectBuilder().add("success", false).add("error", message).build();
  }

  private static IMap getActiveMap() {
    return Game.world().environment() != null ? Game.world().environment().getMap() : null;
  }

  static JsonObject scriptToJson(ScriptDefinition def) {
    JsonObjectBuilder b = Json.createObjectBuilder()
        .add("id", def.getId() != null ? def.getId() : "")
        .add("name", def.getName() != null ? def.getName() : "")
        .add("source", def.getSource() != null ? def.getSource() : "")
        .add("host", def.getHost() != null ? def.getHost().name() : "ENTITY")
        .add("language", def.getLanguage() != null ? def.getLanguage() : "java");
    if (def.getTargetType() != null) b.add("targetType", def.getTargetType());

    // Properties metadata
    JsonArrayBuilder props = Json.createArrayBuilder();
    for (ScriptPropertyMetadata prop : Game.scripts().getPropertyMetadata(def.getId())) {
      props.add(Json.createObjectBuilder()
          .add("name", prop.name())
          .add("type", prop.type() != null ? prop.type() : "String")
          .add("description", prop.description() != null ? prop.description() : "")
          .add("defaultValue", prop.defaultValue() != null ? prop.defaultValue() : ""));
    }
    b.add("properties", props);

    // Diagnostics count
    long diagCount = Game.scripts().getDiagnostics().stream()
        .filter(d -> Objects.equals(d.scriptId(), def.getId())).count();
    b.add("diagnosticsCount", diagCount);

    return b.build();
  }

  static JsonObject diagnosticToJson(ScriptDiagnostic diag) {
    JsonObjectBuilder b = Json.createObjectBuilder()
        .add("severity", diag.severity() != null ? diag.severity().name() : "INFO")
        .add("message", diag.message() != null ? diag.message() : "")
        .add("line", diag.line())
        .add("column", diag.column());
    if (diag.scriptId() != null) b.add("scriptId", diag.scriptId());
    if (diag.source() != null) b.add("source", diag.source());
    return b.build();
  }

  private static JsonArrayBuilder bindingsToJson(List<ScriptBinding> bindings) {
    JsonArrayBuilder arr = Json.createArrayBuilder();
    for (ScriptBinding b : bindings) {
      JsonObjectBuilder obj = Json.createObjectBuilder()
          .add("script", b.getScript())
          .add("enabled", b.isEnabled())
          .add("order", b.getOrder());
      JsonObjectBuilder params = Json.createObjectBuilder();
      for (Map.Entry<String, String> entry : b.getParameters().entrySet()) {
        params.add(entry.getKey(), entry.getValue());
      }
      obj.add("parameters", params);
      arr.add(obj);
    }
    return arr;
  }

  private static ScriptDefinition findDefinition(String idOrName) {
    if (idOrName == null || Editor.instance().getGameFile() == null) return null;
    return Editor.instance().getGameFile().getScripts().stream()
        .filter(s -> idOrName.equals(s.getId()) || idOrName.equals(s.getName()) || idOrName.equals(s.getSource()))
        .findFirst().orElse(null);
  }

  static String readScriptSource(ScriptDefinition def) {
    if (def == null) return "";
    Path path = resolvePath(def.getSource());
    if (path != null && Files.exists(path)) {
      try {
        return Files.readString(path, StandardCharsets.UTF_8);
      } catch (IOException ignored) {}
    }
    return "";
  }

  private static Path resolvePath(String relative) {
    return ScriptSourcePaths.resolvePath(Editor.instance().getProjectPath(), relative);
  }

  private static Path resolveScriptsDirectory() {
    return ScriptSourcePaths.resolveScriptsDirectory(Editor.instance().getProjectPath());
  }

  private static ScriptHostType parseHostType(String hostStr) {
    if (hostStr == null) return ScriptHostType.ENTITY;
    try {
      return ScriptHostType.valueOf(hostStr.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return ScriptHostType.ENTITY;
    }
  }

  private static String generateTemplateCode(String className, ScriptHostType host, String targetType, String packageName) {
    return ScriptTemplateFactory.generateTemplate(className, host, targetType, packageName, className);
  }
}
