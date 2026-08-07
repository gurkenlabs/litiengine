package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import org.junit.jupiter.api.Test;

class ScriptApiIndexTest {
  @Test
  void indexesReachableEngineAndHostMembersFromTheRuntimeApi() {
    ScriptDefinition definition = new ScriptDefinition("test", "java", null, "TestScript", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());

    ScriptApiIndex index = ScriptApiIndex.create(definition);

    assertTrue(index.types().stream().anyMatch(type -> type.qualifiedName().equals(Creature.class.getName())));
    assertTrue(index.types().stream().flatMap(type -> type.members().stream()).anyMatch(member -> member.name().equals("getCenter")));
    assertFalse(index.types().stream().flatMap(type -> type.members().stream()).toList().isEmpty());
    ScriptApiIndex.TypeSymbol creature = index.type(Creature.class.getName());
    assertTrue(creature.members().stream().anyMatch(member -> member.name().equals("equals")));
    assertTrue(creature.members().stream().anyMatch(member -> member.name().equals("getClass")
      && member.returnType().equals("Class<? extends Creature>")));
  }
}
