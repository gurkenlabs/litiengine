package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.annotation.AbilityInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;

/**
 * The Class AbilityAttributes.
 */
public class AbilityAttributes {

  /** The cooldown. */
  private final Attribute<Integer> cooldown;

  /** The duration. */
  private final Attribute<Integer> duration;

  /** The impact. */
  private final Attribute<Integer> impact;

  /** The impact angle. */
  private final Attribute<Integer> impactAngle;

  /** The range. */
  private final Attribute<Integer> range;

  /** The value. */
  private final Attribute<Integer> value;

  /**
   * Instantiates a new ability attributes.
   *
   * @param info
   *          the info
   */
  public AbilityAttributes(final AbilityInfo info) {
    this.cooldown = new Attribute<>(info.cooldown());
    this.range = new Attribute<>(info.range());
    this.impact = new Attribute<>(info.impact());
    this.duration = new Attribute<>(info.duration());
    this.value = new Attribute<>(info.value());
    this.impactAngle = new Attribute<>(info.impactAngle());
  }

  /**
   * Gets the cooldown.
   *
   * @return the cooldown
   */
  public Attribute<Integer> getCooldown() {
    return this.cooldown;
  }

  /**
   * Gets the duration.
   *
   * @return the duration
   */
  public Attribute<Integer> getDuration() {
    return this.duration;
  }

  /**
   * Gets the impact.
   *
   * @return the impact
   */
  public Attribute<Integer> getImpact() {
    return this.impact;
  }

  /**
   * Gets the impact angle.
   *
   * @return the impact angle
   */
  public Attribute<Integer> getImpactAngle() {
    return this.impactAngle;
  }

  /**
   * Gets the range.
   *
   * @return the range
   */
  public Attribute<Integer> getRange() {
    return this.range;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public Attribute<Integer> getValue() {
    return this.value;
  }
}
