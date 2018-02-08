package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;

public abstract class StateEffect extends Effect {

  protected StateEffect(final Ability ability, final EffectTarget... targtes) {
    super(ability, targtes);
  }
}
