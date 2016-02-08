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
  private final IGameLoop gameLoop;
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
    this.gameLoop = gameLoop;

    this.ability = ability;
    this.executionTicks = this.gameLoop.getTicks();
    this.impactArea = ability.calculateImpactArea();
    this.gameLoop.registerForUpdate(this);
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
  public void update() {
    if (this.getAbility().getEffects().size() == 0) {
      this.gameLoop.unregisterFromUpdate(this);
      return;
    }

    // only execute effects once and during the duration
    if (this.allEffectsAreFinished()) {
      for (final IEffect effect : this.getAppliedEffects().keySet()) {
        effect.cease();
      }

      this.gameLoop.unregisterFromUpdate(this);
      return;
    }

    // handle already applied effects
    for (final IEffect effect : this.getAppliedEffects().keySet()) {
      // while the duration + delay of an effect is not reached
      if (this.gameLoop.getDeltaTime(this.getAppliedEffects().get(effect)) < effect.getDuration() + effect.getDelay()) {
        continue;
      }

      if (effect.isActive()) {
        // cease the effect when the total duration is reached
        effect.cease();

        // execute all follow up effects
        effect.getFollowUpEffects().forEach(followUp -> {
          followUp.apply(this.getExecutionImpactArea());
          this.getAppliedEffects().put(followUp, this.gameLoop.getTicks());
        });
      }
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
      if (this.gameLoop.getDeltaTime(this.getExecutionTicks()) < effect.getDelay()) {
        continue;
      }

      effect.apply(this.getExecutionImpactArea());
      this.getAppliedEffects().put(effect, this.gameLoop.getTicks());
    }
  }

  private boolean hasFinished(final IEffect effect) {
    if (!this.getAppliedEffects().containsKey(effect) || this.gameLoop.getDeltaTime(this.getAppliedEffects().get(effect)) < effect.getDelay() + effect.getDuration()) {
      return false;
    }

    // Recursively called for all follow ups
    for (final IEffect followUp : effect.getFollowUpEffects()) {
      if (!this.hasFinished(followUp)) {
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
  protected boolean allEffectsAreFinished() {
    for (final IEffect effect : this.getAbility().getEffects()) {
      if (!this.hasFinished(effect)) {
        return false;
      }
    }

    return true;
  }
}
