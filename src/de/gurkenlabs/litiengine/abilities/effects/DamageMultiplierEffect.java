/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

/**
 * The Class DamageMultiplierEffect.
 */
public class DamageMultiplierEffect extends AttributeStateEffect<Float> {

  /**
   * Instantiates a new damage multiplier effect.
   *
   * @param ability
   *          the ability
   * @param delta
   *          the delta
   * @param targtes
   *          the targtes
   */
  public DamageMultiplierEffect(final IEnvironment environment, final Ability ability, final Float delta, final Modification modification, final EffectTarget... targtes) {
    super(environment, ability, modification, delta, targtes);
  }
  
  @Override
  protected Attribute<Float> getAttribute(ICombatEntity entity) {
    return entity.getAttributes().getDamageMultiplier();
  }
}