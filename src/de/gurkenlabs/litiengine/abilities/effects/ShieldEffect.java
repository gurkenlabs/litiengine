/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;

/**
 * The Class ShieldEffect.
 */
public class ShieldEffect extends AttributeStateEffect<Short> {

  /**
   * Instantiates a new shield effect.
   *
   * @param ability
   *          the ability
   * @param shieldDelta
   *          the shield delta
   * @param targtes
   *          the targtes
   */
  public ShieldEffect(final IEnvironment environment, final Ability ability, final short shieldDelta, final Modification modifictaion, final EffectTarget... targtes) {
    super(environment, ability, modifictaion, shieldDelta, targtes);
  }

  @Override
  public void cease(final ICombatEntity affectedEntity) {
    final AttributeModifier<Short> revert = new AttributeModifier<>(this.getModifier().getModification(), -this.getModifier().getModifyValue());
    this.getAttribute(affectedEntity).modifyBaseValue(revert);
  }

  @Override
  protected void apply(final ICombatEntity affectedEntity) {
    this.getAttribute(affectedEntity).modifyBaseValue(this.getModifier());
  }

  @Override
  protected Attribute<Short> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getShield();
  }
}
