package de.gurkenlabs.utiliti.mcp;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/** Supplies standard MCP annotation hints for utiLITI tools. */
final class McpToolGroups {
  private McpToolGroups() {
    throw new UnsupportedOperationException();
  }

  static JsonObject annotationsFor(String toolName) {
    boolean readOnly = isReadOnly(toolName);
    boolean destructive = !readOnly && isDestructive(toolName);
    boolean idempotent = readOnly || isIdempotent(toolName);
    boolean openWorld = toolName != null
        && (toolName.startsWith("import-")
            || toolName.startsWith("export-")
            || "load-project".equals(toolName)
            || "open-snapshot-folder".equals(toolName));

    return Json.createObjectBuilder()
        .add("title", title(toolName))
        .add("readOnlyHint", readOnly)
        .add("destructiveHint", destructive)
        .add("idempotentHint", idempotent)
        .add("openWorldHint", openWorld)
        .build();
  }

  private static boolean isReadOnly(String toolName) {
    if (toolName == null) {
      return false;
    }
    return !"get-canvas-snapshot".equals(toolName)
        && (toolName.startsWith("get-")
            || toolName.startsWith("list-")
            || toolName.startsWith("search-")
            || toolName.startsWith("query-")
            || toolName.startsWith("validate-"));
  }

  private static boolean isDestructive(String toolName) {
    if (toolName == null) {
      return true;
    }
    return toolName.startsWith("delete-")
        || toolName.startsWith("remove-")
        || toolName.startsWith("clear-")
        || toolName.startsWith("import-")
        || "load-project".equals(toolName)
        || "save-project".equals(toolName)
        || "export-resource".equals(toolName)
        || "reassign-map-ids".equals(toolName);
  }

  private static boolean isIdempotent(String toolName) {
    if (toolName == null) {
      return false;
    }
    return toolName.startsWith("set-")
        || toolName.startsWith("configure-")
        || toolName.startsWith("select-")
        || toolName.startsWith("deselect-")
        || toolName.startsWith("center-");
  }

  private static String title(String toolName) {
    if (toolName == null || toolName.isBlank()) {
      return "Utiliti Tool";
    }
    StringBuilder title = new StringBuilder();
    for (String part : toolName.split("-")) {
      if (!title.isEmpty()) {
        title.append(' ');
      }
      title.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
    }
    return title.toString();
  }
}
