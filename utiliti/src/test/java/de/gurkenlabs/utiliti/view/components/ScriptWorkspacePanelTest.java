package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import org.junit.jupiter.api.Test;

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

  private static ScriptDefinition definition() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", "src/main/groovy/TestScript.groovy",
      "TestScript", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    return definition;
  }
}

class TypedCreature extends Creature {}
