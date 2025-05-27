package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.targeting.TargetingStrategy;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

/**
 * Represents an effect that modifies an attribute of a combat entity.
 *
 * @param <T> the type of the attribute value
 */
public abstract class AttributeEffect<T extends Number> extends Effect {

  private final AttributeModifier<T> modifier;

  /**
   * Constructs an AttributeEffect with the specified targeting strategy, modification, and delta.
   *
   * @param targetingStrategy the strategy to determine the targets of the effect
   * @param modification      the type of modification to apply to the attribute
   * @param delta             the value to modify the attribute by
   */
  protected AttributeEffect(final TargetingStrategy targetingStrategy, final Modification modification, final double delta) {
    this(targetingStrategy, modification, delta, 0);
  }

  /**
   * Constructs an AttributeEffect with the specified targeting strategy, modification, and delta.
   *
   * @param targetingStrategy the strategy to determine the targets of the effect
   * @param modification      the type of modification to apply to the attribute
   * @param delta             the value to modify the attribute by
   * @param duration          the duration of the effect in milliseconds
   */
  protected AttributeEffect(final TargetingStrategy targetingStrategy, final Modification modification, final double delta,
    final int duration) {
    this(targetingStrategy, null, modification, delta, duration);
  }

  /**
   * Constructs an AttributeEffect with the specified targeting strategy, executing entity, modification, and delta.
   *
   * @param targetingStrategy the strategy to determine the targets of the effect
   * @param executingEntity   the entity executing the effect
   * @param modification      the type of modification to apply to the attribute
   * @param delta             the value to modify the attribute by
   */
  protected AttributeEffect(final TargetingStrategy targetingStrategy, final ICombatEntity executingEntity, final Modification modification,
    final double delta) {
    this(targetingStrategy, executingEntity, modification, delta, 0);
  }

  /**
   * Constructs an AttributeEffect with the specified targeting strategy, executing entity, modification, and delta.
   *
   * @param targetingStrategy the strategy to determine the targets of the effect
   * @param executingEntity   the entity executing the effect
   * @param modification      the type of modification to apply to the attribute
   * @param delta             the value to modify the attribute by
   * @param duration          the duration of the effect in milliseconds
   */
  protected AttributeEffect(final TargetingStrategy targetingStrategy, final ICombatEntity executingEntity, final Modification modification,
    final double delta, final int duration) {
    super(targetingStrategy, executingEntity, duration);
    this.modifier = new AttributeModifier<>(modification, delta);
  }

  /**
   * Ceases the effect on the specified entity, removing the attribute modifier.
   *
   * @param affectedEntity the entity affected by the effect
   */
  @Override public void cease(final ICombatEntity affectedEntity) {
    super.cease(affectedEntity);
    this.getAttribute(affectedEntity).removeModifier(this.getModifier());
  }

  /**
   * Gets the attribute modifier associated with this effect.
   *
   * @return the attribute modifier
   */
  public AttributeModifier<T> getModifier() {
    return this.modifier;
  }

  /**
   * Applies the effect to the specified entity, adding the attribute modifier.
   *
   * @param affectedEntity the entity affected by the effect
   */
  @Override protected void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    if (getAttribute(affectedEntity) == null) {
      return;
    }
    getAttribute(affectedEntity).addModifier(getModifier());
  }

  /**
   * Gets the attribute to be modified for the specified entity.
   *
   * @param entity the entity whose attribute is to be modified
   * @return the attribute to be modified
   */
  protected abstract Attribute<T> getAttribute(final ICombatEntity entity);
}
