package de.gurkenlabs.litiengine.attributes;

/**
 * An attribute modifier allows to modify attributes by the specified Modification and modify value.
 *
 * @param <T> the generic type
 * @see Attribute#addModifier(AttributeModifier)
 * @see Attribute#modifyBaseValue(AttributeModifier)
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>> {
  private final Modification modification;
  private double modifyValue;
  private boolean active;

  /**
   * Initializes a new instance of the {@code AttributeModifier} class.
   *
   * @param mod         The modification type.
   * @param modifyValue The modification value to be applied by this instance.
   */
  public AttributeModifier(final Modification mod, final double modifyValue) {
    this.modification = mod;
    this.modifyValue = modifyValue;
    this.active = true;
  }

  /**
   * Compares this attribute modifier to another based on the apply order of their modifications.
   *
   * @param otherModifier The other attribute modifier to compare.
   * @return An integer representing the comparison result.
   */
  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(getModification().getApplyOrder(), otherModifier.getModification().getApplyOrder());
  }

  /**
   * Checks if this attribute modifier is equal to another object.
   *
   * @param obj The object to compare with.
   * @return True if the objects are equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeModifier<?> am) {
      return isActive() == am.isActive() && getModification() == am.getModification() && getModifyValue() == am.getModifyValue();
    }

    return super.equals(obj);
  }

  /**
   * Generates a hash code for this attribute modifier.
   *
   * @return The hash code.
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
    return modifyValue;
  }

  /**
   * Checks if this attribute modifier is active.
   *
   * @return True if active, false otherwise.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Modifies the provided value based on the modification type and modify value of this attribute modifier.
   *
   * @param modvalue The original value to be modified.
   * @return The modified value.
   */
  public T modify(final T modvalue) {
    if (!this.isActive()) {
      return modvalue;
    }

    return switch (getModification()) {
      case ADD -> ensureType(modvalue.doubleValue() + getModifyValue(), modvalue);
      case SUBTRACT -> ensureType(modvalue.doubleValue() - getModifyValue(), modvalue);
      case MULTIPLY -> ensureType(modvalue.doubleValue() * getModifyValue(), modvalue);
      case DIVIDE -> ensureType(modvalue.doubleValue() / getModifyValue(), modvalue);
      case SET -> ensureType(getModifyValue(), modvalue);
      default -> modvalue;
    };
  }

  /**
   * Sets the modify value for this attribute modifier.
   *
   * @param value The new modify value.
   */
  public void setModifyValue(double value) {
    this.modifyValue = value;
  }

  /**
   * Sets the active status of this attribute modifier.
   *
   * @param active True to activate, false to deactivate.
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
