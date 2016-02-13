/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities;

import java.awt.Shape;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;

/**
 * The Class AbilityExecution.
 */
public class AbilityExecution implements IUpdateable {
  private final Map<IEffect, Long> appliedEffects;
  /** The executed ability. */
  private final Ability ability;

  /** The execution ticks. */
  private final long executionTicks;

  /** The impact area. */
  private final Shape impactArea;

  /**
   * Instantiates a new ability execution.
   *
   * @param ability
   *          the ability
   */
  public AbilityExecution(final IGameLoop gameLoop, final Ability ability) {
    this.appliedEffects = new ConcurrentHashMap<>();

    this.ability = ability;
    this.executionTicks = gameLoop.getTicks();
    this.impactArea = ability.calculateImpactArea();
    gameLoop.registerForUpdate(this);
  }

  /**
   * Gets the executed ability.
   *
   * @return the executed ability
   */
  public Ability getAbility() {
    return this.ability;
  }

  public Map<IEffect, Long> getAppliedEffects() {
    return this.appliedEffects;
  }

  /**
   * Gets the impact area.
   *
   * @return the impact area
   */
  public Shape getExecutionImpactArea() {
    return this.impactArea;
  }

  /**
   * Gets the ticks.
   *
   * @return the ticks
   */
  public long getExecutionTicks() {
    return this.executionTicks;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update(final IGameLoop loop) {
    if (this.getAbility().getEffects().size() == 0) {
      loop.unregisterFromUpdate(this);
      return;
    }

    // only execute effects once and during the duration
    if (this.allEffectsAreFinished(loop)) {
      for (final IEffect effect : this.getAppliedEffects().keySet()) {
        loop.unregisterFromUpdate(effect);
        effect.cease();
      }

      loop.unregisterFromUpdate(this);
      return;
    }

    // handle already applied effects
    for (final IEffect effect : this.getAppliedEffects().keySet()) {
      // while the duration + delay of an effect is not reached or an effect
      // without duration is still active
      // effects without a duration are cancelled after the abiltity duration
      if (effect.getDuration() == IEffect.NO_DURATION && effect.isActive() && loop.getDeltaTime(this.getAppliedEffects().get(effect)) < this.getAbility().getAttributes().getDuration().getCurrentValue()
          || effect.getDuration() != IEffect.NO_DURATION && loop.getDeltaTime(this.getAppliedEffects().get(effect)) < effect.getDuration() + effect.getDelay()) {
        continue;
      }

      // cease the effect when the total duration is reached
      loop.unregisterFromUpdate(effect);
      effect.cease();

      // execute all follow up effects
      effect.getFollowUpEffects().forEach(followUp -> {
        followUp.apply(this.getExecutionImpactArea());
        loop.registerForUpdate(followUp);
        this.getAppliedEffects().put(followUp, loop.getTicks());
      });
    }

    // TODO: Take effect appliance type into consideration...
    // ONCAST, ONPROJECTILEHIT, ONTICK
    // handle all effects from the ability that were not applied yet
    for (final IEffect effect : this.getAbility().getEffects()) {
      if (this.getAppliedEffects().containsKey(effect)) {
        continue;
      }

      // if the ability was not executed yet or the delay of the effect is not
      // yet reached
      if (loop.getDeltaTime(this.getExecutionTicks()) < effect.getDelay()) {
        continue;
      }

      effect.apply(this.getExecutionImpactArea());
      loop.registerForUpdate(effect);
      this.getAppliedEffects().put(effect, loop.getTicks());
    }
  }

  private boolean hasFinished(final IGameLoop loop, final IEffect effect) {
    if(effect.getDuration() == IEffect.NO_DURATION ){
      if(!this.getAppliedEffects().containsKey(effect) || effect.isActive()){
        return false;
      }
    }else{
      if(!this.getAppliedEffects().containsKey(effect) || loop.getDeltaTime(this.getAppliedEffects().get(effect)) < effect.getDelay() + effect.getDuration()) {
        return false;
      }
    }

    // Recursively called for all follow ups
    for (final IEffect followUp : effect.getFollowUpEffects()) {
      if (!this.hasFinished(loop, followUp)) {
        return false;
      }
    }

    return true;
  }

  /**
   * All effects were applied.
   *
   * @return true, if successful
   */
  protected boolean allEffectsAreFinished(final IGameLoop loop) {
    for (final IEffect effect : this.getAbility().getEffects()) {
      if (!this.hasFinished(loop, effect)) {
        return false;
      }
    }

    return true;
  }
}
