package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import org.junit.jupiter.api.Test;

class ScriptComponentIndexTest {
  @Test
  void discoversControllerContractsAndHostCompatibleImplementations() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", null, "Test", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());

    ScriptComponentIndex index = ScriptComponentIndex.create(definition);

    assertTrue(index.components().stream()
      .anyMatch(component -> component.simpleName().equals("IMovementController") && component.contract()));
    assertTrue(index.components().stream()
      .anyMatch(component -> component.simpleName().equals("KeyboardEntityController")
        && !component.contract() && component.preferredConstructor().acceptsHost()));
  }

  @Test
  void excludesImplementationsWhoseConstructorsCannotAcceptTheHost() {
    ScriptDefinition definition = new ScriptDefinition("test", "groovy", null, "Test", ScriptHostType.ENTITY);

    ScriptComponentIndex index = ScriptComponentIndex.create(definition);

    assertFalse(index.components().stream()
      .anyMatch(component -> component.simpleName().equals("KeyboardEntityController") && !component.contract()));
  }
}
