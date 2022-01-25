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
   * <ol>
   * <li>Apply all ability effects after their delay.</li>
   * <li>Unregister this instance after all effects were applies.</li>
   * <li>Effects will apply their follow up effects on their own.</li>
   * </ol>
   */
  @Override
  public void update() {
    // if there are no effects to apply -> unregister this instance, and we're done
    if (this.getAbility().getEffects().isEmpty() || this.getAbility().getEffects().size() == this.getAppliedEffects().size()) {
      Game.loop().detach(this);
      return;
    }
    this.applyAbilityEffects();
  }

  private void applyAbilityEffects() {
    long gameTicksSinceExecution = Game.time().since(this.getExecutionTicks());
    // ability not executed yet or delay of effect not yet reached
    this.getAbility().getEffects().stream()
        .filter(effect -> !this.getAppliedEffects().contains(effect) && gameTicksSinceExecution >= effect.getDelay())
        .forEach(effect -> {
          effect.apply(this.getExecutionImpactArea());
          this.getAppliedEffects().add(effect);
        });
  }
}
