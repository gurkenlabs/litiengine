/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

/**
 * The Class VelocityEffect.
 */
public class VelocityEffect extends AttributeStateEffect<Float> {

  /**
   * Instantiates a new velocity effect.
   *
   * @param ability
   *          the ability
   * @param velocityDelta
   *          the velocity delta
   * @param targtes
   *          the targtes
   */
  public VelocityEffect(final Ability ability, final float velocityDelta, final Modification modification, final EffectTarget... targtes) {
    super(ability, modification, velocityDelta, targtes);
  }

  @Override
  protected Attribute<Float> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getVelocity();
  }
}
