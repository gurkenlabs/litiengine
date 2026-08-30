package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Parameter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/// Provides descriptive parameter names for reflected methods in script intellisense.
final class ScriptParameterNamer {
  private ScriptParameterNamer() {}

  static List<Parameter> extractParameters(Method method) {
    java.lang.reflect.Parameter[] rawParams = method.getParameters();
    List<Parameter> parameters = new ArrayList<>();
    for (int i = 0; i < rawParams.length; i++) {
      String pName = formatParamName(rawParams[i], i, rawParams.length, method.getName());
      parameters.add(new Parameter(pName, rawParams[i].getParameterizedType().getTypeName()));
    }
    return parameters;
  }

  static String formatParamName(java.lang.reflect.Parameter param, int index, int total, String methodName) {
    String rawName = param.getName();
    if (rawName != null && !rawName.startsWith("arg") && !rawName.isBlank()) {
      return rawName;
    }
    String typeName = simpleName(param.getParameterizedType().getTypeName());
    return switch (typeName) {
      case "String" -> {
        if (methodName.contains("Text") || methodName.contains("Message") || methodName.contains("Banner")) yield index == 0 ? "text" : "subtitle";
        if (methodName.contains("Sound") || methodName.contains("Audio")) yield "soundName";
        if (methodName.contains("Prop") || methodName.contains("Sprite")) yield "spriteSheet";
        yield "name";
      }
      case "Point2D" -> methodName.contains("Pan") || methodName.contains("Move") ? "target" : "location";
      case "Color" -> "color";
      case "Font" -> "font";
      case "IEntity", "Entity" -> "entity";
      case "Creature" -> "creature";
      case "Environment" -> "environment";
      case "Direction" -> "direction";
      case "Sound" -> "sound";
      case "Graphics2D" -> "g";
      case "int" -> {
        if (methodName.contains("Pan") || methodName.contains("Shake") || methodName.contains("Duration")) yield "durationTicks";
        if (methodName.contains("Zoom") || methodName.contains("Delay") || methodName.contains("Time") || methodName.contains("Text") || methodName.contains("Banner")) yield "durationMs";
        if (index == 0 && total >= 2) yield "x";
        if (index == 1 && total >= 2) yield "y";
        yield "value";
      }
      case "double", "float" -> {
        if (methodName.contains("Zoom")) yield "targetZoom";
        if (methodName.contains("Shake")) yield "intensity";
        if (methodName.contains("Velocity") || methodName.contains("Speed")) yield "velocity";
        if (methodName.contains("Angle")) yield "angleDegrees";
        if (index == 0 && total >= 2) yield "x";
        if (index == 1 && total >= 2) yield "y";
        yield "value";
      }
      case "boolean" -> "enabled";
      default -> {
        if (!typeName.isEmpty() && Character.isUpperCase(typeName.charAt(0))) {
          yield Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
        }
        yield "arg" + index;
      }
    };
  }

  private static String simpleName(String fqn) {
    if (fqn == null) return "";
    int lastDot = fqn.lastIndexOf('.');
    return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
  }
}
