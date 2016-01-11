/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.attributes;

// TODO: Auto-generated Javadoc
/**
 * The Class AttributeModifier.
 *
 * @param <T>
 *          the generic type
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>> {

  /** The modification. */
  private final Modification modification;

  /** The modify value. */
  private final double modifyValue;

  /**
   * Instantiates a new attribute modifier.
   *
   * @param mod
   *          the mod
   * @param modifyValue
   *          the modify value
   */
  public AttributeModifier(final Modification mod, final double modifyValue) {
    this.modification = mod;
    this.modifyValue = modifyValue;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(this.getModification().getApplyOrder(), otherModifier.getModification().getApplyOrder());
  }

  /**
   * Gets the modification.
   *
   * @return the modification
   */
  public Modification getModification() {
    return this.modification;
  }

  /**
   * Gets the modify value.
   *
   * @return the modify value
   */
  public double getModifyValue() {
    return this.modifyValue;
  }

  /**
   * Modify.
   *
   * @param modvalue
   *          the modvalue
   * @return the t
   */
  public T modify(final T modvalue) {
    switch (this.getModification()) {
    case Add:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() + this.getModifyValue()), modvalue);
    case Substract:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() - this.getModifyValue()), modvalue);
    case Multiply:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() * this.getModifyValue()), modvalue);
    case Divide:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() / this.getModifyValue()), modvalue);
    case AddPercent:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() + modvalue.doubleValue() / 100 * this.getModifyValue()), modvalue);
    case SubstractPercent:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() - modvalue.doubleValue() / 100 * this.getModifyValue()), modvalue);
    case Set:
      return this.ensureType(Double.valueOf(this.getModifyValue()), modvalue);
    case Unknown:
    default:
      return modvalue;
    }
  }

  /**
   * Ensure type.
   *
   * @param modValue
   *          the mod value
   * @param originalValue
   *          the original value
   * @return the t
   */
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
