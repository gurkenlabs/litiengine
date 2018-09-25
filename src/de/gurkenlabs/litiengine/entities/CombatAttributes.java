package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

/**
 * The Class CombatAttributes contains attributes for an ICombatEntity that have
 * impact on the combat behavior.
 */
public class CombatAttributes {
  private final Attribute<Float> damageMultiplier;
  private final RangeAttribute<Integer> health;

  /**
   * Instantiates a new attributes.
   *
   * @param info
   *          the info
   */
  public CombatAttributes(final CombatAttributesInfo info) {

    // init range attributes
    this.health = new RangeAttribute<>(info.health(), 0, info.health());

    // init single value attributes
    this.damageMultiplier = new Attribute<>(info.damageMultiplier());
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
}
