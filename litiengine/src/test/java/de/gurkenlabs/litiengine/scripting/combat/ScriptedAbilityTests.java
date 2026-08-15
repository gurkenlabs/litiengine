package de.gurkenlabs.litiengine.scripting.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.abilities.CastType;
import de.gurkenlabs.litiengine.entities.Creature;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScriptedAbilityTests {
  @BeforeEach
  void initGame() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void testScriptedAbilityBuildingAndExecution() {
    Creature creature = new Creature();
    AtomicBoolean executed = new AtomicBoolean(false);

    ScriptedAbility ability = new ScriptedAbilityBuilder(creature, "Fireball")
        .description("Shoots a fiery projectile.")
        .cooldown(500)
        .range(200)
        .impact(50)
        .value(25)
        .castType(CastType.INSTANT)
        .onCast(execution -> executed.set(true))
        .build();

    assertEquals("Fireball", ability.getName());
    assertEquals("Shoots a fiery projectile.", ability.getDescription());
    assertEquals(500, ability.getAttributes().cooldown().getModifiedValue());
    assertEquals(200, ability.getAttributes().range().getModifiedValue());
    assertEquals(50, ability.getAttributes().impact().getModifiedValue());
    assertEquals(25, ability.getAttributes().value().getModifiedValue());
    assertEquals(CastType.INSTANT, ability.getCastType());

    assertTrue(ability.canCast());
    AbilityExecution exec = ability.cast();
    assertNotNull(exec);
    assertTrue(executed.get());
  }
}
