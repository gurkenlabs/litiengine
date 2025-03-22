package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an attribute with a base value and a list of modifiers. The attribute value can be modified by adding or removing modifiers.
 *
 * @param <T> the type of the attribute value, which must be a Number
 */
public class Attribute<T extends Number> implements IAttribute<T> {
  private static final String VALUE_PROPERTY = "value";
  protected final PropertyChangeSupport support = new PropertyChangeSupport(this);
  private final PropertyChangeListener modifierListener;
  private final List<PropertyModifier<T>> modifiers = new ArrayList<>();
  protected T baseValue;

  /**
   * Constructs a new Attribute with the specified initial value.
   *
   * @param initialValue the initial value of the attribute
   */
  public Attribute(T initialValue) {
    this.baseValue = initialValue;
    this.modifierListener = _ -> support.firePropertyChange(VALUE_PROPERTY, getBase(), computeModifiedValue());
  }

  /**
   * Gets the base value of the attribute.
   *
   * @return the base value
   */
  @Override
  public T getBase() {
    return baseValue;
  }

  /**
   * Gets the current value of the attribute, computed by applying all active modifications to the base value.
   *
   * @return the current value
   */
  @Override
  public T getCurrent() {
    return computeModifiedValue();
  }

  /**
   * Sets the base value of the attribute and fires a property change event.
   *
   * @param newValue the new base value of the attribute
   */
  @Override
  public void set(T newValue) {
    T oldValue = this.getCurrent();
    this.baseValue = newValue;
    T newComputedValue = computeModifiedValue();
    support.firePropertyChange("baseValue", oldValue, newComputedValue);
  }

  /**
   * Adds a property change listener to the attribute.
   *
   * @param listener the property change listener to be added
   */
  @Override
  public void addListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Removes a property change listener from the attribute.
   *
   * @param listener the property change listener to be removed
   */
  @Override
  public void removeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Adds a modifier to the attribute and fires a property change event. If the modifier is already present, it will not be added again.
   *
   * @param modifier the property modifier to be added
   */
  @Override
  public void addModifier(PropertyModifier<T> modifier) {
    if (modifiers.contains(modifier)) {
      return;
    }
    T oldValue = computeModifiedValue();
    modifiers.add(modifier);
    Collections.sort(getModifiers());
    modifier.addListener(modifierListener);
    T newValue = computeModifiedValue();
    support.firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
  }

  /**
   * Removes a modifier from the attribute and fires a property change event. If the modifier is not present, no action is taken.
   *
   * @param modifier the property modifier to be removed
   */
  @Override
  public void removeModifier(PropertyModifier<T> modifier) {
    if (!modifiers.contains(modifier)) {
      return;
    }
    T oldValue = computeModifiedValue();
    modifiers.remove(modifier);
    Collections.sort(getModifiers());
    modifier.removeListener(modifierListener);
    T newValue = computeModifiedValue();
    support.firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
  }

  /**
   * Gets the list of all property modifiers applied to the attribute.
   *
   * @return the list of property modifiers
   */
  public List<PropertyModifier<T>> getModifiers() {
    return modifiers;
  }

  /**
   * Modifies the current value of the attribute using the specified property modifier.
   *
   * @param modifier the property modifier to apply
   */
  @Override
  public void modify(PropertyModifier<T> modifier) {
    this.set(modifier.modify(this.getCurrent()));
  }

  /**
   * Modifies the current value of the attribute using a new property modifier created with the specified modification and value.
   *
   * @param modification the type of modification to apply
   * @param value        the value to use for the modification
   */
  @Override
  public void modify(Modification modification, double value) {
    this.modify(new PropertyModifier<>(modification, value));
  }

  /**
   * Returns a string representation of the current value of the attribute.
   *
   * @return the string representation of the current value
   */
  @Override
  public String toString() {
    return getCurrent().toString();
  }

  /**
   * Computes the current value of the attribute by applying all active modifiers to the base value.
   *
   * @return the computed current value
   */
  private T computeModifiedValue() {
    T modifiedValue = baseValue;
    for (PropertyModifier<T> modifier : modifiers) {
      if (modifier.isActive()) {
        modifiedValue = modifier.modify(modifiedValue);
      }
    }
    return modifiedValue;
  }
}

