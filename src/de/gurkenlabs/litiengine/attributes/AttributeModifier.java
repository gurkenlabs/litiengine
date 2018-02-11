package de.gurkenlabs.litiengine.attributes;

/**
 * An attribute modifier allows to modify a specific CombatAttribute by the
 * specified Modification and the specified modifyValue.
 *
 * @param <T>
 *          the generic type
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>> {
  private final Modification modification;
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

  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(this.getModification().getApplyOrder(), otherModifier.getModification().getApplyOrder());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeModifier<?>) {
      AttributeModifier<?> attr = (AttributeModifier<?>) obj;
      return this.getModification() == attr.getModification() && this.getModifyValue() == attr.getModifyValue();
    }

    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public Modification getModification() {
    return this.modification;
  }

  public double getModifyValue() {
    return this.modifyValue;
  }

  public T modify(final T modvalue) {
    switch (this.getModification()) {
    case ADD:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() + this.getModifyValue()), modvalue);
    case SUBSTRACT:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() - this.getModifyValue()), modvalue);
    case MULTIPLY:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() * this.getModifyValue()), modvalue);
    case DIVIDE:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() / this.getModifyValue()), modvalue);
    case ADDPERCENT:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() + modvalue.doubleValue() / 100 * this.getModifyValue()), modvalue);
    case SUBSTRACTPERCENT:
      return this.ensureType(Double.valueOf(modvalue.doubleValue() - modvalue.doubleValue() / 100 * this.getModifyValue()), modvalue);
    case SET:
      return this.ensureType(Double.valueOf(this.getModifyValue()), modvalue);
    case UNKNOWN:
    default:
      return modvalue;
    }
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
