package de.gurkenlabs.litiengine.abilities;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.effects.Effect;

public class AbilityExecution implements IUpdateable {
  private final Ability ability;
  private final List<Effect> appliedEffects;
  private final Point2D castLocation;
  private final long executionTicks;
  private final Shape impactArea;

  /**
   * Instantiates a new ability execution.
   *
   * @param ability
   *          the ability
   */
  AbilityExecution(final Ability ability) {
    this.appliedEffects = new CopyOnWriteArrayList<>();
    this.ability = ability;
    this.executionTicks = Game.time().now();
    this.impactArea = ability.calculateImpactArea();
    this.castLocation = ability.getExecutor().getCenter();
    Game.loop().attach(this);
  }

  public Ability getAbility() {
    return this.ability;
  }

  public List<Effect> getAppliedEffects() {
    return this.appliedEffects;
  }

  public Point2D getCastLocation() {
    return this.castLocation;
  }

  public Shape getExecutionImpactArea() {
    return this.impactArea;
  }

  public long getExecutionTicks() {
    return this.executionTicks;
  }

  /**
   * 1. Apply all ability effects after their delay. 
   * 2. Unregister this instance after all effects were applied. 
   * 3. Effects will apply their follow up effects on their own.
   */
  @Override
  public void update() {
    // if there a no effects to apply -> unregister this instance and we're done
    if (this.getAbility().getEffects().isEmpty() || this.getAbility().getEffects().size() == this.getAppliedEffects().size()) {
      Game.loop().detach(this);
      return;
    }

    // handle all effects from the ability that were not applied yet
    for (final Effect effect : this.getAbility().getEffects()) {
      // if the ability was not executed yet or the delay of the effect is not
      // yet reached
      if (this.getAppliedEffects().contains(effect) || Game.time().since(this.getExecutionTicks()) < effect.getDelay()) {
        continue;
      }

      effect.apply(this.getExecutionImpactArea());
      this.getAppliedEffects().add(effect);
    }
  }
}
