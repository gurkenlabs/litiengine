package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "param")
public class ParticleParameter {
  public static int randomInRange(final int min, final int max) {
    return min + new Random().nextInt(max - min + 1);
  }

  @XmlAttribute
  private float maxValue;

  @XmlAttribute
  private float minValue;

  @XmlAttribute
  private boolean randomValue;

  @XmlAttribute
  private float value;

  public ParticleParameter() {
  }

  public ParticleParameter(final float value, final boolean randomValue, final float minValue, final float maxValue) {
    if (minValue > maxValue) {
      throw new IllegalArgumentException("minValue must be < than maxValue");
    }

    this.value = value;
    this.randomValue = randomValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /**
   * Gets either the acutal value or a random value, depending on the random
   * number flag being set.
   *
   * @return
   */
  public float get() {
    if (this.isRandomValue()) {
      return this.getRandomNumber();
    } else {
      return this.getValue();
    }
  }

  @XmlTransient
  public float getMaxValue() {
    return this.maxValue;
  }

  @XmlTransient
  public float getMinValue() {
    return this.minValue;
  }

  @XmlTransient
  public float getRandomNumber() {
    return (float) (this.getMinValue() + Math.random() * (this.getMaxValue() - this.getMinValue() + 1));
  }

  @XmlTransient
  public float getValue() {
    return this.value;
  }

  @XmlTransient
  public boolean isRandomValue() {
    return this.randomValue;
  }

  public void setMaxValue(final float maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(final float minValue) {
    this.minValue = minValue;
  }

  public void setRandomValue(final boolean randomValue) {
    this.randomValue = randomValue;
  }

  public void setValue(final float value) {
    this.value = value;
  }
}