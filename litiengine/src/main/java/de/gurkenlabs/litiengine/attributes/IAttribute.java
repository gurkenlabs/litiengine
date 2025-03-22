package de.gurkenlabs.litiengine.attributes;

import java.beans.PropertyChangeListener;

/**
 * Interface representing an observable property that holds a numeric value. It provides methods to get and set the value, add and remove listeners,
 * and add and remove modifiers.
 *
 * @param <T> the type of the numeric value
 */
public interface IAttribute<T extends Number> {
  /**
   * Gets the base value of the attribute.
   *
   * @return the base value of the attribute
   */
  T getValue();

  /**
   * Gets the current value of the attribute, including any modifications.
   *
   * @return the current value
   */
  T getModifiedValue();

  /**
   * Sets a new value for this property.
   *
   * @param newValue the new value to set
   */
  void setValue(T newValue);

  /**
   * Adds a PropertyChangeListener to the listener list.
   *
   * @param listener the PropertyChangeListener to be added
   */
  void addListener(PropertyChangeListener listener);

  /**
   * Removes a PropertyChangeListener from the listener list.
   *
   * @param listener the PropertyChangeListener to be removed
   */
  void removeListener(PropertyChangeListener listener);

  /**
   * Adds a PropertyModifier to this property.
   *
   * @param modifier the PropertyModifier to be added
   */
  void addModifier(AttributeModifier<T> modifier);

  /**
   * Removes a PropertyModifier from this property.
   *
   * @param modifier the PropertyModifier to be removed
   */
  void removeModifier(AttributeModifier<T> modifier);

  /**
   * Modifies the value of this property using the specified PropertyModifier.
   *
   * @param modifier the PropertyModifier to apply
   */
  void modify(AttributeModifier<T> modifier);

  /**
   * Modifies the value of this property using the specified modification type and value.
   *
   * @param modification the type of modification to apply
   * @param value        the value to use for the modification
   */
  void modify(Modification modification, double value);
}
