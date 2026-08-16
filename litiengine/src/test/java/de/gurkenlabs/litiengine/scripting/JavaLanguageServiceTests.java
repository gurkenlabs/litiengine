package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaLanguageServiceTests {

  private JavaLanguageService service;

  @BeforeEach
  void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    this.service = new JavaLanguageService(new ScriptLanguageService.Workspace(
      Path.of("."),
      Thread.currentThread().getContextClassLoader(),
      Map.of()
    ));
  }

  @Test
  void testAnnotationCompletionOnAtSymbol() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class TestScript extends EntityScript<Object> {
          @
          private int speed;
        }
        """;

    int atLine = 4;
    int atCol = 3; // right after '@'
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///TestScript.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.Completion> completions = this.service.complete(doc, new ScriptLanguageService.Position(atLine, atCol));
    assertNotNull(completions);
    assertFalse(completions.isEmpty());

    List<String> labels = completions.stream().map(ScriptLanguageService.Completion::label).toList();
    assertTrue(labels.contains("ScriptProperty"), "Completions should include ScriptProperty");
    assertTrue(labels.stream().anyMatch(l -> l.startsWith("ScriptProperty")), "Completions should include ScriptProperty snippet");
    assertTrue(labels.contains("ScriptInfo"), "Completions should include ScriptInfo");
    assertTrue(labels.contains("Override"), "Completions should include Override");
  }

  @Test
  void testAnnotationCompletionOnPrefix() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class JanitorBehavior extends EntityScript<Object> {
          @Sc
          private int speed;
        }
        """;

    int line = 4;
    int col = 5; // right after '@Sc'
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///JanitorBehavior.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.Completion> completions = this.service.complete(doc, new ScriptLanguageService.Position(line, col));
    assertNotNull(completions);
    assertFalse(completions.isEmpty());

    List<String> labels = completions.stream().map(ScriptLanguageService.Completion::label).toList();
    assertTrue(labels.contains("ScriptProperty"), "Completions should include ScriptProperty when typed @Sc");
    assertTrue(labels.stream().anyMatch(l -> l.startsWith("ScriptProperty")), "Completions should include ScriptProperty snippet");
  }

  @Test
  void testCodeActionsOnIncompleteScriptAnnotation() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class JanitorBehavior extends EntityScript<Object> {
          @Script
          private int speed;
        }
        """;

    int line = 4;
    int col = 8;
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///JanitorBehavior.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.CodeAction> actions = this.service.codeActions(
      doc,
      new ScriptLanguageService.Range(new ScriptLanguageService.Position(line, 2), new ScriptLanguageService.Position(line, col)),
      List.of(new ScriptDiagnostic(
        ScriptDiagnostic.Severity.ERROR,
        "JanitorBehavior",
        "JanitorBehavior.java",
        line + 1,
        3,
        "cannot find symbol: class Script"
      ))
    );

    assertNotNull(actions);
    assertFalse(actions.isEmpty(), "QuickFix code actions should not be empty for @Script");

    List<String> titles = actions.stream().map(ScriptLanguageService.CodeAction::title).toList();
    assertTrue(titles.contains("Change to '@ScriptProperty'"), "Should offer quick fix to change to @ScriptProperty");
    assertTrue(titles.stream().anyMatch(t -> t.startsWith("Change to '@ScriptProperty(")), "Should offer quick fix to change to @ScriptProperty snippet");
    assertTrue(titles.contains("Change to '@ScriptInfo'"), "Should offer quick fix to change to @ScriptInfo");
  }
}

