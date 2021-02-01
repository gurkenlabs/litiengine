package com.litiengine.abilities;

import com.litiengine.Game;
import com.litiengine.GameTest;
import com.litiengine.abilities.effects.Effect;
import com.litiengine.abilities.effects.EffectTarget;
import com.litiengine.entities.Creature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AbilityExecutionTest {

    @BeforeEach
    public void setup() {
        Game.init(Game.COMMADLINE_ARG_NOGUI);
    }

    @AfterEach
    void tearDown() {
        GameTest.resetGame();
    }

    @Test
    void initAbilityExecution() {
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature);
        TestEffect testEffect = new TestEffect(testAbility);
        testAbility.addEffect(testEffect);
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        assertTrue(testAbilityExecution.getAppliedEffects().isEmpty());
        assertEquals(testAbility, testAbilityExecution.getAbility());
        assertEquals(0L, testAbilityExecution.getExecutionTicks());
        assertTrue(Objects.nonNull(testAbilityExecution.getExecutionImpactArea()));
        assertTrue(Objects.nonNull(testAbilityExecution.getCastLocation()));
    }

    @Test
    void update() {
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature);
        TestEffect testEffect = new TestEffect(testAbility);
        testAbility.addEffect(testEffect);

        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertFalse(testAbilityExecution.getAppliedEffects().contains(testEffect));

        testAbilityExecution.update();

        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));
    }

    @Test
    void updateWithAlreadyAppliedEffects() {
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature);
        TestEffect testEffect = new TestEffect(testAbility);
        testAbility.addEffect(testEffect);

        TestEffect testOtherEffect = new TestEffect(testAbility);
        testAbility.addEffect(testOtherEffect);

        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);
        testAbilityExecution.getAppliedEffects().add(testEffect);

        assertEquals(3, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));

        testAbilityExecution.update();

        assertEquals(4, Game.loop().getUpdatableCount());
        assertTrue(testAbilityExecution.getAppliedEffects().contains(testEffect));
    }

    @Test
    void updateWithNoEffects() {
        Creature testCreature = new Creature();
        TestAbility testAbility = new TestAbility(testCreature);
        AbilityExecution testAbilityExecution = new AbilityExecution(testAbility);

        assertEquals(3, Game.loop().getUpdatableCount());

        testAbilityExecution.update();

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