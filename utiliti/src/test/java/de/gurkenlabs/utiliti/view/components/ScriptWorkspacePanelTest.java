package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class ScriptWorkspacePanelTest {
  private static final String SOURCE = """
    import de.gurkenlabs.litiengine.scripting.*
    @ScriptInfo(id = "test", host = ScriptHostType.ENTITY, target = Creature.class)
    class TestScript extends CreatureScript {
      @Override
      void onLoaded() {}
    }
    """;

  @Test
  void newScriptsUseANeutralDefaultName() {
    assertEquals("NewScript", ScriptWorkspacePanel.DEFAULT_SCRIPT_NAME);
  }

  @Test
  void newScriptNameValidationRejectsInvalidKeywordsAndCollisions() {
    assertEquals("Enter a script name.", ScriptWorkspacePanel.scriptNameValidationError(" ", false));
    assertEquals("The script name must be a valid Java class name.",
        ScriptWorkspacePanel.scriptNameValidationError("123 Script", false));
    assertEquals("The script name must be a valid Java class name.",
        ScriptWorkspacePanel.scriptNameValidationError("class", false));
    assertEquals("A script or source file with this name already exists.",
        ScriptWorkspacePanel.scriptNameValidationError("PlayerBehavior", true));
    assertNull(ScriptWorkspacePanel.scriptNameValidationError("PlayerBehavior", false));
  }

  @Test
  void inspectorMetadataUpdatesEntityAnnotationAndBaseType() {
    ScriptDefinition definition = definition();
    definition.setTargetType(Prop.class.getName());

    String updated = ScriptWorkspacePanel.synchronizeDeclaration(SOURCE, definition);

    assertTrue(updated.contains("target = " + Prop.class.getName() + ".class"));
    assertTrue(updated.contains("extends EntityScript<" + Prop.class.getName() + ">"));
    assertTrue(updated.contains("void onLoaded("));
  }

  @Test
  void inspectorMetadataUpdatesGameLifecycleAndRemovesEntityTarget() {
    ScriptDefinition definition = definition();
    definition.setHost(ScriptHostType.GAME);
    definition.setTargetType(null);

    String updated = ScriptWorkspacePanel.synchronizeDeclaration(SOURCE, definition);

    assertTrue(updated.contains("host = ScriptHostType.GAME)"));
    assertTrue(updated.contains("extends GameScript"));
    assertTrue(updated.contains("void onStarted("));
    assertFalse(updated.contains("target ="));
  }

  @Test
  void concreteCreatureTargetsKeepTheirProjectType() {
    ScriptDefinition definition = definition();
    definition.setTargetType(TypedCreature.class.getName());

    String updated = ScriptWorkspacePanel.synchronizeDeclaration(SOURCE, definition);

    assertTrue(updated.contains("extends EntityScript<" + TypedCreature.class.getName() + ">"));
  }

  @Test
  void monacoEditorIsLoadedOnlyAfterScriptIsDisplayed() {
    de.gurkenlabs.litiengine.Game.init(de.gurkenlabs.litiengine.Game.COMMANDLINE_ARG_NOGUI);
    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    assertNull(panel.getMonaco(), "Monaco editor should not be loaded on ScriptWorkspacePanel creation");

    ScriptDefinition definition = definition();
    panel.open(definition);

    org.junit.jupiter.api.Assertions.assertNotNull(panel.getMonaco(), "Monaco editor should be loaded once a script is displayed");
  }

  private static ScriptDefinition definition() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", "src/main/groovy/TestScript.groovy",
      "TestScript", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    return definition;
  }
}

class TypedCreature extends Creature {}
