package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

/**
 * An attribute effect appies an attribute modifier to the affected entity when
 * applied and removes it when ceased.
 *
 * @param <T>
 *          the generic type
 */
public abstract class AttributeStateEffect<T extends Number> extends StateEffect {

  /** The modifier. */
  private final AttributeModifier<T> modifier;

  /**
   * Instantiates a new attribute effect.
   *
   * @param ability
   *          the ability
   * @param modification
   *          the modification
   * @param delta
   *          the delta
   * @param targtes
   *          the targtes
   */
  protected AttributeStateEffect(final Ability ability, final Modification modification, final double delta, final EffectTarget... targtes) {
    super(ability, targtes);
    this.modifier = new AttributeModifier<>(modification, delta);
  }

  @Override
  public void cease(final ICombatEntity affectedEntity) {
    super.cease(affectedEntity);
    this.getAttribute(affectedEntity).removeModifier(this.getModifier());
  }

  /**
   * Gets the modifier.
   *
   * @return the modifier
   */
  public AttributeModifier<T> getModifier() {
    return this.modifier;
  }

  @Override
  protected void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    this.getAttribute(affectedEntity).addModifier(this.getModifier());
  }

  protected abstract Attribute<T> getAttribute(final ICombatEntity entity);
}
