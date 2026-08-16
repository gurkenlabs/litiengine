package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.abilities.AbilityInfo;
import de.gurkenlabs.litiengine.abilities.DynamicAbility;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class CreatureAbilityTests {

  @AbilityInfo(name = "CustomHeal", cooldown = 1000)
  private static class TestHealAbility extends Ability {
    private boolean castExecuted = false;

    public TestHealAbility(Creature executor) {
      super(executor);
    }

    @Override
    public AbilityExecution cast() {
      this.castExecuted = true;
      return super.cast();
    }
  }

  private Creature creature;

  @BeforeAll
  static void init() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @BeforeEach
  void setUp() {
    this.creature = new Creature();
  }

  @Test
  void testAddAndRetrieveAbilities() {
    assertTrue(creature.getAbilities().isEmpty());
    assertFalse(creature.hasAbility("CustomHeal"));
    assertFalse(creature.hasAbility(TestHealAbility.class));

    TestHealAbility heal = new TestHealAbility(creature);
    creature.addAbility(heal);

    assertEquals(1, creature.getAbilities().size());
    assertTrue(creature.hasAbility("CustomHeal"));
    assertTrue(creature.hasAbility(TestHealAbility.class));
    assertSame(heal, creature.getAbility("CustomHeal").orElse(null));
    assertSame(heal, creature.getAbility(TestHealAbility.class).orElse(null));

    // Remove by name
    creature.removeAbility("CustomHeal");
    assertFalse(creature.hasAbility("CustomHeal"));
    assertTrue(creature.getAbilities().isEmpty());

    // Add again and remove by instance
    creature.addAbility(heal);
    assertEquals(1, creature.getAbilities().size());
    creature.removeAbility(heal);
    assertTrue(creature.getAbilities().isEmpty());
  }

  @Test
  void testCastByNameAndClass() {
    TestHealAbility heal = new TestHealAbility(creature);
    creature.addAbility(heal);

    assertTrue(creature.canCast("CustomHeal"));
    assertTrue(creature.canCast(TestHealAbility.class));
    assertFalse(creature.isOnCooldown("CustomHeal"));
    assertFalse(creature.isOnCooldown(TestHealAbility.class));

    assertFalse(heal.castExecuted);
    AbilityExecution execution = creature.cast("CustomHeal");
    assertNotNull(execution);
    assertTrue(heal.castExecuted);

    heal.castExecuted = false;
    AbilityExecution classExec = creature.cast(TestHealAbility.class);
    assertNotNull(classExec);
    assertTrue(heal.castExecuted);

    assertNull(creature.cast("NonExistentAbility"));
    assertFalse(creature.canCast("NonExistentAbility"));
    assertFalse(creature.isOnCooldown("NonExistentAbility"));
  }

  @Test
  void testCreateAbilityFluent() {
    AtomicInteger castCount = new AtomicInteger();

    DynamicAbility ability = creature.createAbility("Dash")
        .cooldown(2000)
        .range(100)
        .onCast(exec -> castCount.incrementAndGet())
        .register();

    assertNotNull(ability);
    assertTrue(creature.hasAbility("Dash"));
    assertTrue(creature.canCast("Dash"));
    assertSame(ability, creature.getAbility("Dash").orElse(null));

    creature.cast("Dash");
    assertEquals(1, castCount.get());
  }
}
