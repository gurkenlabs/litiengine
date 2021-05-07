package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Shape;
import java.util.Arrays;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EffectTests {

  private Creature creature;
  private TestEffect testEffect;
  private Effect.EffectCeasedListener listener;

  @BeforeEach
  public void setUp() {
    // arrange
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    creature = new Creature();
    TestAbility ability = new TestAbility(creature);
    testEffect = new TestEffect(ability);
    listener = mock(Effect.EffectCeasedListener.class);
    testEffect.onEffectCeased(listener);
  }

  @Test
  void testCease_combatEntity() {
    // act
    testEffect.cease(creature);

    // assert
    verify(listener, times(1)).ceased(any());
  }

  @Test
  void testCease_effectApplication() {
    // arrange
    Shape shape = mock(Shape.class);

    EffectApplication effectApplication = new EffectApplication(
        Arrays.asList(new Creature(), new Creature(), new Creature()), shape);

    // act
    testEffect.cease(effectApplication);

    // assert
    verify(listener, times(3)).ceased(any());
  }

  /**
   * Same as in the AbilityExecutionTests
   */
  private static class TestAbility extends Ability {
    protected TestAbility(Creature executor) {
      super(executor);
    }
  }

  /**
   * Same as in the AbilityExecutionTests
   */
  private static class TestEffect extends Effect {
    protected TestEffect(Ability ability, EffectTarget... targets) {
      super(ability, targets);
    }
  }
}
