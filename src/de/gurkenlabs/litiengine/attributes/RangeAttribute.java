/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class RangeAttribute.
 *
 * @param <T>
 *          the generic type
 */
public class RangeAttribute<T extends Number> extends Attribute<T> {

  /** The max base value. */
  private T maxBaseValue;

  /** The max modifiers. */
  private final List<AttributeModifier<T>> maxModifiers;

  /** The min base value. */
  private final T minBaseValue;

  /**
   * Instantiates a new range attribute.
   *
   * @param maxValue
   *          the max value
   * @param minValue
   *          the min value
   * @param baseValue
   *          the base value
   */
  public RangeAttribute(final T maxValue, final T minValue, final T baseValue) {
    super(baseValue);

    this.maxModifiers = new ArrayList<>();
    this.maxBaseValue = maxValue;
    this.minBaseValue = minValue;
  }

  /**
   * Adds the max modifier.
   *
   * @param modifier
   *          the modifier
   */
  public void addMaxModifier(final AttributeModifier<T> modifier) {
    if (this.getMaxModifiers().contains(modifier)) {
      return;
    }

    this.getMaxModifiers().add(modifier);
    Collections.sort(this.getMaxModifiers());
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.attributes.Attribute#getCurrentValue()
   */
  @Override
  public T getCurrentValue() {
    final T current = this.applyModifiers(this.getBaseValue());
    return this.valueInRange(current);
  }

  /**
   * Gets the max modifiers.
   *
   * @return the max modifiers
   */
  protected List<AttributeModifier<T>> getMaxModifiers() {
    return this.maxModifiers;
  }

  /**
   * Gets the max value.
   *
   * @return the max value
   */
  public T getMaxValue() {
    return this.applyModifiers(this.maxBaseValue);
  }

  /**
   * Gets the relative current value.
   *
   * @return the relative current value
   */
  public float getRelativeCurrentValue() {
    return this.getCurrentValue().floatValue() / this.getMaxValue().floatValue();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.attributes.Attribute#modifyBaseValue(de.gurkenlabs.liti.
   * attributes.AttributeModifier)
   */
  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBaseValue())));
  }

  /**
   * Modify max base value.
   *
   * @param modifier
   *          the modifier
   */
  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxBaseValue = modifier.modify(this.maxBaseValue);
  }

  /**
   * Value in range.
   *
   * @param value
   *          the value
   * @return the t
   */
  private T valueInRange(final T value) {
    if (value.doubleValue() < this.minBaseValue.doubleValue()) {
      return this.minBaseValue;
    } else if (value.doubleValue() > this.getMaxValue().doubleValue()) {
      return this.getMaxValue();
    }

    return value;
  }
}
