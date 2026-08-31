package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Completion;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.CompletionKind;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Position;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Range;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService.TextEdit;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Suggests context-aware values and constants for active method parameter types.
final class ScriptParameterSuggestionProvider {
  private static final List<String> COLOR_NAMES = List.of(
    "RED", "GREEN", "BLUE", "YELLOW", "WHITE", "BLACK", "ORANGE", "CYAN", "MAGENTA", "GRAY", "DARK_GRAY", "LIGHT_GRAY", "PINK"
  );
  private static final List<String> DIRECTION_NAMES = List.of("UP", "DOWN", "LEFT", "RIGHT", "UNDEFINED");

  private ScriptParameterSuggestionProvider() {}

  static List<Completion> suggestForType(
      Class<?> paramType,
      Map<String, Class<?>> variables,
      Set<String> importedFqns,
      int importInsertLine) {

    List<Completion> result = new ArrayList<>();

    if (paramType == Color.class) {
      List<TextEdit> colorEdits = importedFqns.contains("java.awt.Color") ? List.of()
        : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)), "import java.awt.Color;\n"));
      for (String cName : COLOR_NAMES) {
        result.add(new Completion("Color." + cName, CompletionKind.FIELD, "Color", "Standard Color constant", "Color." + cName, "Color", List.of(), colorEdits));
      }
      result.add(new Completion("new Color(r, g, b)", CompletionKind.SNIPPET, "Custom Color", "Creates a custom RGB color", "new Color(${1:255}, ${2:0}, ${3:0})", "Color", List.of(), colorEdits));
    } else if (paramType == Direction.class) {
      for (String dName : DIRECTION_NAMES) {
        result.add(new Completion("Direction." + dName, CompletionKind.FIELD, "Direction", "Cardinal direction constant", "Direction." + dName, "Direction", List.of(), List.of()));
      }
    } else if (paramType == Point2D.class) {
      result.add(new Completion("host().getCenter()", CompletionKind.METHOD, "Point2D", "Center coordinate of host entity", "host().getCenter()", "Point2D", List.of(), List.of()));
      result.add(new Completion("host().getLocation()", CompletionKind.METHOD, "Point2D", "Location of host entity", "host().getLocation()", "Point2D", List.of(), List.of()));
      result.add(new Completion("new Point2D.Double(x, y)", CompletionKind.SNIPPET, "Point2D", "Creates a new Point2D coordinate", "new Point2D.Double(${1:0}, ${2:0})", "Point2D", List.of(), List.of()));
    } else if (IEntity.class.isAssignableFrom(paramType)) {
      result.add(new Completion("host()", CompletionKind.METHOD, paramType.getSimpleName(), "The current host entity", "host()", paramType.getSimpleName(), List.of(), List.of()));
    } else if (paramType == Font.class) {
      result.add(new Completion("new Font(name, style, size)", CompletionKind.SNIPPET, "Font", "Creates a custom Font", "new Font(${1:\"Arial\"}, Font.PLAIN, ${2:12})", "Font", List.of(), List.of()));
    }

    if (variables != null) {
      for (var entry : variables.entrySet()) {
        if (paramType.isAssignableFrom(entry.getValue())) {
          result.add(new Completion(entry.getKey(), CompletionKind.VARIABLE, entry.getValue().getSimpleName(), "Local variable", entry.getKey(), entry.getValue().getSimpleName(), List.of(), List.of()));
        }
      }
    }

    return result;
  }
}
