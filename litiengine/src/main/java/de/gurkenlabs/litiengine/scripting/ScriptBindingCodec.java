package de.gurkenlabs.litiengine.scripting;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/// Encodes structured script bindings in TMX-compatible JSON properties.
public final class ScriptBindingCodec {
  private ScriptBindingCodec() {}

  public static String encode(List<ScriptBinding> bindings) {
    JsonArrayBuilder array = Json.createArrayBuilder();
    if (bindings != null) {
      bindings.stream().sorted(Comparator.comparingInt(ScriptBinding::getOrder)).forEach(binding -> {
        JsonObjectBuilder parameters = Json.createObjectBuilder();
        binding.getParameters().forEach((name, value) -> parameters.add(name, value == null ? "" : value));
        array.add(Json.createObjectBuilder()
          .add("script", binding.getScript())
          .add("enabled", binding.isEnabled())
          .add("order", binding.getOrder())
          .add("parameters", parameters));
      });
    }
    return array.build().toString();
  }

  public static List<ScriptBinding> decode(String value) {
    if (value == null || value.isBlank()) return List.of();
    try (JsonReader reader = Json.createReader(new StringReader(value))) {
      JsonArray array = reader.readArray();
      List<ScriptBinding> bindings = new ArrayList<>();
      for (int i = 0; i < array.size(); i++) {
        JsonObject object = array.getJsonObject(i);
        String script = object.getString("script", "").trim();
        if (script.isEmpty()) continue;
        ScriptBinding binding = new ScriptBinding(script);
        binding.setEnabled(object.getBoolean("enabled", true));
        binding.setOrder(object.getInt("order", i));
        JsonObject parameters = object.getJsonObject("parameters");
        if (parameters != null) {
          for (Map.Entry<String, jakarta.json.JsonValue> parameter : parameters.entrySet()) {
            String parameterValue = parameter.getValue().getValueType() == jakarta.json.JsonValue.ValueType.STRING
              ? parameters.getString(parameter.getKey()) : parameter.getValue().toString();
            binding.setParameter(parameter.getKey(), parameterValue);
          }
        }
        bindings.add(binding);
      }
      bindings.sort(Comparator.comparingInt(ScriptBinding::getOrder));
      return List.copyOf(bindings);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid script binding JSON.", e);
    }
  }
}
