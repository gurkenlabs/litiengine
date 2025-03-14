package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;

/**
 * The `AbilityEffect` class is an abstract class that represents an effect associated with a specific ability. It extends the `Effect` class and is
 * used to apply effects that are tied to a particular ability.
 * <p>
 * This class provides a way to manage and access the ability that triggers the effect, allowing for the integration of ability-specific attributes
 * such as the executor and duration.
 */
public abstract class AbilityEffect extends Effect {
  private final Ability ability;

  /**
   * Constructs a new `AbilityEffect` with the specified targeting strategy and ability.
   * <p>
   * The effect will inherit the executor and duration attributes from the provided ability.
   *
   * @param targetingStrategy The strategy used to select the targets for this effect.
   * @param ability           The ability associated with this effect, providing information such as the executor and duration.
   */
  protected AbilityEffect(TargetingStrategy targetingStrategy, Ability ability) {
    super(targetingStrategy, ability.getExecutor(), ability.getAttributes().duration().get());
    this.ability = ability;
  }

  /**
   * Gets the ability associated with this effect.
   *
   * @return The ability associated with this effect.
   */
  public Ability getAbility() {
    return ability;
  }
}
