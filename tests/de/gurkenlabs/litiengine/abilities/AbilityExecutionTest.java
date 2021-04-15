package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AbilityExecutionTest {

    private TestAbility testAbility;
    private TestEffect testEffect;

    @BeforeEach
    public void setup() {
        Game.init(Game.COMMADLINE_ARG_NOGUI);

        Creature testCreature = new Creature();
        testAbility = new TestAbility(testCreature);
        testEffect = new TestEffect(testAbility);
        testAbility.addEffect(testEffect);
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
    void update() {
        // arrange
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertFalse(testAbilityExecution.getAppliedEffects().contains(testEffect));

        // act
        testAbilityExecution.update();

        // assert
        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));
    }

    @Test
    void updateWithAlreadyAppliedEffects() {
        // arrange
        TestEffect testOtherEffect = new TestEffect(testAbility);
        testAbility.addEffect(testOtherEffect);

        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);
        testAbilityExecution.getAppliedEffects().add(testEffect);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));

        // act
        testAbilityExecution.update();

        // assert
        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));
    }

    @Test
    void updateWithNoEffects() {
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature); // make ability without any effects
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);
        assertEquals(3, Game.loop().getUpdatableCount());

        // act
        testAbilityExecution.update();

        // assert
        assertEquals(2, Game.loop().getUpdatableCount());
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