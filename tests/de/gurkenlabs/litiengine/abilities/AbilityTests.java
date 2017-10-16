package de.gurkenlabs.litiengine.abilities;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.annotation.AbilityInfo;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;

public class AbilityTests {

  @Test
  public void testInitialization() {
    IMovableCombatEntity entity = mock(IMovableCombatEntity.class);
    TestAbility ability = new TestAbility(entity);

    Assert.assertEquals("I do somethin", ability.getName());
    Assert.assertEquals("does somethin", ability.getDescription());
    Assert.assertEquals(CastType.ONCONFIRM, ability.getCastType());
    Assert.assertEquals(true, ability.isMultiTarget());

    Assert.assertEquals(333, ability.getAttributes().getCooldown().getCurrentValue().intValue());
    Assert.assertEquals(222, ability.getAttributes().getDuration().getCurrentValue().intValue());
    Assert.assertEquals(111, ability.getAttributes().getImpact().getCurrentValue().intValue());
    Assert.assertEquals(99, ability.getAttributes().getImpactAngle().getCurrentValue().intValue());
    Assert.assertEquals(444, ability.getAttributes().getRange().getCurrentValue().intValue());
    Assert.assertEquals(999, ability.getAttributes().getValue().getCurrentValue().intValue());
  }

  @Test
  public void testEffectInitialization() {
    IMovableCombatEntity entity = mock(IMovableCombatEntity.class);
    TestAbility ability = new TestAbility(entity);

    Effect effect = new TestEffect(ability, EffectTarget.ENEMY);
    Assert.assertEquals(ability.getAttributes().getDuration().getCurrentValue().intValue(), effect.getDuration());
    Assert.assertEquals(ability, effect.getAbility());
    Assert.assertEquals(0, effect.getFollowUpEffects().size());
    Assert.assertFalse(effect.isActive(entity));
    Assert.assertArrayEquals(new EffectTarget[] { EffectTarget.ENEMY }, effect.getEffectTargets());

  }

  @AbilityInfo(castType = CastType.ONCONFIRM, name = "I do somethin", description = "does somethin", cooldown = 333, duration = 222, impact = 111, impactAngle = 99, multiTarget = true, origin = AbilityOrigin.COLLISIONBOX_CENTER, range = 444, value = 999)
  private class TestAbility extends Ability {

    protected TestAbility(IMovableCombatEntity executor) {
      super(executor);
    }
  }

  private class TestEffect extends Effect {
    protected TestEffect(Ability ability, EffectTarget... targets) {
      super(ability, targets);
    }
  }
}
