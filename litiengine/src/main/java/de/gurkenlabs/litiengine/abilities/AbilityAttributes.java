package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.attributes.Attribute;

/**
 * The {@code AbilityAttributes} class represents the attributes of an ability in the game. Each ability has a set of attributes such as cooldown,
 * duration, impact, impact angle, range, and value.
 */
public class AbilityAttributes {
  private final Attribute<Integer> cooldown;
  private final Attribute<Integer> duration;
  private final Attribute<Integer> impact;
  private final Attribute<Integer> impactAngle;
  private final Attribute<Integer> range;
  private final Attribute<Integer> value;

  /**
   * Initializes a new instance of the {@code AbilityAttributes} class.
   *
   * @param info The information of the ability
   */
  AbilityAttributes(final AbilityInfo info) {
    this.cooldown = new Attribute<>(info.cooldown());
    this.range = new Attribute<>(info.range());
    this.impact = new Attribute<>(info.impact());
    this.duration = new Attribute<>(info.duration());
    this.value = new Attribute<>(info.value());
    this.impactAngle = new Attribute<>(info.impactAngle());
  }

  /**
   * Gets the cooldown attribute of this ability.
   *
   * @return The cooldown attribute of this ability
   */
  public Attribute<Integer> cooldown() {
    return this.cooldown;
  }

  /**
   * Gets the duration attribute of this ability.
   *
   * @return The duration attribute of this ability
   */
  public Attribute<Integer> duration() {
    return this.duration;
  }

  /**
   * Gets the impact attribute of this ability.
   *
   * @return The impact attribute of this ability
   */
  public Attribute<Integer> impact() {
    return this.impact;
  }

  /**
   * Gets the impact angle attribute of this ability.
   *
   * @return The impact angle attribute of this ability
   */
  public Attribute<Integer> impactAngle() {
    return this.impactAngle;
  }

  /**
   * Gets the range attribute of this ability.
   *
   * @return The range attribute of this ability
   */
  public Attribute<Integer> range() {
    return this.range;
  }

  /**
   * Gets the value attribute of this ability.
   *
   * @return The value attribute of this ability
   */
  public Attribute<Integer> value() {
    return this.value;
  }
}
