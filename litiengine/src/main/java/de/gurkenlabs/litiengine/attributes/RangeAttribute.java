package de.gurkenlabs.litiengine.attributes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RangeAttribute<T extends Number> extends Attribute<T> {
  private final List<AttributeModifier<T>> minModifiers;
  private final List<AttributeModifier<T>> maxModifiers;
  private T minBaseValue;
  private T maxBaseValue;
  private T minValue;
  private T maxValue;

  /**
   * Initializes a new instance of the {@code RangeAttribute} class.
   *
   * @param maxValue
   *          The max value of this attribute.
   * @param minValue
   *          The min value of this attribute
   * @param baseValue
   *          The base (initial) value of this attribute
   */
  public RangeAttribute(final T maxValue, final T minValue, final T baseValue) {
    super(baseValue);

    this.minModifiers = new CopyOnWriteArrayList<>();
    this.maxModifiers = new CopyOnWriteArrayList<>();
    this.maxBaseValue = maxValue;
    this.minBaseValue = minValue;
    this.evaluateValue();
  }

  public void addMinModifier(final AttributeModifier<T> modifier) {
    if (this.getMinModifiers().contains(modifier)) {
      return;
    }

    this.getMinModifiers().add(modifier);
    Collections.sort(this.getMinModifiers());
    this.evaluateValue();
  }

  public void addMaxModifier(final AttributeModifier<T> modifier) {
    if (this.getMaxModifiers().contains(modifier)) {
      return;
    }

    this.getMaxModifiers().add(modifier);
    Collections.sort(this.getMaxModifiers());
    this.evaluateValue();
  }

  @Override
  public T get() {
    return this.valueInRange(super.get());
  }

  public T getMin() {
    return this.minValue;
  }

  public T getMax() {
    return this.maxValue;
  }

  public float getRelativeCurrentValue() {
    return this.get().floatValue() / this.getMax().floatValue();
  }

  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBase())));
    this.evaluateValue();
  }

  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxBaseValue = modifier.modify(this.maxBaseValue);
    this.evaluateValue();
  }

  public void setToMin() {
    this.setBaseValue(this.getMin());
  }

  public void setToMax() {
    this.setBaseValue(this.getMax());
  }

  public void setMaxBaseValue(final T maxValue) {
    this.maxBaseValue = maxValue;
    this.evaluateValue();
  }

  public void setMinBaseValue(final T minValue) {
    this.minBaseValue = minValue;
    this.evaluateValue();
  }

  protected List<AttributeModifier<T>> getMinModifiers() {
    return this.minModifiers;
  }

  protected List<AttributeModifier<T>> getMaxModifiers() {
    return this.maxModifiers;
  }

  protected T applyMinModifiers(final T maxValue) {
    T currentValue = maxValue;
    for (final AttributeModifier<T> modifier : this.getMinModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  protected T applyMaxModifiers(final T maxValue) {
    T currentValue = maxValue;
    for (final AttributeModifier<T> modifier : this.getMaxModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  @Override
  protected void evaluateValue() {
    super.evaluateValue();
    this.minValue = this.applyMinModifiers(this.minBaseValue);
    this.maxValue = this.applyMaxModifiers(this.maxBaseValue);
  }

  private T valueInRange(final T value) {
    if (value.doubleValue() < this.getMin().doubleValue()) {
      return this.getMin();
    } else if (value.doubleValue() > this.getMax().doubleValue()) {
      return this.getMax();
    }

    return value;
  }
}
