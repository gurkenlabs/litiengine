package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.*;
import de.gurkenlabs.litiengine.abilities.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EntityHitEventTests {

  @BeforeAll
  public static void initGame() {
    // necessary because the environment need access to the current game time
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AbilityInfo(castType = CastType.ONCONFIRM, name = "random name", description = "random description", cooldown = 333, duration = 222, impact = 111,
      impactAngle = 99, multiTarget = true, origin = EntityPivotType.OFFSET, range = 444, value = 999)
  private static class TestAbility extends Ability {
    protected TestAbility(Creature executor) {
      super(executor);
    }
  }

  @Test
  void createHitEvent_NullExecutor() {
    // arrange
    ICombatEntity hitEntity = new CombatEntity();

    // act
    EntityHitEvent event = new EntityHitEvent(hitEntity, null, 9001, true);

    // assert
    assertNull(event.getExecutor());
  }

  @Test
  void createHitEvent_ValidExecutor() {
    // arrange
    ICombatEntity hitEntity = new CombatEntity();
    Creature executorCreature = new Creature();
    Ability abilityValidExecutor = new TestAbility(executorCreature);

    // act
    EntityHitEvent event = new EntityHitEvent(hitEntity, abilityValidExecutor, 9001, true);

    // assert
    assertEquals(executorCreature, event.getExecutor());
  }
}
