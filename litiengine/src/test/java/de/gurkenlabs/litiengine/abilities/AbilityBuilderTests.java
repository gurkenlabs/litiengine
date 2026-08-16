package de.gurkenlabs.litiengine.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class AbilityBuilderTests {

  @BeforeEach
  void init() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void testBuildDynamicAbility() {
    Creature creature = new Creature();
    AtomicBoolean castCalled = new AtomicBoolean(false);
    Effect dummyEffect = new Effect(TargetingStrategy.none()) {};

    DynamicAbility ability = new AbilityBuilder(creature, "Fireball")
        .description("Shoots a ball of fire")
        .cooldown(1500)
        .range(120)
        .impact(30)
        .value(50)
        .castType(CastType.INSTANT)
        .multiTarget(true)
        .effect(dummyEffect)
        .onCast(exec -> castCalled.set(true))
        .build();

    assertNotNull(ability);
    assertEquals("Fireball", ability.getName());
    assertEquals("Shoots a ball of fire", ability.getDescription());
    assertEquals(1500, ability.getAttributes().cooldown().getModifiedValue());
    assertEquals(120, ability.getAttributes().range().getModifiedValue());
    assertEquals(30, ability.getAttributes().impact().getModifiedValue());
    assertEquals(50, ability.getAttributes().value().getModifiedValue());
    assertEquals(CastType.INSTANT, ability.getCastType());
    assertTrue(ability.isMultiTarget());
    assertEquals(1, ability.getEffects().size());
    assertSame(dummyEffect, ability.getEffects().get(0));
    assertSame(creature, ability.getExecutor());

    ability.cast();
    assertTrue(castCalled.get());
  }

  @Test
  void testRegisterOnCreature() {
    Creature creature = new Creature();
    assertFalse(creature.hasAbility("Slash"));

    DynamicAbility registered = creature.createAbility("Slash")
        .cooldown(500)
        .impact(10)
        .register();

    assertNotNull(registered);
    assertTrue(creature.hasAbility("Slash"));
    assertEquals(1, creature.getAbilities().size());
    assertSame(registered, creature.getAbility("Slash").orElse(null));
  }
}
