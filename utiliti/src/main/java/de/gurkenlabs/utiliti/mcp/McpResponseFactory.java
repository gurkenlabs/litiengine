package de.gurkenlabs.utiliti.mcp;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.util.Collection;

public final class McpResponseFactory {
  private McpResponseFactory() {}

  public static JsonObject createMutationResult(
      String mapId,
      long previousRevision,
      long currentRevision,
      Collection<Integer> createdIds,
      Collection<Integer> updatedIds,
      Collection<Integer> deletedIds,
      JsonObject affectedBounds,
      Collection<String> warnings,
      String undoToken) {
    JsonObjectBuilder builder = Json.createObjectBuilder()
        .add("success", true)
        .add("mapId", mapId != null ? mapId : "")
        .add("previousRevision", previousRevision)
        .add("revision", currentRevision);

    JsonArrayBuilder createdArr = Json.createArrayBuilder();
    if (createdIds != null) {
      createdIds.forEach(createdArr::add);
    }
    builder.add("createdIds", createdArr);

    JsonArrayBuilder updatedArr = Json.createArrayBuilder();
    if (updatedIds != null) {
      updatedIds.forEach(updatedArr::add);
    }
    builder.add("updatedIds", updatedArr);

    JsonArrayBuilder deletedArr = Json.createArrayBuilder();
    if (deletedIds != null) {
      deletedIds.forEach(deletedArr::add);
    }
    builder.add("deletedIds", deletedArr);

    if (affectedBounds != null) {
      builder.add("affectedBounds", affectedBounds);
    } else {
      builder.add("affectedBounds", Json.createObjectBuilder()
          .add("x", 0).add("y", 0).add("width", 0).add("height", 0));
    }

    JsonArrayBuilder warningsArr = Json.createArrayBuilder();
    if (warnings != null) {
      warnings.forEach(warningsArr::add);
    }
    builder.add("warnings", warningsArr);
    builder.add("undoToken", undoToken != null ? undoToken : "");

    return builder.build();
  }

  public static JsonObject createError(String code, String message, boolean recoverable, JsonObject details) {
    JsonObjectBuilder errorObj = Json.createObjectBuilder()
        .add("code", code != null ? code : "UNKNOWN_ERROR")
        .add("message", message != null ? message : "An unknown error occurred")
        .add("recoverable", recoverable);

    if (details != null) {
      errorObj.add("details", details);
    }

    return Json.createObjectBuilder()
        .add("success", false)
        .add("error", errorObj)
        .build();
  }
}
