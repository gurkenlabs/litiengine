package de.gurkenlabs.utiliti.controller.debug;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Serializes machine-local script breakpoints without modifying game resources or source files. */
public final class ScriptBreakpointStore {
  private ScriptBreakpointStore() {}

  public static List<ScriptBreakpoint> decode(String serialized) {
    if (serialized == null || serialized.isBlank()) return List.of();
    try (var reader = Json.createReader(new StringReader(serialized))) {
      JsonArray array = reader.readArray();
      List<ScriptBreakpoint> result = new ArrayList<>();
      for (JsonValue value : array) {
        if (!(value instanceof JsonObject item)) continue;
        try {
          int line = item.getInt("line", 0);
          if (line < 1) continue;
          result.add(new ScriptBreakpoint(
              item.getString("project", ""),
              item.getString("scriptId", ""),
              item.getString("source", ""),
              line,
              item.getBoolean("enabled", true)));
        } catch (RuntimeException ignored) {
          // Preserve valid breakpoints when one stored entry is corrupt.
        }
      }
      return List.copyOf(result);
    } catch (RuntimeException ignored) {
      return List.of();
    }
  }

  public static String encode(Collection<ScriptBreakpoint> breakpoints) {
    JsonArrayBuilder array = Json.createArrayBuilder();
    if (breakpoints != null) {
          breakpoints.stream()
          .filter(java.util.Objects::nonNull)
          .distinct()
          .sorted(java.util.Comparator.comparing(ScriptBreakpoint::project)
              .thenComparing(ScriptBreakpoint::scriptId)
              .thenComparing(ScriptBreakpoint::source)
              .thenComparingInt(ScriptBreakpoint::line))
          .forEach(item -> array.add(Json.createObjectBuilder()
              .add("project", item.project())
              .add("scriptId", item.scriptId())
              .add("source", item.source())
              .add("line", item.line())
              .add("enabled", item.enabled())));
    }
    StringWriter output = new StringWriter();
    try (var writer = Json.createWriter(output)) {
      writer.writeArray(array.build());
    }
    return output.toString();
  }
}
