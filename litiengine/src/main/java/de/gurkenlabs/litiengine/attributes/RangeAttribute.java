package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an attribute with a defined range, extending the base Attribute class. The attribute value is constrained between a minimum and maximum
 * value.
 *
 * @param <T> the type of the attribute value, which must be a Number and Comparable
 */
public class RangeAttribute<T extends Number & Comparable<T>> extends Attribute<T> implements Serializable {
  private static final String MIN_PROPERTY = "min";
  private static final String MAX_PROPERTY = "max";
  private final PropertyChangeSupport minSupport;
  private final PropertyChangeSupport maxSupport;
  private final transient PropertyChangeListener minModifierListener;
  private final transient PropertyChangeListener maxModifierListener;

  private final List<AttributeModifier<T>> minModifiers = new ArrayList<>();
  private final List<AttributeModifier<T>> maxModifiers = new ArrayList<>();
  private T min;
  private T max;

  public RangeAttribute() {
    this(null, null, null);
  }

  /**
   * Constructs a new RangeAttribute with the specified initial value, minimum value, and maximum value. Ensures that the minimum value is not greater
   * than the maximum value.
   *
   * @param initialValue the initial value of the attribute
   * @param min          the minimum value of the attribute
   * @param max          the maximum value of the attribute
   * @throws IllegalArgumentException if the minimum value is greater than the maximum value
   */
  public RangeAttribute(T initialValue, T min, T max) {
    super(initialValue);
    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("min cannot be greater than max");
    }
    this.min = min;
    this.max = max;
    this.minSupport = new PropertyChangeSupport(this);
    this.maxSupport = new PropertyChangeSupport(this);
    this.minModifierListener = evt -> minSupport.firePropertyChange(MIN_PROPERTY, evt.getOldValue(), evt.getNewValue());
    this.maxModifierListener = evt -> maxSupport.firePropertyChange(MAX_PROPERTY, evt.getOldValue(), evt.getNewValue());
  }

  /**
   * Gets the current value of the attribute, ensuring it is within the defined range.
   *
   * @return the current value of the attribute
   */
  @Override public T getModifiedValue() {
    return enforceRangeForValue(super.getModifiedValue());
  }

  /**
   * Gets the base minimum value of the attribute.
   *
   * @return the minimum value of the attribute
   */
  public T getMin() {
    return min;
  }

  /**
   * Computes the modified minimum value by applying all active modifiers to the base minimum value.
   *
   * @return the computed modified minimum value
   */
  public T getModifiedMin() {
    T modifiedMin = min;
    for (AttributeModifier<T> modifier : getMinModifiers()) {
      if (modifier.isActive()) {
        modifiedMin = modifier.modify(modifiedMin);
      }
    }
    return modifiedMin;
  }

  /**
   * Sets the minimum value of the attribute and notifies listeners of the change.
   *
   * @param min the new minimum value
   */
  public void setMin(T min) {
    T oldMin = this.min;
    this.min = min;
    getMinModifiers().clear();
    minSupport.firePropertyChange("min", oldMin, min);
  }

  /**
   * Gets the base maximum value of the attribute.
   *
   * @return the maximum value of the attribute
   */
  public T getMax() {
    return max;
  }

  /**
   * Computes the modified maximum value by applying all active modifiers to the base maximum value.
   *
   * @return the computed modified maximum value
   */
  public T getModifiedMax() {
    T modifiedMax = max;
    for (AttributeModifier<T> modifier : getMaxModifiers()) {
      if (modifier.isActive()) {
        modifiedMax = modifier.modify(modifiedMax);
      }
    }
    return modifiedMax;
  }

  /**
   * Sets the maximum value of the attribute and notifies listeners of the change.
   *
   * @param max the new maximum value
   */
  public void setMax(T max) {
    T oldMax = this.max;
    this.max = max;
    getMaxModifiers().clear();
    maxSupport.firePropertyChange("max", oldMax, max);
  }

  /**
   * Adds a PropertyChangeListener for the minimum value.
   *
   * @param listener the listener to be added
   */
  public void addMinListener(PropertyChangeListener listener) {
    minSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for the minimum value.
   *
   * @param listener the listener to be removed
   */
  public void removeMinListener(PropertyChangeListener listener) {
    minSupport.removePropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener for the maximum value.
   *
   * @param listener the listener to be added
   */
  public void addMaxListener(PropertyChangeListener listener) {
    maxSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for the maximum value.
   *
   * @param listener the listener to be removed
   */
  public void removeMaxListener(PropertyChangeListener listener) {
    maxSupport.removePropertyChangeListener(listener);
  }

  /**
   * Gets the list of minimum value modifiers.
   *
   * @return the list of minimum value modifiers
   */
  public List<AttributeModifier<T>> getMinModifiers() {
    return minModifiers;
  }

  /**
   * Gets the list of maximum value modifiers.
   *
   * @return the list of maximum value modifiers
   */
  public List<AttributeModifier<T>> getMaxModifiers() {
    return maxModifiers;
  }

  /**
   * Adds a modifier to the list of minimum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to add
   */
  public void addMinModifier(AttributeModifier<T> modifier) {
    if (getMinModifiers().contains(modifier)) {
      return;
    }
    T oldMin = getModifiedMin();
    minModifiers.add(modifier);
    Collections.sort(getMinModifiers());
    modifier.addListener(minModifierListener);
    T newMin = getModifiedMin();
    minSupport.firePropertyChange(MIN_PROPERTY, oldMin, newMin);
  }

  /**
   * Removes a modifier from the list of minimum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to remove
   */
  public void removeMinModifier(AttributeModifier<T> modifier) {
    if (!getMinModifiers().contains(modifier)) {
      return;
    }
    T oldMin = getModifiedMin();
    minModifiers.remove(modifier);
    Collections.sort(getMinModifiers());
    modifier.removeListener(minModifierListener);
    T newMin = getModifiedMin();
    minSupport.firePropertyChange(MIN_PROPERTY, oldMin, newMin);
  }

  /**
   * Adds a modifier to the list of maximum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to add
   */
  public void addMaxModifier(AttributeModifier<T> modifier) {
    if (getMaxModifiers().contains(modifier)) {
      return;
    }
    T oldMax = getModifiedMax();
    maxModifiers.add(modifier);
    Collections.sort(getMaxModifiers());
    modifier.addListener(maxModifierListener);
    T newMax = getModifiedMax();
    maxSupport.firePropertyChange(MAX_PROPERTY, oldMax, newMax);
  }

  /**
   * Removes a modifier from the list of maximum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to remove
   */
  public void removeMaxModifier(AttributeModifier<T> modifier) {
    if (!getMaxModifiers().contains(modifier)) {
      return;
    }
    T oldMax = getModifiedMax();
    maxModifiers.remove(modifier);
    Collections.sort(getMaxModifiers());
    modifier.removeListener(maxModifierListener);
    T newMax = getModifiedMax();
    maxSupport.firePropertyChange(MAX_PROPERTY, oldMax, newMax);
  }

  /**
   * Gets the ratio of the current value to the maximum value as a float. This is calculated as the current value divided by the maximum value.
   *
   * @return The ratio of the current value to the maximum value.
   */
  public float getRatio() {
    return getModifiedValue().floatValue() / getModifiedMax().floatValue();
  }


  private T enforceRangeForValue(T value) {
    if (value.compareTo(getModifiedMin()) < 0) {
      return getModifiedMin();
    } else if (value.compareTo(getModifiedMax()) > 0) {
      return getModifiedMax();
    }
    return value;
  }


  @Override public void setValue(T newValue) {
    super.setValue(enforceRangeForValue(newValue));
  }
}
