package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an attribute with a defined range, extending the base Attribute class. The attribute value is constrained between a minimum and maximum
 * value.
 *
 * @param <T> the type of the attribute value, which must be a Number and Comparable
 */
public class RangedAttribute<T extends Number & Comparable<T>> extends Attribute<T> {
  private static final String MIN_PROPERTY = "min";
  private static final String MAX_PROPERTY = "max";
  private final PropertyChangeSupport minSupport = new PropertyChangeSupport(this);
  private final PropertyChangeSupport maxSupport = new PropertyChangeSupport(this);
  private final PropertyChangeListener minModifierListener;
  private final PropertyChangeListener maxModifierListener;

  private final List<PropertyModifier<T>> minModifiers = new ArrayList<>();
  private final List<PropertyModifier<T>> maxModifiers = new ArrayList<>();
  private T min;
  private T max;

  /**
   * Constructs a new RangedAttribute with the specified initial value, minimum value, and maximum value. Ensures that the minimum value is not
   * greater than the maximum value.
   *
   * @param initialValue the initial value of the attribute
   * @param min          the minimum value of the attribute
   * @param max          the maximum value of the attribute
   * @throws IllegalArgumentException if the minimum value is greater than the maximum value
   */
  public RangedAttribute(T initialValue, T min, T max) {
    super(initialValue);
    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("min cannot be greater than max");
    }
    this.min = min;
    this.max = max;

    this.minModifierListener = _ -> minSupport.firePropertyChange(MIN_PROPERTY, getMin(), computeModifiedMin());
    this.maxModifierListener = _ -> maxSupport.firePropertyChange(MAX_PROPERTY, getMax(), computeModifiedMax());
  }

  /**
   * Gets the current value of the attribute, ensuring it is within the defined range.
   *
   * @return the current value of the attribute
   */
  @Override
  public T getCurrent() {
    enforceRange();
    return super.getCurrent();
  }

  /**
   * Gets the minimum value of the attribute.
   *
   * @return the minimum value
   */
  public T getMin() {
    return min;
  }

  /**
   * Sets the minimum value of the attribute and notifies listeners of the change.
   *
   * @param min the new minimum value
   */
  public void setMin(T min) {
    T oldMin = this.min;
    this.min = min;
    minSupport.firePropertyChange("min", oldMin, min);
  }

  /**
   * Gets the maximum value of the attribute.
   *
   * @return the maximum value
   */
  public T getMax() {
    return max;
  }

  /**
   * Sets the maximum value of the attribute and notifies listeners of the change.
   *
   * @param max the new maximum value
   */
  public void setMax(T max) {
    T oldMax = this.max;
    this.max = max;
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
  public List<PropertyModifier<T>> getMinModifiers() {
    return minModifiers;
  }

  /**
   * Gets the list of maximum value modifiers.
   *
   * @return the list of maximum value modifiers
   */
  public List<PropertyModifier<T>> getMaxModifiers() {
    return maxModifiers;
  }

  /**
   * Adds a modifier to the list of minimum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to add
   */
  public void addMinModifier(PropertyModifier<T> modifier) {
    if (getMinModifiers().contains(modifier)) {
      return;
    }
    T oldMin = computeModifiedMin();
    minModifiers.add(modifier);
    Collections.sort(getMinModifiers());
    modifier.addListener(minModifierListener);
    T newMin = computeModifiedMin();
    minSupport.firePropertyChange(MIN_PROPERTY, oldMin, newMin);
  }

  /**
   * Removes a modifier from the list of minimum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to remove
   */
  public void removeMinModifier(PropertyModifier<T> modifier) {
    if (!getMinModifiers().contains(modifier)) {
      return;
    }
    T oldMin = computeModifiedMin();
    minModifiers.remove(modifier);
    Collections.sort(getMinModifiers());
    modifier.removeListener(minModifierListener);
    T newMin = computeModifiedMin();
    minSupport.firePropertyChange(MIN_PROPERTY, oldMin, newMin);
  }

  /**
   * Adds a modifier to the list of maximum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to add
   */
  public void addMaxModifier(PropertyModifier<T> modifier) {
    if (getMaxModifiers().contains(modifier)) {
      return;
    }
    T oldMax = computeModifiedMax();
    maxModifiers.add(modifier);
    Collections.sort(getMaxModifiers());
    modifier.addListener(maxModifierListener);
    T newMax = computeModifiedMax();
    maxSupport.firePropertyChange(MAX_PROPERTY, oldMax, newMax);
  }

  /**
   * Removes a modifier from the list of maximum value modifiers and notifies listeners of the change.
   *
   * @param modifier the modifier to remove
   */
  public void removeMaxModifier(PropertyModifier<T> modifier) {
    if (!getMaxModifiers().contains(modifier)) {
      return;
    }
    T oldMax = computeModifiedMax();
    maxModifiers.remove(modifier);
    Collections.sort(getMaxModifiers());
    modifier.removeListener(maxModifierListener);
    T newMax = computeModifiedMax();
    maxSupport.firePropertyChange(MAX_PROPERTY, oldMax, newMax);
  }

  /**
   * Gets the ratio of the current value to the maximum value as a float. This is calculated as the current value divided by the maximum value.
   *
   * @return The ratio of the current value to the maximum value.
   */
  public float getRatio() {
    return this.getCurrent().floatValue() / this.getMax().floatValue();
  }

  /**
   * Computes the modified minimum value by applying all active modifiers to the base minimum value.
   *
   * @return the computed modified minimum value
   */
  private T computeModifiedMin() {
    T modifiedMin = min;
    for (PropertyModifier<T> modifier : getMinModifiers()) {
      if (modifier.isActive()) {
        modifiedMin = modifier.modify(modifiedMin);
      }
    }
    return modifiedMin;
  }

  /**
   * Computes the modified maximum value by applying all active modifiers to the base maximum value.
   *
   * @return the computed modified maximum value
   */
  private T computeModifiedMax() {
    T modifiedMax = max;
    for (PropertyModifier<T> modifier : getMaxModifiers()) {
      if (modifier.isActive()) {
        modifiedMax = modifier.modify(modifiedMax);
      }
    }
    return modifiedMax;
  }

  /**
   * Ensures that the base value of the attribute is within the defined range. If the base value is less than the minimum value, it is set to the
   * minimum value. If the base value is greater than the maximum value, it is set to the maximum value.
   */
  private void enforceRange() {
    if (baseValue.compareTo(getMin()) < 0) {
      this.baseValue = getMin();
    } else if (baseValue.compareTo(getMax()) > 0) {
      this.baseValue = getMax();
    }
  }
}
