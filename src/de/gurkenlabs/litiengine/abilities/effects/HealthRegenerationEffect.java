/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

// TODO: Auto-generated Javadoc
/**
 * The Class HealthRegenerationEffect.
 */
public class HealthRegenerationEffect extends AttributeStateEffect<Byte> {

  /**
   * Instantiates a new health regeneration effect.
   *
   * @param ability
   *          the ability
   * @param healthRegenerationDelta
   *          the health regeneration delta
   * @param targtes
   *          the targtes
   */
  public HealthRegenerationEffect(final IEnvironment environment, final Ability ability, final byte healthRegenerationDelta, final Modification modification, final EffectTarget... targtes) {
    super(environment, ability, modification, healthRegenerationDelta, targtes);
  }
  
  @Override
  protected Attribute<Byte> getAttribute(ICombatEntity entity) {
    return entity.getAttributes().getHealthRegeneration();
  }
}
