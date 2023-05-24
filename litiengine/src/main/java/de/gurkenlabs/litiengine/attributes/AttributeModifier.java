package de.gurkenlabs.litiengine.attributes;

import java.io.Serializable;

/**
 * The AttributeModifier class represents a modifier that can be applied to an {@code Attribute}.
 * The modifier applies the specified modification to the attribute's value by the specified modify
 * value.
 *
 * @param <T> the type of the attribute being modified
 * @see Attribute#addModifier(AttributeModifier)
 * @see Attribute#modifyBaseValue(AttributeModifier)
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>>,
  Serializable {

  private final Modification modification;
  private double modifyValue;
  private boolean active;

  /**
   * Initializes a new instance of the {@code AttributeModifier} class.
   *
   * @param mod         The modification type to be applied by this modifier.
   * @param modifyValue The value by which the attribute will be modified.
   */
  public AttributeModifier(final Modification mod, final double modifyValue) {
    this.modification = mod;
    this.modifyValue = modifyValue;
    this.active = true;
  }

  /**
   * Compares this attribute modifier to another attribute modifier based on their apply order.
   *
   * @param otherModifier The other attribute modifier to compare to.
   * @return An integer value indicating the relative ordering of the modifiers.
   */
  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(this.getModification().getApplyOrder(),
      otherModifier.getModification().getApplyOrder());
  }

  /**
   * Determines whether this attribute modifier is equal to another object.
   *
   * @param obj The object to compare to.
   * @return {@code true} if the objects are equal, {@code false} otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeModifier<?> attr) {
      return this.isActive() == attr.isActive() && this.getModification() == attr.getModification()
        && this.getModifyValue() == attr.getModifyValue();
    }

    return super.equals(obj);
  }

  /**
   * Generates a hash code value for this attribute modifier.
   *
   * @return The hash code value for this attribute modifier.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Gets the modification type applied by this modifier.
   *
   * @return The modification type applied by this modifier.
   */
  public Modification getModification() {
    return this.modification;
  }

  /**
   * Gets the value that is used to modify an attribute.
   *
   * @return The value that is used to modify an attribute.
   */
  public double getModifyValue() {
    return this.modifyValue;
  }

  /**
   * Gets the active state of this attribute modifier.
   *
   * @return {@code true} if the modifier is currently active, {@code false} otherwise.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Applies this attribute modifier to the given attribute value.
   *
   * @param modValue The value of the attribute being modified.
   * @return The modified attribute value.
   */
  public T modify(final T modValue) {
    if (!this.isActive()) {
      return modValue;
    }

    return switch (this.getModification()) {
      case ADD -> this.ensureType(modValue.doubleValue() + this.getModifyValue(),
        modValue);
      case SUBTRACT -> this.ensureType(modValue.doubleValue() - this.getModifyValue(),
        modValue);
      case MULTIPLY -> this.ensureType(modValue.doubleValue() * this.getModifyValue(),
        modValue);
      case DIVIDE -> this.ensureType(modValue.doubleValue() / this.getModifyValue(),
        modValue);
      case ADDPERCENT -> this.ensureType(
        modValue.doubleValue() + modValue.doubleValue() / 100 * this.getModifyValue(), modValue);
      case SUBTRACTPERCENT -> this.ensureType(
        modValue.doubleValue() - modValue.doubleValue() / 100 * this.getModifyValue(), modValue);
      case SET -> this.ensureType(this.getModifyValue(), modValue);
      default -> modValue;
    };
  }

  public void setModifyValue(double value) {
    this.modifyValue = value;
  }

  /**
   * Sets the active state of this attribute modifier.
   *
   * @param active {@code true} if the modifier should be active, {@code false} otherwise.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  @SuppressWarnings("unchecked")
  private T ensureType(final Double modValue, final T originalValue) {
    if (originalValue instanceof Double) {
      return (T) modValue;
    } else if (originalValue instanceof Float) {
      return (T) Float.valueOf(modValue.floatValue());
    } else if (originalValue instanceof Long) {
      return (T) Long.valueOf(modValue.longValue());
    } else if (originalValue instanceof Byte) {
      return (T) Byte.valueOf(modValue.byteValue());
    } else if (originalValue instanceof Short) {
      return (T) Short.valueOf(modValue.shortValue());
    } else if (originalValue instanceof Integer) {
      return (T) Integer.valueOf(modValue.intValue());
    }

    return null;
  }
}
