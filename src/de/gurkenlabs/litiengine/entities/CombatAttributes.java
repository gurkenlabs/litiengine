/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

// TODO: Auto-generated Javadoc
/**
 * The Class Attributes.
 */
public class CombatAttributes {

  /** The attack speed. */
  private final Attribute<Float> attackSpeed;

  /** The damage multiplier. */
  private final Attribute<Float> damageMultiplier;

  /** The experience. */
  private final RangeAttribute<Integer> experience;

  /** The health. */
  private final RangeAttribute<Short> health;

  /** The health regeneration. */
  private final Attribute<Byte> healthRegeneration;

  /** The level. */
  private final RangeAttribute<Byte> level;

  /** The shield. */
  private final RangeAttribute<Short> shield;

  /** The velocity. */
  private final Attribute<Float> velocity;

  /**
   * Instantiates a new attributes.
   *
   * @param info
   *          the info
   */
  public CombatAttributes(final CombatAttributesInfo info) {
    // init range attributes
    this.health = new RangeAttribute<Short>(info.health(), (short) 0, info.health());
    this.shield = new RangeAttribute<Short>(info.maxShield(), (short) 0, info.shield());
    this.level = new RangeAttribute<Byte>(info.maxLevel(), (byte) 0, info.level());
    this.experience = new RangeAttribute<Integer>(info.maxExperience(), 0, info.experience());

    // init single value attributes
    this.velocity = new Attribute<Float>(info.velocityFactor());
    this.attackSpeed = new Attribute<Float>(info.attackSpeed());
    this.damageMultiplier = new Attribute<Float>(info.damageMultiplier());
    this.healthRegeneration = new Attribute<Byte>(info.healthRegenerationPerSecond());

  }

  /**
   * Gets the attack speed.
   *
   * @return the attack speed
   */
  public Attribute<Float> getAttackSpeed() {
    return this.attackSpeed;
  }

  /**
   * Gets the damage multiplier.
   *
   * @return the damage multiplier
   */
  public Attribute<Float> getDamageMultiplier() {
    return this.damageMultiplier;
  }

  /**
   * Gets the experience.
   *
   * @return the experience
   */
  public RangeAttribute<Integer> getExperience() {
    return this.experience;
  }

  /**
   * Gets the health.
   *
   * @return the health
   */
  public RangeAttribute<Short> getHealth() {
    return this.health;
  }

  /**
   * Gets the health regeneration.
   *
   * @return the health regeneration
   */
  public Attribute<Byte> getHealthRegeneration() {
    return this.healthRegeneration;
  }

  /**
   * Gets the level.
   *
   * @return the level
   */
  public RangeAttribute<Byte> getLevel() {
    return this.level;
  }

  /**
   * Gets the shield.
   *
   * @return the shield
   */
  public RangeAttribute<Short> getShield() {
    return this.shield;
  }

  /**
   * Gets the velocity.
   *
   * @return the velocity
   */
  public Attribute<Float> getVelocity() {
    return this.velocity;
  }
}
