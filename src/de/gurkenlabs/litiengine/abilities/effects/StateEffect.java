/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;

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
  protected StateEffect(final Ability ability, final EffectTarget... targtes) {
    super(ability, targtes);
  }
}
