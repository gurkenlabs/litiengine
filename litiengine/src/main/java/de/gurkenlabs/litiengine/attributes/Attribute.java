package de.gurkenlabs.litiengine.attributes;

import de.gurkenlabs.litiengine.environment.tilemap.xml.NumberAdapter;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An attribute is a numerical representation of a property that can be adjusted using
 * {@link AttributeModifier}s.
 * <p>
 * It typically doesn't adjust the raw base value (unless explicitly requested) and instead adjusts
 * the value by registered modifications. This is e.g. useful when a property might only be changed
 * for a certain period of time or we need to know the original value of a property.
 * </p>
 *
 * <p>
 * <i> An example use-case are player stats that might be affected throughout the game (e.g. via
 * certain skills, upgrades or level-ups). </i>
 * </p>
 *
 * @param <T> The type of the attribute value.
 */
public class Attribute<T extends Number> implements Serializable {

  @XmlElementWrapper
  private final List<AttributeModifier<T>> modifiers;
  @XmlAttribute
  @XmlJavaTypeAdapter(NumberAdapter.class)
  private T baseValue;

  /**
   * Initializes a new instance of the {@code Attribute} class.
   *
   * @param initialValue The initial value
   */
  public Attribute(final T initialValue) {
    this();
    setBaseValue(initialValue);
  }

  public Attribute() {
    this.modifiers = new ArrayList<>();
  }

  /**
   * Adds the specified modifier to this attribute.
   *
   * @param modifier The modifier to be added to this instance.
   */
  public void addModifier(final AttributeModifier<T> modifier) {
    if (this.getModifiers().contains(modifier)) {
      return;
    }

    this.getModifiers().add(modifier);
    Collections.sort(this.getModifiers());
  }

  /**
   * Removes the specified modifier from this attribute.
   *
   * @param modifier The modifier to be removed from this instance.
   */
  public void removeModifier(final AttributeModifier<T> modifier) {
    this.getModifiers().remove(modifier);
    Collections.sort(this.getModifiers());
  }

  /**
   * Gets the current value of this attribute, respecting all the registered
   * {@code AttributeModifier}s.
   *
   * @return The current value of this attribute.
   */
  public T get() {
    return this.applyModifiers(this.getBase());
  }

  /**
   * Gets the raw base value of this attribute without applying any modifications.
   *
   * @return The raw base value of this attribute.
   */
  public T getBase() {
    return this.baseValue;
  }

  /**
   * Gets all modifiers added to this instance.
   *
   * @return All modifiers added to this instance.
   */
  public List<AttributeModifier<T>> getModifiers() {
    return this.modifiers;
  }

  /**
   * Determines whether the specified modifier instance is added to this attribute instance.
   *
   * @param modifier The modifier to check for.
   * @return True if the modifier was added to this attribute instance; otherwise false.
   */
  public boolean isModifierApplied(final AttributeModifier<T> modifier) {
    return this.getModifiers().contains(modifier);
  }

  /**
   * Adjusts the base value of this attribute once with the specified modifier.
   *
   * @param modifier The modifier used to adjust this attribute's base value.
   * @see #getBase()
   * @see #setBaseValue(Number)
   */
  public void modifyBaseValue(final AttributeModifier<T> modifier) {
    this.baseValue = modifier.modify(this.getBase());
  }

  /**
   * Adjusts the base value of this attribute once with the specified modifier.
   *
   * @param modification The modification type.
   * @param value        The modification value to be applied.
   */
  public void modifyBaseValue(final Modification modification, double value) {
    this.baseValue = new AttributeModifier<T>(modification, value).modify(this.getBase());
  }

  /**
   * Sets the base value of this attribute.
   *
   * @param baseValue The base value to be set.
   */
  public void setBaseValue(final T baseValue) {
    this.baseValue = baseValue;
  }

  /**
   * Apply modifiers.
   *
   * @param baseValue the base value
   * @return the t
   */
  protected T applyModifiers(final T baseValue) {
    T currentValue = baseValue;
    for (final AttributeModifier<T> modifier : this.getModifiers()) {
      currentValue = modifier.modify(currentValue);
    }

    return currentValue;
  }

  @Override
  public String toString() {
    return this.get() == null ? null : this.get().toString();
  }
}
