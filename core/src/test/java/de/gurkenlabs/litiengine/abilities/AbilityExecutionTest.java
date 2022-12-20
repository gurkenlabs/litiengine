package de.gurkenlabs.litiengine.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.GameTime;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.test.GameTestSuite;

import java.awt.Shape;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameTestSuite.class)
class AbilityExecutionTest {

  private TestAbility ability;
  private TestEffect effectSpy;

  @BeforeEach
  public void setup() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    Creature testCreature = new Creature();
    ability = new TestAbility(testCreature);
    effectSpy = spy(new TestEffect(ability));
    ability.addEffect(effectSpy);
  }

  @AfterEach
  void tearDown() {
    GameTest.terminateGame();
  }

  @Test
  void testAbilityExecutionInitialization() {
    // act
    AbilityExecution abilityExecution = new AbilityExecution(ability);

    // assert
    assertTrue(abilityExecution.getAppliedEffects().isEmpty());
    assertEquals(ability, abilityExecution.getAbility());
    assertEquals(0L, abilityExecution.getExecutionTicks());
    assertTrue(Objects.nonNull(abilityExecution.getExecutionImpactArea()));
    assertTrue(Objects.nonNull(abilityExecution.getCastLocation()));
  }

  @Test
  void update_noAppliedEffect() {
    // arrange
    AbilityExecution abilityExecution = new AbilityExecution(ability);

    assertEquals(3, Game.loop().getUpdatableCount());
    assertFalse(abilityExecution.getAppliedEffects().contains(effectSpy));

    // act
    abilityExecution.update();

    // assert
    assertEquals(4, Game.loop().getUpdatableCount());
    assertTrue(abilityExecution.getAppliedEffects().contains(effectSpy));
    verify(effectSpy, times(1)).apply(any(Shape.class));
  }

  @Test
  void update_alreadyAppliedEffect() {
    // arrange
    TestEffect appliedEffectSpy = spy(new TestEffect(ability));
    ability.addEffect(appliedEffectSpy);

    AbilityExecution abilityExecution = new AbilityExecution(ability);
    abilityExecution.getAppliedEffects().add(appliedEffectSpy);

    assertEquals(3, Game.loop().getUpdatableCount());
    assertTrue(abilityExecution.getAppliedEffects().contains(appliedEffectSpy));

    // act
    abilityExecution.update();

    // assert
    assertEquals(4, Game.loop().getUpdatableCount());
    assertTrue(abilityExecution.getAppliedEffects().contains(appliedEffectSpy));
    verify(appliedEffectSpy, times(0)).apply(any(Shape.class));
    verify(effectSpy, times(1)).apply(any(Shape.class));
  }

  @Test
  void update_delayedEffect() {
    // arrange
    TestEffect delayedEffectSpy = spy(new TestEffect(ability));
    delayedEffectSpy.setDelay(5);
    ability.addEffect(delayedEffectSpy);

    GameTime timeMock = mock(GameTime.class);
    when(timeMock.since(anyLong())).thenReturn(1L);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic.when(Game::time).thenReturn(timeMock);
    gameMockedStatic
        .when(Game::loop)
        .thenCallRealMethod(); // otherwise Game.loop() returns null because of the mock

    AbilityExecution abilityExecution = new AbilityExecution(ability);
    assertEquals(3, Game.loop().getUpdatableCount());

    // act
    abilityExecution.update();

    // assert
    assertEquals(4, Game.loop().getUpdatableCount());
    assertTrue(abilityExecution.getAppliedEffects().contains(effectSpy));
    verify(effectSpy, times(1)).apply(any(Shape.class));
    verify(delayedEffectSpy, times(0)).apply(any(Shape.class));

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  void update_noEffects() {
    // arrange
    Creature creature = new Creature();
    TestAbility ability = new TestAbility(creature); // create ability without any effects

    IGameLoop loopSpy = spy(Game.loop());
    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic
        .when(Game::time)
        .thenCallRealMethod(); // otherwise Game.time() returns null because of the mock
    gameMockedStatic.when(Game::loop).thenReturn(loopSpy);

    AbilityExecution abilityExecution = new AbilityExecution(ability);
    assertEquals(3, Game.loop().getUpdatableCount());
    assertTrue(abilityExecution.getAppliedEffects().isEmpty());

    // act
    abilityExecution.update();

    // assert
    assertEquals(2, Game.loop().getUpdatableCount());
    verify(loopSpy, times(1)).detach(abilityExecution);

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  void update_allEffectsApplied() {
    // arrange
    IGameLoop loopSpy = spy(Game.loop());
    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic
        .when(Game::time)
        .thenCallRealMethod(); // otherwise Game.time() returns null because of the mock
    gameMockedStatic.when(Game::loop).thenReturn(loopSpy);

    TestEffect otherEffectSpy = spy(new TestEffect(ability));
    ability.addEffect(otherEffectSpy);

    AbilityExecution testAbilityExecution = new AbilityExecution(ability);
    testAbilityExecution.getAppliedEffects().add(effectSpy);
    testAbilityExecution.getAppliedEffects().add(otherEffectSpy);

    assertEquals(3, Game.loop().getUpdatableCount());
    assertTrue(testAbilityExecution.getAppliedEffects().contains(effectSpy));
    assertTrue(testAbilityExecution.getAppliedEffects().contains(otherEffectSpy));

    // act
    testAbilityExecution.update();

    // assert
    assertEquals(2, Game.loop().getUpdatableCount());
    verify(loopSpy, times(1)).detach(testAbilityExecution);

    // cleanup
    gameMockedStatic.close();
  }

  private class TestAbility extends Ability {
    protected TestAbility(Creature executor) {
      super(executor);
    }
  }

  private class TestEffect extends Effect {
    protected TestEffect(Ability ability, EffectTarget... targets) {
      super(ability, targets);
    }
  }
}
