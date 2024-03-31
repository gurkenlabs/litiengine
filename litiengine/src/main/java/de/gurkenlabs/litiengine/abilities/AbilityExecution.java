package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code AbilityExecution} class represents the execution of an ability in the game. It contains information about the ability, its effects, and
 * the location and time of its execution.
 */
public class AbilityExecution implements IUpdateable {
  private final Ability ability;
  private final List<Effect> appliedEffects;
  private final Point2D castLocation;
  private final long executionTicks;
  private final Shape impactArea;

  /**
   * Initializes a new instance of the {@code AbilityExecution} class.
   *
   * @param ability The ability to be executed
   */
  AbilityExecution(final Ability ability) {
    this.appliedEffects = new CopyOnWriteArrayList<>();
    this.ability = ability;
    this.executionTicks = Game.time().now();
    this.impactArea = ability.calculateImpactArea();
    this.castLocation = ability.getExecutor().getCenter();
    Game.loop().attach(this);
  }

  /**
   * Gets the ability being executed.
   *
   * @return The ability being executed
   */
  public Ability getAbility() {
    return this.ability;
  }

  /**
   * Gets the effects that have been applied during this execution.
   *
   * @return The effects that have been applied
   */
  public List<Effect> getAppliedEffects() {
    return this.appliedEffects;
  }

  /**
   * Gets the location where the ability was cast.
   *
   * @return The location where the ability was cast
   */
  public Point2D getCastLocation() {
    return this.castLocation;
  }

  /**
   * Gets the impact area of the ability execution.
   *
   * @return The impact area of the ability execution
   */
  public Shape getExecutionImpactArea() {
    return this.impactArea;
  }

  /**
   * Gets the time (in ticks) when the ability was executed.
   *
   * @return The time in ticks when the ability was executed
   */
  public long getExecutionTicks() {
    return this.executionTicks;
  }

  /**
   * Updates the state of this ability execution. This method applies all ability effects after their delay and unregisters this instance after all
   * effects were applied. Effects will apply their follow up effects on their own.
   */
  @Override
  public void update() {
    // if there are no effects to apply -> unregister this instance and we're done
    if (this.getAbility().getEffects().isEmpty() || this.getAbility().getEffects().size() == this.getAppliedEffects().size()) {
      Game.loop().detach(this);
      return;
    }
    this.applyAbilityEffects();
  }

  /**
   * Applies the effects of the ability. This method filters the effects that have not been applied yet and whose delay has passed, and applies them.
   */
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
