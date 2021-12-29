package de.gurkenlabs.litiengine.graphics.emitters.xml;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "param")
public class ParticleParameter implements Serializable {
  private static final long serialVersionUID = 4893417265998349179L;

  @XmlAttribute
  private double maxValue;

  @XmlAttribute
  private double minValue;

  public ParticleParameter() {}

  public ParticleParameter(final float value) {
    this.setMinValue(value);
    this.setMaxValue(value);
  }

  public ParticleParameter(final float minValue, final float maxValue) {
    this.setMinValue(minValue);
    this.setMaxValue(maxValue);
  }

  /**
   * Gets either the actual value or a random value, depending on the random number flag being set.
   *
   * @return The value of this parameter.
   */
  public double get() {
    if (minValue < maxValue) {
      return this.getRandomNumber();
    } else {
      return this.getMinValue();
    }
  }

  @XmlTransient
  public double getMaxValue() {
    return this.maxValue;
  }

  @XmlTransient
  public double getMinValue() {
    return this.minValue;
  }

  @XmlTransient
  public float getRandomNumber() {
    return (float) ThreadLocalRandom.current().nextDouble(this.getMinValue(), this.getMaxValue());
  }

  public void setMaxValue(final double maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(final double minValue) {
    this.minValue = minValue;
  }
}
