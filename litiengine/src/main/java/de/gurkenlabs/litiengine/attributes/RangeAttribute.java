package de.gurkenlabs.litiengine.attributes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an attribute with a range of values, including minimum and maximum values. This class extends the {@code Attribute} class and provides
 * additional functionality for handling minimum and maximum value modifiers.
 *
 * @param <T>
 *   The type of the attribute value, which must extend {@code Number}.
 */
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
   *   The max value of this attribute.
   * @param minValue
   *   The min value of this attribute
   * @param baseValue
   *   The base (initial) value of this attribute
   */
  public RangeAttribute(final T maxValue, final T minValue, final T baseValue) {
    super(baseValue);

    this.minModifiers = new CopyOnWriteArrayList<>();
    this.maxModifiers = new CopyOnWriteArrayList<>();
    this.maxBaseValue = maxValue;
    this.minBaseValue = minValue;
    this.evaluateValue();
  }

  /**
   * Adds a modifier to the minimum value of the attribute. If the modifier is already present, it does nothing. After adding the modifier, it sorts
   * the modifiers and re-evaluates the attribute value.
   *
   * @param modifier
   *   The modifier to be added.
   */
  public void addMinModifier(final AttributeModifier<T> modifier) {
    if (this.getMinModifiers().contains(modifier)) {
      return;
    }

    this.getMinModifiers().add(modifier);
    Collections.sort(this.getMinModifiers());
    this.evaluateValue();
  }

  /**
   * Adds a modifier to the maximum value of the attribute. If the modifier is already present, it does nothing. After adding the modifier, it sorts
   * the modifiers and re-evaluates the attribute value.
   *
   * @param modifier
   *   The modifier to be added.
   */
  public void addMaxModifier(final AttributeModifier<T> modifier) {
    if (this.getMaxModifiers().contains(modifier)) {
      return;
    }

    this.getMaxModifiers().add(modifier);
    Collections.sort(this.getMaxModifiers());
    this.evaluateValue();
  }

  /**
   * Gets the current value of the attribute, ensuring it is within the defined range.
   *
   * @return The current value of the attribute.
   */
  @Override
  public T get() {
    return this.valueInRange(super.get());
  }

  /**
   * Gets the minimum value of the attribute after applying all modifiers.
   *
   * @return The minimum value of the attribute.
   */
  public T getMin() {
    return this.minValue;
  }

  /**
   * Gets the maximum value of the attribute after applying all modifiers.
   *
   * @return The maximum value of the attribute.
   */
  public T getMax() {
    return this.maxValue;
  }

  /**
   * Gets the relative current value of the attribute as a float. This is calculated as the current value divided by the maximum value.
   *
   * @return The relative current value of the attribute.
   */
  public float getRelativeCurrentValue() {
    return this.get().floatValue() / this.getMax().floatValue();
  }

  /**
   * Modifies the base value of the attribute using the provided modifier. Ensures the new base value is within the defined range and re-evaluates the
   * attribute value.
   *
   * @param modifier
   *   The modifier to be applied to the base value.
   */
  @Override
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.setBaseValue(this.valueInRange(modifier.modify(this.getBase())));
    this.evaluateValue();
  }

  /**
   * Modifies the maximum base value of the attribute using the provided modifier. Re-evaluates the attribute value after modification.
   *
   * @param modifier
   *   The modifier to be applied to the maximum base value.
   */
  public void modifyMaxBaseValue(final AttributeModifier<T> modifier) {
    this.maxBaseValue = modifier.modify(this.maxBaseValue);
    this.evaluateValue();
  }

  /**
   * Sets the base value of the attribute to the minimum value.
   */
  public void setToMin() {
    this.setBaseValue(this.getMin());
  }

  /**
   * Sets the base value of the attribute to the maximum value.
   */
  public void setToMax() {
    this.setBaseValue(this.getMax());
  }

  /**
   * Sets the maximum base value of the attribute. Re-evaluates the attribute value after setting the new maximum base value.
   *
   * @param maxValue
   *   The new maximum base value to be set.
   */
  public void setMaxBaseValue(final T maxValue) {
    this.maxBaseValue = maxValue;
    this.evaluateValue();
  }

  /**
   * Sets the minimum base value of the attribute. Re-evaluates the attribute value after setting the new minimum base value.
   *
   * @param minValue
   *   The new minimum base value to be set.
   */
  public void setMinBaseValue(final T minValue) {
    this.minBaseValue = minValue;
    this.evaluateValue();
  }

  /**
   * Gets the list of modifiers applied to the minimum value of the attribute.
   *
   * @return The list of minimum value modifiers.
   */
  protected List<AttributeModifier<T>> getMinModifiers() {
    return this.minModifiers;
  }

  /**
   * Gets the list of modifiers applied to the maximum value of the attribute.
   *
   * @return The list of maximum value modifiers.
   */
  protected List<AttributeModifier<T>> getMaxModifiers() {
    return this.maxModifiers;
  }

  /**
   * Applies all minimum value modifiers to the given value.
   *
   * @param maxValue
   *   The value to which the minimum value modifiers will be applied.
   * @return The modified value after applying all minimum value modifiers.
   */
  protected T applyMinModifiers(final T maxValue) {
    T currentValue = maxValue;
    for (final AttributeModifier<T> modifier : this.getMinModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  /**
   * Applies all maximum value modifiers to the given value.
   *
   * @param maxValue
   *   The value to which the maximum value modifiers will be applied.
   * @return The modified value after applying all maximum value modifiers.
   */
  protected T applyMaxModifiers(final T maxValue) {
    T currentValue = maxValue;
    for (final AttributeModifier<T> modifier : this.getMaxModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  /**
   * Evaluates the current value of the attribute by applying all registered modifiers to the base, minimum, and maximum values. Ensures the attribute
   * value is within the defined range.
   */
  @Override
  protected void evaluateValue() {
    super.evaluateValue();
    this.minValue = this.applyMinModifiers(this.minBaseValue);
    this.maxValue = this.applyMaxModifiers(this.maxBaseValue);
  }

  /**
   * Ensures the given value is within the defined range of minimum and maximum values.
   *
   * @param value
   *   The value to be checked.
   * @return The value if it is within the range, otherwise the nearest boundary value.
   */
  private T valueInRange(final T value) {
    if (value.doubleValue() < this.getMin().doubleValue()) {
      return this.getMin();
    } else if (value.doubleValue() > this.getMax().doubleValue()) {
      return this.getMax();
    }

    return value;
  }
}
