package de.gurkenlabs.litiengine.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The class Attribute represents a numerical representation of a character property that can be adjusted using {@link AttributeModifier}s.
 *
 * @param <T>
 *          The type of the actual attribute.
 */
public class Attribute<T extends Number> {
  private final List<AttributeModifier<T>> modifiers;
  
  private T baseValue;

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

  public void addModifier(final AttributeModifier<T> modifier) {
    if (this.getModifiers().contains(modifier)) {
      return;
    }

    this.getModifiers().add(modifier);
    Collections.sort(this.getModifiers());
  }

  public T getCurrentValue() {
    return this.applyModifiers(this.getBaseValue());
  }

  public List<AttributeModifier<T>> getModifiers() {
    return this.modifiers;
  }

  public boolean isModifierApplied(final AttributeModifier<T> modifier) {
    return this.getModifiers().contains(modifier);
  }

  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.baseValue = modifier.modify(this.getBaseValue());
  }

  public void removeModifier(final AttributeModifier<T> modifier) {
    this.getModifiers().remove(modifier);
    Collections.sort(this.getModifiers());
  }
  
  public void setBaseValue(final T baseValue) {
    this.baseValue = baseValue;
  }

  protected T applyModifiers(final T baseValue) {
    if (this.getModifiers().isEmpty()) {
      return baseValue;
    }

    T currentValue = baseValue;
    for (final AttributeModifier<T> modifier : this.getModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  protected T getBaseValue() {
    return this.baseValue;
  }
}
