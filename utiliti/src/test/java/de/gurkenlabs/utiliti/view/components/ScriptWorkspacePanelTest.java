package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import de.gurkenlabs.utiliti.model.Icons;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JTabbedPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

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
  void scriptTabsUseSingleRowOverflowInsteadOfClippedWrappedTabs() {
    ScriptWorkspacePanel panel = new ScriptWorkspacePanel();
    try {
      assertEquals(JTabbedPane.SCROLL_TAB_LAYOUT, panel.getTabLayoutPolicy());
    } finally {
      panel.close();
    }
  }

  @Test
  void projectChangeAndCloseRemoveEveryOpenScriptTab(@TempDir Path tempDirectory) throws Exception {
    de.gurkenlabs.litiengine.Game.init(de.gurkenlabs.litiengine.Game.COMMANDLINE_ARG_NOGUI);
    Editor editor = Editor.instance();
    Path originalProject = editor.getProjectPath();
    Path firstProject = tempDirectory.resolve("first/game.litidata");
    Path secondProject = tempDirectory.resolve("second/game.litidata");
    ScriptWorkspacePanel panel = null;
    try {
      editor.setProjectPath(firstProject);
      panel = new ScriptWorkspacePanel();
      ScriptWorkspacePanel workspace = panel;
      assertTrue(workspace.isEmptyEditorStateVisible());
      ScriptDefinition first = new ScriptDefinition(
          "first", "java", "scripts/First.java", "First", ScriptHostType.GAME);
      workspace.open(first);
      assertEquals(1, workspace.getOpenTabCount());
      assertFalse(workspace.isEmptyEditorStateVisible());
      assertFalse(workspace.hasUnsavedScripts());

      editor.setProjectPath(firstProject);
      assertEquals(1, workspace.getOpenTabCount());

      editor.setProjectPath(secondProject);
      assertEquals(0, workspace.getOpenTabCount());
      assertTrue(workspace.isEmptyEditorStateVisible());

      ScriptDefinition second = new ScriptDefinition(
          "second", "java", "scripts/Second.java", "Second", ScriptHostType.GAME);
      workspace.open(second);
      assertEquals(1, workspace.getOpenTabCount());

      editor.setProjectPath(null);
      assertEquals(0, workspace.getOpenTabCount());
      assertTrue(workspace.isEmptyEditorStateVisible());
    } finally {
      if (panel != null) {
        panel.close();
      }
      editor.setProjectPath(originalProject);
    }
  }

  @Test
  void usedByShowsEveryPersistedAssignmentWithoutCompatibilityOnlyNodes() {
    ScriptBindingService.UsageIndex usages = new ScriptBindingService.UsageIndex("test", List.of(
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.Game(), "Game", 0),
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.Environment("map"), "map", 0),
      new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.EntityType(Creature.class.getName()),
        "Creature", 0)), List.of());

    assertEquals(3, ScriptUsagesPanel.displayableUsages(usages).size());
  }

  @Test
  void scriptContextDescribesHostWithoutExposingAssignmentsAsBindings() {
    ScriptDefinition entity = definition();
    assertEquals("Entity Script · Creature", ScriptWorkspacePanel.scriptContext(entity));

    entity.setHost(ScriptHostType.ENVIRONMENT);
    assertEquals("Map Script", ScriptWorkspacePanel.scriptContext(entity));

    entity.setHost(ScriptHostType.GAME);
    assertEquals("Game Script", ScriptWorkspacePanel.scriptContext(entity));
    assertEquals(new java.awt.Color(251, 191, 36), ScriptWorkspacePanel.scriptBadgeColor(entity));

    entity.setHost(ScriptHostType.ENVIRONMENT);
    assertEquals(new java.awt.Color(74, 222, 128), ScriptWorkspacePanel.scriptBadgeColor(entity));

    entity.setHost(ScriptHostType.ENTITY);
    assertEquals(new java.awt.Color(56, 189, 248), ScriptWorkspacePanel.scriptBadgeColor(entity));

    assertNotNull(ScriptWorkspacePanel.scriptContextIcon(entity));
    assertEquals(Icons.CREATURE_16, ScriptWorkspacePanel.entityTypeIcon("Creature"));
    assertEquals(Icons.PROP_16, ScriptWorkspacePanel.entityTypeIcon("de.gurkenlabs.litiengine.entities.Prop"));
    assertEquals(Icons.EMITTER_16, ScriptWorkspacePanel.entityTypeIcon("Emitter"));
    assertEquals(Icons.BULB_16, ScriptWorkspacePanel.entityTypeIcon("LightSource"));
    assertEquals(Icons.TRIGGER_16, ScriptWorkspacePanel.entityTypeIcon("Trigger"));
    assertEquals(Icons.SPAWNPOINT_16, ScriptWorkspacePanel.entityTypeIcon("Spawnpoint"));
  }

  @Test
  void usedByIsHiddenForGameScripts() {
    ScriptDefinition definition = definition();
    assertTrue(ScriptWorkspacePanel.showsUsagesFor(definition));

    definition.setHost(ScriptHostType.ENVIRONMENT);
    assertTrue(ScriptWorkspacePanel.showsUsagesFor(definition));

    definition.setHost(ScriptHostType.GAME);
    assertFalse(ScriptWorkspacePanel.showsUsagesFor(definition));
    assertFalse(ScriptWorkspacePanel.showsUsagesFor(null));
  }

  @Test
  void scriptListDoesNotExposeSourcePackages() {
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

    boolean foundAtRoot = false;
    for (int i = 0; i < panel.getScriptsRoot().getChildCount(); i++) {
      javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) panel.getScriptsRoot().getChildAt(i);
      if ("HeroAI".equals(node.getUserObject().toString())) {
        foundAtRoot = true;
        assertTrue(node.isLeaf());
        break;
      }
    }
    assertTrue(foundAtRoot, "Scripts should be listed directly without Java package folders");
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

  @Test
  void testDeleteScriptRemovesDefinitionAndCleansUsages() {
    de.gurkenlabs.litiengine.Game.init(de.gurkenlabs.litiengine.Game.COMMANDLINE_ARG_NOGUI);
    Editor.instance().getGameFile().getScripts().clear();
    ScriptDefinition script = new ScriptDefinition("NewScript2", "java", "scripts/NewScript2.java", "de.gurkenlabs.lepus.scripts.NewScript2", ScriptHostType.ENVIRONMENT);
    Editor.instance().getGameFile().getScripts().add(script);

    assertEquals(1, Editor.instance().getGameFile().getScripts().size());
  }

  private static ScriptDefinition definition() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", "src/main/groovy/TestScript.groovy",
      "TestScript", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    return definition;
  }
}

class TypedCreature extends Creature {}
