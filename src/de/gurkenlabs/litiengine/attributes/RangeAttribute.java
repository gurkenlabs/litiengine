package de.gurkenlabs.litiengine.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangeAttribute<T extends Number> extends Attribute<T> {
  private final List<AttributeModifier<T>> maxModifiers;
  private final T minBaseValue;
  
  private T maxBaseValue;

  public RangeAttribute(final T maxValue, final T minValue, final T baseValue) {
    super(baseValue);

    this.maxModifiers = new ArrayList<>();
    this.maxBaseValue = maxValue;
    this.minBaseValue = minValue;
  }

  public void addMaxModifier(final AttributeModifier<T> modifier) {
    if (this.getMaxModifiers().contains(modifier)) {
      return;
    }

    this.getMaxModifiers().add(modifier);
    Collections.sort(this.getMaxModifiers());
  }

  @Override
  public T getCurrentValue() {
    final T current = this.applyModifiers(this.getBaseValue());
    return this.valueInRange(current);
  }

  public T getMaxValue() {
    return this.applyModifiers(this.maxBaseValue);
  }

  public float getRelativeCurrentValue() {
    return this.getCurrentValue().floatValue() / this.getMaxValue().floatValue();
  }

  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBaseValue())));
  }

  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxBaseValue = modifier.modify(this.maxBaseValue);
  }

  protected List<AttributeModifier<T>> getMaxModifiers() {
    return this.maxModifiers;
  }

  private T valueInRange(final T value) {
    if (value.doubleValue() < this.minBaseValue.doubleValue()) {
      return this.minBaseValue;
    } else if (value.doubleValue() > this.getMaxValue().doubleValue()) {
      return this.getMaxValue();
    }

    return value;
  }
}
