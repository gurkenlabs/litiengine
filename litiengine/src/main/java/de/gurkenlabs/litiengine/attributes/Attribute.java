package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an attribute with a base value and a list of modifiers. The attribute value can be modified by adding or removing modifiers.
 *
 * @param <T> the type of the attribute value, which must be a Number
 */
public class Attribute<T extends Number> implements IAttribute<T>, Serializable {
  /**
   * The support object used to manage and notify property change listeners. It allows other components to listen for changes to the properties of
   * this object.
   */
  protected final PropertyChangeSupport support;
  /**
   * The base value of the attribute. This value represents the unmodified state of the attribute before any modifiers are applied.
   */
  protected T value;
  private static final String VALUE_PROPERTY = "value";
  private final transient PropertyChangeListener modifierListener;
  private final List<AttributeModifier<T>> modifiers = new ArrayList<>();


  /**
   * Default no-argument constructor.
   */
  public Attribute() {
    this(null);
  }

  /**
   * Constructs a new Attribute with the specified initial value.
   *
   * @param initialValue the initial value of the attribute
   */
  public Attribute(T initialValue) {
    this.value = initialValue;
    this.support = new PropertyChangeSupport(this);
    this.modifierListener = evt -> support.firePropertyChange(VALUE_PROPERTY, evt.getOldValue(), evt.getNewValue());
  }

  /**
   * Gets the base value of the attribute.
   *
   * @return the base value of the attribute
   */
  public T getValue() {
    return this.value;
  }

  /**
   * Gets the current value of the attribute, computed by applying all active modifications to the base value.
   *
   * @return the current value
   */
  @Override
  public T getModifiedValue() {
    T modifiedValue = value;
    for (AttributeModifier<T> modifier : modifiers) {
      if (modifier.isActive()) {
        modifiedValue = modifier.modify(modifiedValue);
      }
    }
    return modifiedValue;
  }

  /**
   * Sets the base value of the attribute and fires a property change event.
   *
   * @param newValue the new base value of the attribute
   */
  @Override
  public void setValue(T newValue) {
    T oldValue = getModifiedValue();
    this.value = newValue;
    T newModifiedValue = getModifiedValue();
    getModifiers().clear();
    support.firePropertyChange("baseValue", oldValue, newModifiedValue);
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
  public void addModifier(AttributeModifier<T> modifier) {
    if (modifiers.contains(modifier)) {
      return;
    }
    T oldValue = getModifiedValue();
    modifiers.add(modifier);
    Collections.sort(getModifiers());
    modifier.addListener(modifierListener);
    T newValue = getModifiedValue();
    support.firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
  }

  /**
   * Removes a modifier from the attribute and fires a property change event. If the modifier is not present, no action is taken.
   *
   * @param modifier the property modifier to be removed
   */
  @Override
  public void removeModifier(AttributeModifier<T> modifier) {
    if (!modifiers.contains(modifier)) {
      return;
    }
    T oldValue = getModifiedValue();
    modifiers.remove(modifier);
    Collections.sort(getModifiers());
    modifier.removeListener(modifierListener);
    T newValue = getModifiedValue();
    support.firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
  }

  /**
   * Gets the list of all property modifiers applied to the attribute.
   *
   * @return the list of property modifiers
   */
  public List<AttributeModifier<T>> getModifiers() {
    return modifiers;
  }

  /**
   * Modifies the current value of the attribute using the specified property modifier.
   *
   * @param modifier the property modifier to apply
   */
  @Override
  public void modify(AttributeModifier<T> modifier) {
    this.setValue(modifier.modify(this.getModifiedValue()));
  }

  /**
   * Modifies the current value of the attribute using a new property modifier created with the specified modification and value.
   *
   * @param modification the type of modification to apply
   * @param value        the value to use for the modification
   */
  @Override
  public void modify(Modification modification, double value) {
    this.modify(new AttributeModifier<>(modification, value));
  }

  /**
   * Returns a string representation of the current value of the attribute.
   *
   * @return the string representation of the current value
   */
  @Override
  public String toString() {
    return getModifiedValue().toString();
  }
}

