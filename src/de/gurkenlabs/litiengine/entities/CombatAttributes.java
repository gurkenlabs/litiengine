package de.gurkenlabs.litiengine.entities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

/**
 * The Class CombatAttributes contains attributes for an ICombatEntity that have
 * impact on the combat behavior.
 */
public class CombatAttributes {

  // 10% increase per level
  private static final float LEVEL_MULTIPLIER = 1.1f;

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

  private final CombatAttributesInfo info;

  /** The level. */
  private final RangeAttribute<Byte> level;

  private final List<Consumer<CombatAttributes>> levelUpConsumer;

  /** The shield. */
  private final RangeAttribute<Short> shield;

  /** The velocity. */
  private final Attribute<Float> velocity;

  private final Attribute<Integer> vision;

  /**
   * Instantiates a new attributes.
   *
   * @param info
   *          the info
   */
  public CombatAttributes(final CombatAttributesInfo info) {
    this.levelUpConsumer = new CopyOnWriteArrayList<>();
    this.info = info;

    // init range attributes
    this.health = new RangeAttribute<>(info.health(), (short) 0, info.health());
    this.shield = new RangeAttribute<>(info.maxShield(), (short) 0, info.shield());
    this.level = new RangeAttribute<>(info.maxLevel(), (byte) 0, info.level());
    this.experience = new RangeAttribute<>(info.maxExperience(), 0, 0);

    // init single value attributes
    this.velocity = new Attribute<>(info.velocityFactor());
    this.attackSpeed = new Attribute<>(info.attackSpeed());
    this.damageMultiplier = new Attribute<>(info.damageMultiplier());
    this.healthRegeneration = new Attribute<>(info.healthRegenerationPerSecond());
    this.vision = new Attribute<>(info.vision());

  }

  public void addXP(final int deltaXP) {
    this.getExperience().modifyBaseValue(new AttributeModifier<Integer>(Modification.ADD, deltaXP));
    if (this.getExperience().getCurrentValue() >= this.getExperience().getMaxValue()) {
      this.levelUp();
    }
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

  /**
   * The vision radius of the combat entity.
   * 
   * @return
   */
  public Attribute<Integer> getVision() {
    return this.vision;
  }

  /**
   * Level up.
   */
  public void levelUp() {
    if (this.getLevel().getCurrentValue() >= this.getLevel().getMaxValue()) {
      return;
    }

    this.getLevel().modifyBaseValue(new AttributeModifier<>(Modification.ADD, 1));
    this.getExperience().modifyBaseValue(new AttributeModifier<>(Modification.SET, 0));
    this.updateAttributes();

    for (final Consumer<CombatAttributes> consumer : this.levelUpConsumer) {
      consumer.accept(this);
    }
  }

  public void onLevelUp(final Consumer<CombatAttributes> consumer) {
    if (!this.levelUpConsumer.contains(consumer)) {
      this.levelUpConsumer.add(consumer);
    }
  }

  /**
   * Update attributes.
   */
  protected void updateAttributes() {
    final float maxXp = (float) (this.info.maxExperience() * Math.sqrt(this.getLevel().getCurrentValue()));

    this.getHealth().modifyMaxBaseValue(new AttributeModifier<>(Modification.MULTIPLY, LEVEL_MULTIPLIER));
    this.getExperience().modifyMaxBaseValue(new AttributeModifier<>(Modification.SET, maxXp));
    this.getShield().modifyMaxBaseValue(new AttributeModifier<>(Modification.MULTIPLY, LEVEL_MULTIPLIER));
    this.getHealthRegeneration().modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, LEVEL_MULTIPLIER));
    this.getDamageMultiplier().modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, LEVEL_MULTIPLIER));
  }
}
