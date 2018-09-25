package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

/**
 * The Class CombatAttributes contains attributes for an ICombatEntity that have
 * impact on the combat behavior.
 */
public class CombatAttributes {

  private final Attribute<Float> attackSpeed;
  private final Attribute<Float> damageMultiplier;
  private final RangeAttribute<Integer> health;
  private final Attribute<Byte> healthRegeneration;
  private final RangeAttribute<Short> shield;
  private final Attribute<Float> velocity;
  private final Attribute<Integer> vision;

  /**
   * Instantiates a new attributes.
   *
   * @param info
   *          the info
   */
  public CombatAttributes(final CombatAttributesInfo info) {

    // init range attributes
    this.health = new RangeAttribute<>(info.health(), 0, info.health());
    this.shield = new RangeAttribute<>(info.maxShield(), (short) 0, info.shield());

    // init single value attributes
    this.velocity = new Attribute<>(info.velocityFactor());
    this.attackSpeed = new Attribute<>(info.attackSpeed());
    this.damageMultiplier = new Attribute<>(info.damageMultiplier());
    this.healthRegeneration = new Attribute<>(info.healthRegenerationPerSecond());
    this.vision = new Attribute<>(info.vision());
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
   * Gets the health.
   *
   * @return the health
   */
  public RangeAttribute<Integer> getHealth() {
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

  /**
   * The vision radius of the combat entity.
   * 
   * @return The {@link Attribute} that corresponds to the entity vision.
   */
  public Attribute<Integer> getVision() {
    return this.vision;
  }
}
