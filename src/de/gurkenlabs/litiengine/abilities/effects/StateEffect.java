/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.environment.IEnvironment;

// TODO: Auto-generated Javadoc
/**
 * The Class StateEffect.
 */
public abstract class StateEffect extends Effect {
  /**
   * Instantiates a new state effect.
   *
   * @param ability
   *          the ability
   * @param targtes
   *          the targtes
   */
  protected StateEffect(final IEnvironment environment, final Ability ability, final EffectTarget... targtes) {
    super(environment, ability, targtes);
  }
}
