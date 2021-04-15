package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.Shape;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AbilityExecutionTest {

    private TestAbility testAbility;
    private TestEffect testEffectSpy;

    @BeforeEach
    public void setup() {
        Game.init(Game.COMMADLINE_ARG_NOGUI);

        Creature testCreature = new Creature();
        testAbility = new TestAbility(testCreature);
        testEffectSpy = spy(new TestEffect(testAbility));
        testAbility.addEffect(testEffectSpy);
    }

    @AfterEach
    void tearDown() {
        GameTest.terminateGame();
    }

    @Test
    void testAbilityExecutionInitialization() {
        // act
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        // assert
        assertTrue(testAbilityExecution.getAppliedEffects().isEmpty());
        assertEquals(testAbility, testAbilityExecution.getAbility());
        assertEquals(0L, testAbilityExecution.getExecutionTicks());
        assertTrue(Objects.nonNull(testAbilityExecution.getExecutionImpactArea()));
        assertTrue(Objects.nonNull(testAbilityExecution.getCastLocation()));
    }

    @Test
    void update_noAppliedEffect() {
        // arrange
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertFalse(testAbilityExecution.getAppliedEffects().contains(testEffectSpy));

        // act
        testAbilityExecution.update();

        // assert
        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffectSpy));
        verify(testEffectSpy, times(1)).apply(any(Shape.class));
    }

    @Test
    void update_alreadyAppliedEffect() {
        // arrange
        TestEffect testOtherEffectSpy = spy(new TestEffect(testAbility));
        testAbility.addEffect(testOtherEffectSpy);

        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);
        testAbilityExecution.getAppliedEffects().add(testEffectSpy);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffectSpy));

        // act
        testAbilityExecution.update();

        // assert
        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffectSpy));
        verify(testEffectSpy, times(0)).apply(any(Shape.class));
        verify(testOtherEffectSpy, times(1)).apply(any(Shape.class));
    }

    @Test
    void update_noEffects() {
        // arrange
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature); // make ability without any effects

        IGameLoop loopSpy = spy(Game.loop());
        MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
        gameMockedStatic.when(Game::time).thenCallRealMethod(); // otherwise Game.time() returns null because of the mock
        gameMockedStatic.when(Game::loop).thenReturn(loopSpy);

        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);
        assertEquals(3, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().isEmpty());

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
