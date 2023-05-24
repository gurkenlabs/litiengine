package de.gurkenlabs.litiengine.attributes;

import de.gurkenlabs.litiengine.environment.tilemap.xml.NumberAdapter;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RangeAttribute<T extends Number> extends Attribute<T> implements Serializable {

  @XmlElementWrapper
  private final List<AttributeModifier<T>> minModifiers;
  @XmlElementWrapper
  private final List<AttributeModifier<T>> maxModifiers;
  @XmlAttribute
  @XmlJavaTypeAdapter(NumberAdapter.class)
  private T minValue;
  @XmlAttribute
  @XmlJavaTypeAdapter(NumberAdapter.class)
  private T maxValue;

  /**
   * Initializes a new instance of the {@code RangeAttribute} class.
   *
   * @param maxValue  The max value of this attribute.
   * @param minValue  The min value of this attribute
   * @param baseValue The base (initial) value of this attribute
   */
  public RangeAttribute(final T maxValue, final T minValue, final T baseValue) {
    super(baseValue);
    this.minModifiers = new CopyOnWriteArrayList<>();
    this.maxModifiers = new CopyOnWriteArrayList<>();
    this.maxValue = maxValue;
    this.minValue = minValue;
  }

  public RangeAttribute() {
    this.minModifiers = new CopyOnWriteArrayList<>();
    this.maxModifiers = new CopyOnWriteArrayList<>();
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
    return this.applyMinModifiers(this.minValue);
  }

  public T getMax() {
    return this.applyMaxModifiers(this.maxValue);
  }

  public float getRelativeCurrentValue() {
    return this.get().floatValue() / this.getMax().floatValue();
  }

  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBase())));
  }

  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxValue = modifier.modify(this.maxValue);
  }

  public void setToMin() {
    this.setBaseValue(this.getMin());
  }

  public void setToMax() {
    this.setBaseValue(this.getMax());
  }

  public void setMaxValue(final T maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(final T minValue) {
    this.minValue = minValue;
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
    if (value.doubleValue() < this.minValue.doubleValue()) {
      return this.minValue;
    } else if (value.doubleValue() > this.getMax().doubleValue()) {
      return this.getMax();
    }

    return value;
  }
}
