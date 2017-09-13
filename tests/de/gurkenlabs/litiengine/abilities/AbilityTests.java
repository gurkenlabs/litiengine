package de.gurkenlabs.litiengine.abilities;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

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

  @AbilityInfo(castType = CastType.ONCONFIRM, name = "I do somethin", description = "does somethin", cooldown = 333, duration = 222, impact = 111, impactAngle = 99, multiTarget = true, origin = AbilityOrigin.COLLISIONBOX_CENTER, range = 444, value = 999)
  private class TestAbility extends Ability {

    protected TestAbility(IMovableCombatEntity executor) {
      super(executor);
    }
  }
}
