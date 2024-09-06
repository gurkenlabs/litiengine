package de.gurkenlabs.litiengine.graphics.emitters.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a parameter for a particle with a minimum and maximum value.
 */
@XmlRootElement(name = "param")
public class ParticleParameter implements Serializable {
  @Serial
  private static final long serialVersionUID = 4893417265998349179L;

  @XmlAttribute
  private double maxValue;

  @XmlAttribute
  private double minValue;

  /**
   * Default constructor for ParticleParameter.
   */
  public ParticleParameter() {
  }

  /**
   * Constructs a ParticleParameter with the same minimum and maximum value.
   *
   * @param value The value to be set for both minimum and maximum.
   */
  public ParticleParameter(final float value) {
    this.setMinValue(value);
    this.setMaxValue(value);
  }

  /**
   * Constructs a ParticleParameter with specified minimum and maximum values.
   *
   * @param minValue The minimum value.
   * @param maxValue The maximum value.
   */
  public ParticleParameter(final float minValue, final float maxValue) {
    this.setMinValue(minValue);
    this.setMaxValue(maxValue);
  }

  /**
   * Gets a random value between minValue and maxValue if minValue is less than maxValue, otherwise returns minValue.
   *
   * @return A random value or minValue.
   */
  public double get() {
    if (minValue < maxValue) {
      return this.getRandomNumber();
    } else {
      return this.getMinValue();
    }
  }

  /**
   * Gets the maximum value.
   *
   * @return The maximum value.
   */
  @XmlTransient
  public double getMaxValue() {
    return this.maxValue;
  }

  /**
   * Gets the minimum value.
   *
   * @return The minimum value.
   */
  @XmlTransient
  public double getMinValue() {
    return this.minValue;
  }

  /**
   * Gets a random number between minValue and maxValue.
   *
   * @return A random number.
   */
  @XmlTransient
  public float getRandomNumber() {
    return (float) ThreadLocalRandom.current().nextDouble(this.getMinValue(), this.getMaxValue());
  }

  /**
   * Sets the maximum value.
   *
   * @param maxValue The maximum value to set.
   */
  public void setMaxValue(final double maxValue) {
    this.maxValue = maxValue;
  }

  /**
   * Sets the minimum value.
   *
   * @param minValue The minimum value to set.
   */
  public void setMinValue(final double minValue) {
    this.minValue = minValue;
  }
}
