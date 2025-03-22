package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The PropertyModifier class represents a modifier that can be applied to a property. It supports various types of modifications such as addition,
 * subtraction, multiplication, division, and setting a value. The class implements the Comparable interface to allow comparison based on the
 * modification's apply order.
 *
 * @param <T> the type of the number that this modifier can modify
 */
public class AttributeModifier<T extends Number> implements Comparable<AttributeModifier<T>> {
  private final PropertyChangeSupport support;
  private final Modification modification;
  private double modifyValue;
  private boolean active;

  /**
   * Constructs a new PropertyModifier with the specified modification type and value.
   *
   * @param mod         the type of modification to be applied
   * @param modifyValue the value to be used for the modification
   */
  public AttributeModifier(final Modification mod, final double modifyValue) {
    this.modification = mod;
    this.modifyValue = modifyValue;
    this.active = true;
    this.support = new PropertyChangeSupport(this);
  }

  /**
   * Compares this PropertyModifier with the specified PropertyModifier for order. Returns a negative integer, zero, or a positive integer as this
   * PropertyModifier's apply order is less than, equal to, or greater than the specified PropertyModifier's apply order.
   *
   * @param otherModifier the PropertyModifier to be compared
   * @return a negative integer, zero, or a positive integer as this PropertyModifier is less than, equal to, or greater than the specified
   * PropertyModifier
   */
  @Override
  public int compareTo(final AttributeModifier<T> otherModifier) {
    return Integer.compare(getModification().getApplyOrder(), otherModifier.getModification().getApplyOrder());
  }

  /**
   * Checks if this PropertyModifier is equal to the specified object. Two PropertyModifiers are considered equal if they have the same active state,
   * modification type, and modification value.
   *
   * @param obj the object to compare with
   * @return true if the specified object is equal to this PropertyModifier, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeModifier<?> am) {
      return isActive() == am.isActive() &&
        getModification() == am.getModification() &&
        getModifyValue() == am.getModifyValue();
    }
    return super.equals(obj);
  }

  /**
   * Adds a PropertyChangeListener to the listener list.
   *
   * @param listener the PropertyChangeListener to be added
   */
  public void addListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener from the listener list.
   *
   * @param listener the PropertyChangeListener to be removed
   */
  public void removeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Gets the type of modification to be applied.
   *
   * @return the modification type
   */
  public Modification getModification() {
    return this.modification;
  }

  /**
   * Gets the value to be used for the modification.
   *
   * @return the modification value
   */
  public double getModifyValue() {
    return modifyValue;
  }

  /**
   * Sets the modification value for this PropertyModifier and fires a property change event if the value changes.
   *
   * @param value the new modification value
   */
  public void setModifyValue(double value) {
    double oldValue = this.modifyValue;
    this.modifyValue = value;

    if (oldValue != this.modifyValue) {
      support.firePropertyChange("modifyValue", oldValue, this.modifyValue);
    }
  }

  /**
   * Checks if this PropertyModifier is currently active.
   *
   * @return true if this PropertyModifier is active, false otherwise
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the active state of this PropertyModifier and fires a property change event if the state changes.
   *
   * @param active the new active state
   */
  public void setActive(boolean active) {
    boolean oldActive = this.active;
    this.active = active;

    if (oldActive != this.active) {
      support.firePropertyChange("active", oldActive, this.active);
    }
  }

  /**
   * Modifies the given value based on the modification type and value of this PropertyModifier. If the PropertyModifier is not active, the original
   * value is returned.
   *
   * @param modValue the value to be modified
   * @return the modified value
   */
  public T modify(final T modValue) {
    if (!this.isActive()) {
      return modValue;
    }

    return switch (getModification()) {
      case ADD -> ensureType(modValue.doubleValue() + getModifyValue(), modValue);
      case SUBTRACT -> ensureType(modValue.doubleValue() - getModifyValue(), modValue);
      case MULTIPLY -> ensureType(modValue.doubleValue() * getModifyValue(), modValue);
      case DIVIDE -> ensureType(modValue.doubleValue() / getModifyValue(), modValue);
      case SET -> ensureType(getModifyValue(), modValue);
      default -> modValue;
    };
  }

  /**
   * Ensures that the modified value is of the same type as the original value.
   *
   * @param modValue      the modified value as a Double
   * @param originalValue the original value to determine the type
   * @return the modified value cast to the type of the original value, or null if the type is not supported
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
