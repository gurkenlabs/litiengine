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
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import java.util.List;
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
    try {
      assertNull(panel.getMonaco(), "Monaco editor should not be loaded on ScriptWorkspacePanel creation");
    } finally {
      panel.close();
    }
  }

  @Test
  void usedByShowsAssignmentsButNotEntityCompatibilityOrDefaults() {
    ScriptBindingService.UsageIndex usages = new ScriptBindingService.UsageIndex("test", List.of(
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.Game(), "Game", 0),
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.Environment("map"), "map", 0),
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.EntityType(Creature.class.getName()),
        "Creature", 0)), List.of());

    assertEquals(2, ScriptUsagesPanel.displayableUsages(usages).size());
  }

  @Test
  void scriptContextDescribesHostWithoutExposingAssignmentsAsBindings() {
    ScriptDefinition entity = definition();
    assertEquals("Entity · Creature", ScriptWorkspacePanel.scriptContext(entity));

    entity.setHost(ScriptHostType.ENVIRONMENT);
    assertEquals("Map", ScriptWorkspacePanel.scriptContext(entity));

    entity.setHost(ScriptHostType.GAME);
    assertEquals("Game", ScriptWorkspacePanel.scriptContext(entity));
  }

  @Test
  void testRefreshScriptsCompactsEmptyPackages() {
    de.gurkenlabs.litiengine.Game.init(de.gurkenlabs.litiengine.Game.COMMANDLINE_ARG_NOGUI);
    if (Editor.instance().getGameFile() == null) {
      Editor.instance().load(null, false);
    }
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getScripts().clear();
    }
    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    ScriptDefinition def1 = new ScriptDefinition("HeroAI", "java", "src/de/gurkenlabs/game/scripts/HeroAI.java", "HeroAI", ScriptHostType.ENTITY);
    Editor.instance().getGameFile().getScripts().add(def1);

    panel.refreshScripts();

    // Verify compacted package node "de.gurkenlabs.game.scripts" exists
    boolean found = false;
    for (int i = 0; i < panel.getScriptsRoot().getChildCount(); i++) {
      javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) panel.getScriptsRoot().getChildAt(i);
      if ("de.gurkenlabs.game.scripts".equals(node.getUserObject().toString())) {
        found = true;
        break;
      }
    }
    assertTrue(found, "Compacted package folder de.gurkenlabs.game.scripts should exist");
    panel.close();
  }

  @Test
  void testExtractFullyQualifiedClassName() {
    String javaSource = """
      package de.gurkenlabs.lepus.scripts;
      import de.gurkenlabs.litiengine.scripting.*;
      public class Loader extends GameScript {}
      """;
    assertEquals("de.gurkenlabs.lepus.scripts.Loader", ScriptWorkspacePanel.extractFullyQualifiedClassName(javaSource));
    assertEquals("Loader", ScriptWorkspacePanel.extractClassName(javaSource));

    String noPackage = "public class SimpleScript extends GameScript {}";
    assertEquals("SimpleScript", ScriptWorkspacePanel.extractFullyQualifiedClassName(noPackage));
  }

  private static ScriptDefinition definition() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", "src/main/groovy/TestScript.groovy",
      "TestScript", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    return definition;
  }
}

class TypedCreature extends Creature {}
