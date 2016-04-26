/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class Attribute.
 *
 * @param <T>
 *          the generic type
 */
public class Attribute<T extends Number> {

  /** The base value. */
  private T baseValue;

  /** The modifiers. */
  private final List<AttributeModifier<T>> modifiers;

  /**
   * Instantiates a new attribute.
   *
   * @param initialValue
   *          the initial value
   */
  public Attribute(final T initialValue) {
    this.modifiers = new ArrayList<>();
    this.baseValue = initialValue;
  }

  /**
   * Adds the modifier.
   *
   * @param modifier
   *          the modifier
   */
  public void addModifier(final AttributeModifier<T> modifier) {
    if (this.getModifiers().contains(modifier)) {
      return;
    }

    this.getModifiers().add(modifier);
    Collections.sort(this.getModifiers());
  }

  /**
   * Gets the current value.
   *
   * @return the current value
   */
  public T getCurrentValue() {
    return this.applyModifiers(this.getBaseValue());
  }

  /**
   * Modify base value.
   *
   * @param modifier
   *          the modifier
   */
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.baseValue = modifier.modify(this.getBaseValue());
  }

  /**
   * Removes the modifier.
   *
   * @param modifier
   *          the modifier
   */
  public void removeModifier(final AttributeModifier<T> modifier) {
    if (!this.getModifiers().contains(modifier)) {
      return;
    }

    this.getModifiers().remove(modifier);
    Collections.sort(this.getModifiers());
  }

  /**
   * Apply modifiers.
   *
   * @param baseValue
   *          the base value
   * @return the t
   */
  protected T applyModifiers(final T baseValue) {
    if(this.getModifiers().size() == 0){
      return baseValue;
    }
    
    T currentValue = baseValue;
    for (final AttributeModifier<T> modifier : this.getModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  /**
   * Gets the base value.
   *
   * @return the base value
   */
  protected T getBaseValue() {
    return this.baseValue;
  }

  /**
   * Gets the modifiers.
   *
   * @return the modifiers
   */
  public List<AttributeModifier<T>> getModifiers() {
    return this.modifiers;
  }

  /**
   * Sets the base value.
   *
   * @param baseValue
   *          the new base value
   */
  protected void setBaseValue(final T baseValue) {
    this.baseValue = baseValue;
  }
}
