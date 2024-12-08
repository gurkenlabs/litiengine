package de.gurkenlabs.litiengine.attributes;


import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An attribute modifier allows to modify attributes by the specified Modification and modify value.
 *
 * @param <T>
 *   the generic type
 * @see Attribute#addModifier(AttributeModifier)
 * @see Attribute#modifyBaseValue(AttributeModifier)
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>> {
  private final Collection<AttributeModifierListener> listeners = ConcurrentHashMap.newKeySet();
  private final Modification modification;
  private double modifyValue;
  private boolean active;

  /**
   * Initializes a new instance of the {@code AttributeModifier} class.
   *
   * @param mod
   *   The modification type.
   * @param modifyValue
   *   The modification value to be applied by this instance.
   */
  public AttributeModifier(final Modification mod, final double modifyValue) {
    this.modification = mod;
    this.modifyValue = modifyValue;
    this.active = true;
  }

  /**
   * Compares this attribute modifier to another based on the apply order of their modifications.
   *
   * @param otherModifier
   *   The other attribute modifier to compare.
   * @return An integer representing the comparison result.
   */
  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(getModification().getApplyOrder(), otherModifier.getModification().getApplyOrder());
  }

  /**
   * Checks if this attribute modifier is equal to another object.
   *
   * @param obj
   *   The object to compare with.
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
   * Adds a listener that will be notified when the attribute modifier changes.
   *
   * @param listener
   *   The listener to be added.
   */
  public void onChanged(AttributeModifierListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes a listener so that it will no longer be notified when the attribute modifier changes.
   *
   * @param listener
   *   The listener to be removed.
   */
  public void removeListener(AttributeModifierListener listener) {
    this.listeners.add(listener);
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
   * @param modvalue
   *   The original value to be modified.
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
   * @param value
   *   The new modify value.
   */
  public void setModifyValue(double value) {
    var previous = this.modifyValue;
    this.modifyValue = value;

    if (previous != this.modifyValue) {
      this.fireChangedEvent();
    }
  }

  /**
   * Sets the active status of this attribute modifier.
   *
   * @param active
   *   True to activate, false to deactivate.
   */
  public void setActive(boolean active) {
    var previous = this.active;
    this.active = active;

    if (previous != this.active) {
      this.fireChangedEvent();
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

  private void fireChangedEvent() {
    for (var listener : this.listeners) {
      listener.modifierChanged();
    }
  }
}
