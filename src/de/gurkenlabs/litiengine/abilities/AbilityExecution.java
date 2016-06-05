/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.abilities;

import java.awt.Shape;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;

/**
 * The Class AbilityExecution.
 */
public class AbilityExecution implements IUpdateable {
  private final List<IEffect> appliedEffects;
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
    this.appliedEffects = new CopyOnWriteArrayList<>();

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

  public List<IEffect> getAppliedEffects() {
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

  /**
   * 1. Apply all ability effects after their delay. 2. Unregister this instance
   * after all effects were applied. 3. Effects will apply their follow up
   * effects on their own. (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update(final IGameLoop loop) {
    // if there a no effects to apply -> unregister this instance and we're done
    if (this.getAbility().getEffects().size() == 0 || this.getAbility().getEffects().size() == this.getAppliedEffects().size()) {
      loop.unregisterFromUpdate(this);
      return;
    }

    // handle all effects from the ability that were not applied yet
    for (final IEffect effect : this.getAbility().getEffects()) {
      if (this.getAppliedEffects().contains(effect)) {
        continue;
      }

      // if the ability was not executed yet or the delay of the effect is not
      // yet reached
      if (loop.getDeltaTime(this.getExecutionTicks()) < effect.getDelay()) {
        continue;
      }

      effect.apply(loop, this.getExecutionImpactArea());
      this.getAppliedEffects().add(effect);
    }
  }
}
