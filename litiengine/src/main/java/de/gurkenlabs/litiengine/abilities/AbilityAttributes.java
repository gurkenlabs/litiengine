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
  public AbilityAttributes(final AbilityInfo info) {
    this(info.cooldown(), info.duration(), info.range(), info.impact(), info.impactAngle(), info.value());
  }

  /**
   * Initializes a new instance of the {@code AbilityAttributes} class with the specified attribute values.
   *
   * @param cooldown    The cooldown value of the ability
   * @param duration    The duration value of the ability
   * @param range       The range value of the ability
   * @param impact      The impact value of the ability
   * @param impactAngle The impact angle value of the ability
   * @param value       The value of the ability
   */
  public AbilityAttributes(int cooldown, int duration, int range, int impact, int impactAngle, int value) {
    this.cooldown = new Attribute<>(cooldown);
    this.duration = new Attribute<>(duration);
    this.range = new Attribute<>(range);
    this.impact = new Attribute<>(impact);
    this.impactAngle = new Attribute<>(impactAngle);
    this.value = new Attribute<>(value);
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

  /**
   * Copies the values from another `AbilityAttributes` instance to this instance.
   *
   * @param otherAttributes the `AbilityAttributes` instance from which to copy values
   */
  public void copyValues(AbilityAttributes otherAttributes) {
    cooldown().setBaseValue(otherAttributes.cooldown().getBase());
    duration().setBaseValue(otherAttributes.duration().getBase());
    impact().setBaseValue(otherAttributes.impact().getBase());
    impactAngle().setBaseValue(otherAttributes.impactAngle().getBase());
    range().setBaseValue(otherAttributes.range().getBase());
    value().setBaseValue(otherAttributes.value().getBase());
  }
}
