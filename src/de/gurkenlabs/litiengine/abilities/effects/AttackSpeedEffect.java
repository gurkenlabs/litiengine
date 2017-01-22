/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;

/**
 * This effect allows to modify the entitie's attack speed.
 */
public class AttackSpeedEffect extends AttributeStateEffect<Float> {

  /**
   * Instantiates a new attack speed effect.
   *
   * @param ability
   *          the ability
   * @param delta
   *          the delta
   * @param targtes
   *          the targtes
   */
  public AttackSpeedEffect(final IEnvironment environment, final Ability ability, final float delta, final Modification modifictaion, final EffectTarget... targtes) {
    super(environment, ability, Modification.Multiply, delta, targtes);
  }

  @Override
  protected Attribute<Float> getAttribute(final ICombatEntity entity) {
    return entity.getAttributes().getAttackSpeed();
  }
}
