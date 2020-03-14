package de.gurkenlabs.litiengine.attributes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RangeAttribute<T extends Number> extends Attribute<T> {
  private final List<AttributeModifier<T>> minModifiers;
  private final List<AttributeModifier<T>> maxModifiers;
  private T minBaseValue;

  private T maxBaseValue;

  /**
   * Initializes a new instance of the <code>RangeAttribute</code> class.
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
  }

  public void addMinModifier(final AttributeModifier<T> modifier) {
    if (this.getMinModifiers().contains(modifier)) {
      return;
    }

    this.getMinModifiers().add(modifier);
    Collections.sort(this.getMinModifiers());
  }

  public void addMaxModifier(final AttributeModifier<T> modifier) {
    if (this.getMaxModifiers().contains(modifier)) {
      return;
    }

    this.getMaxModifiers().add(modifier);
    Collections.sort(this.getMaxModifiers());
  }

  @Override
  public T get() {
    final T current = this.applyModifiers(this.getBase());
    return this.valueInRange(current);
  }

  public T getMin() {
    return this.applyMinModifiers(this.minBaseValue);
  }

  public T getMax() {
    return this.applyMaxModifiers(this.maxBaseValue);
  }

  public float getRelativeCurrentValue() {
    return this.get().floatValue() / this.getMax().floatValue();
  }

  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBase())));
  }

  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxBaseValue = modifier.modify(this.maxBaseValue);
  }

  public void setToMin() {
    this.setBaseValue(this.getMin());
  }

  public void setToMax() {
    this.setBaseValue(this.getMax());
  }

  public void setMaxBaseValue(final T maxValue) {
    this.maxBaseValue = maxValue;
  }

  public void setMinBaseValue(final T minValue) {
    this.minBaseValue = minValue;
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

  private T valueInRange(final T value) {
    if (value.doubleValue() < this.minBaseValue.doubleValue()) {
      return this.minBaseValue;
    } else if (value.doubleValue() > this.getMax().doubleValue()) {
      return this.getMax();
    }

    return value;
  }
}
