package de.gurkenlabs.litiengine.scripting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  void testAnnotationParameterCompletionInsideScriptProperty() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class JanitorBehavior extends EntityScript<Object> {
          @ScriptProperty(name = "mycnt", description = "something weird", default
          private int speed;
        }
        """;

    int line = 4;
    int col = 74; // right after "default"
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
    assertTrue(labels.contains("defaultValue"), "Should suggest defaultValue attribute of @ScriptProperty");
    assertFalse(labels.contains("DefaultAccessorNamingStrategy"), "Should not suggest 3rd-party classes inside annotation params");
    assertFalse(labels.contains("Default"), "Should not suggest random classes inside annotation params");
  }

  @Test
  void testNoThirdPartyClassesInCatalog() {
    List<Class<?>> publicTypes = EngineTypeCatalog.publicTypes();
    List<Class<?>> projectTypes = EngineTypeCatalog.projectTypes(Thread.currentThread().getContextClassLoader());

    for (Class<?> type : publicTypes) {
      String name = type.getName();
      assertFalse(name.startsWith("tools.jackson."), "Catalog should not contain tools.jackson: " + name);
      assertFalse(name.startsWith("com.google.gson."), "Catalog should not contain com.google.gson: " + name);
      assertFalse(name.startsWith("org.apache."), "Catalog should not contain org.apache: " + name);
      assertFalse(name.startsWith("com.formdev."), "Catalog should not contain com.formdev: " + name);
    }

    for (Class<?> type : projectTypes) {
      String name = type.getName();
      assertFalse(name.startsWith("tools.jackson."), "Project types should not contain tools.jackson: " + name);
      assertFalse(name.startsWith("com.google.gson."), "Project types should not contain com.google.gson: " + name);
      assertFalse(name.startsWith("org.apache."), "Project types should not contain org.apache: " + name);
      assertFalse(name.startsWith("com.formdev."), "Project types should not contain com.formdev: " + name);
    }
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
    assertTrue(titles.stream().anyMatch(t -> t.contains("@ScriptProperty")), "Should offer quick fix to change to @ScriptProperty");
    assertTrue(titles.contains("Change to '@ScriptInfo'"), "Should offer quick fix to change to @ScriptInfo");
  }

  @Test
  void testCodeActionsOnAtSPrefixAboveField() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class Loader extends GameScript {
          @S
          private int speed;
        }
        """;

    int line = 4;
    int col = 4;
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///Loader.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.CodeAction> actions = this.service.codeActions(
      doc,
      new ScriptLanguageService.Range(new ScriptLanguageService.Position(line, 2), new ScriptLanguageService.Position(line, col)),
      List.of()
    );

    assertNotNull(actions);
    assertFalse(actions.isEmpty(), "QuickFix code actions should not be empty for @S above field");

    List<String> titles = actions.stream().map(ScriptLanguageService.CodeAction::title).toList();
    assertTrue(titles.stream().anyMatch(t -> t.contains("@ScriptProperty")), "Should offer to add/change to @ScriptProperty");
  }

  @Test
  void testAlwaysOfferToConvertFieldsToScriptProperty() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class Loader extends GameScript {
          private int speed;
          public boolean active = true;
        }
        """;

    int line = 4; // on `private int speed;`
    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///Loader.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.CodeAction> actions = this.service.codeActions(
      doc,
      new ScriptLanguageService.Range(new ScriptLanguageService.Position(line, 5), new ScriptLanguageService.Position(line, 15)),
      List.of()
    );

    assertNotNull(actions);
    assertFalse(actions.isEmpty(), "Should offer code actions to convert field to @ScriptProperty");

    List<String> titles = actions.stream().map(ScriptLanguageService.CodeAction::title).toList();
    assertTrue(titles.contains("Add '@ScriptProperty' to field 'speed'"), "Should offer to add @ScriptProperty to field 'speed'");
    assertTrue(titles.stream().anyMatch(t -> t.startsWith("Add '@ScriptProperty(name = \"speed\"")), "Should offer configured @ScriptProperty for 'speed'");
    assertTrue(titles.contains("Convert field 'active' to '@ScriptProperty'"), "Should offer to convert other field 'active' too");
  }

  @Test
  void testHoverOnScriptPropertyReturnsRichDocumentation() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class Loader extends GameScript {
          @ScriptProperty(name = "speed", description = "Movement speed")
          private int speed;
        }
        """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///Loader.java"),
      code,
      1,
      null
    );

    // Hover on ScriptProperty
    Optional<ScriptLanguageService.Hover> hover = this.service.hover(doc, new ScriptLanguageService.Position(4, 5));
    assertTrue(hover.isPresent(), "Hover on @ScriptProperty should return documentation");
    String markdown = hover.get().markdown();
    assertTrue(markdown.contains("### @ScriptProperty"), "Hover should include ### @ScriptProperty");
    assertTrue(markdown.contains("utiLITI Inspector"), "Hover should explain utiLITI Inspector integration");
    assertTrue(markdown.contains("Supported Field Types:"), "Hover should list supported field types");
    assertTrue(markdown.contains("Attributes:"), "Hover should explain attributes");

    // Hover on attribute `name`
    Optional<ScriptLanguageService.Hover> attrHover = this.service.hover(doc, new ScriptLanguageService.Position(4, 19));
    assertTrue(attrHover.isPresent(), "Hover on attribute 'name' should return attribute documentation");
    assertTrue(attrHover.get().markdown().contains("**name**"), "Attribute hover should contain **name**");
  }

  @Test
  void testOverrideEventCompletionsForEnvironmentScript() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class MapController extends EnvironmentScript {
          @Override
          
        }
        """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///MapController.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.Completion> completions = this.service.complete(doc, new ScriptLanguageService.Position(4, 11));
    assertNotNull(completions);
    assertFalse(completions.isEmpty());

    List<String> labels = completions.stream().map(ScriptLanguageService.Completion::label).toList();
    assertTrue(labels.contains("@Override public void onLoaded()"), "Should suggest onLoaded for EnvironmentScript");
    assertTrue(labels.contains("@Override protected void onEntityRemoved(IEntity entity)"), "Should suggest onEntityRemoved for EnvironmentScript");
    assertTrue(labels.contains("@Override public void update()"), "Should suggest update for EnvironmentScript");

    // Check that missing import (IEntity) is provided via TextEdit
    ScriptLanguageService.Completion entityRemoved = completions.stream()
      .filter(c -> c.label().contains("onEntityRemoved"))
      .findFirst().orElseThrow();
    assertNotNull(entityRemoved.additionalEdits());
    assertTrue(entityRemoved.additionalEdits().stream().anyMatch(e -> e.text().contains("IEntity")), "Should include auto-import for IEntity");
  }

  @Test
  void testOverrideEventCompletionsForEntityScript() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class MonsterBehavior extends EntityScript<Object> {
          @
        }
        """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///MonsterBehavior.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.Completion> completions = this.service.complete(doc, new ScriptLanguageService.Position(4, 3));
    assertNotNull(completions);
    List<String> labels = completions.stream().map(ScriptLanguageService.Completion::label).toList();
    assertTrue(labels.contains("@Override protected void onHit(EntityHitEvent event)"), "Should suggest onHit for EntityScript");
    assertTrue(labels.contains("@Override protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent)"), "Should suggest onDeath for EntityScript");
  }

  @Test
  void testOverrideAfterExistingAnnotationOnPreviousLine() {
    String code = """
        package scripts;
        import de.gurkenlabs.litiengine.scripting.*;

        public class MonsterBehavior extends EntityScript<Object> {
          @Override
          onD
        }
        """;

    ScriptLanguageService.Document doc = new ScriptLanguageService.Document(
      URI.create("memory:///MonsterBehavior.java"),
      code,
      1,
      null
    );

    List<ScriptLanguageService.Completion> completions = this.service.complete(doc, new ScriptLanguageService.Position(5, 5));
    assertNotNull(completions);
    List<String> labels = completions.stream().map(ScriptLanguageService.Completion::label).toList();
    assertTrue(labels.contains("protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent)"),
      "Should suggest method declaration without duplicate @Override when @Override is on line above");

    ScriptLanguageService.Completion onDeath = completions.stream()
      .filter(c -> c.label().startsWith("protected void onDeath"))
      .findFirst().orElseThrow();
    assertTrue(onDeath.insertText().startsWith("protected void onDeath"), "Insert text should start with protected void onDeath");
    assertTrue(onDeath.insertText().contains("{\n  ${0}\n}"), "Insert text should contain method body template");
    assertFalse(onDeath.insertText().contains("${1:entity}"), "Insert text should not be a raw call with ${1:entity}");
  }
}

