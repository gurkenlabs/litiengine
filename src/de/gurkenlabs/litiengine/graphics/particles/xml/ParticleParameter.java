package de.gurkenlabs.litiengine.graphics.particles.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "param")
public class ParticleParameter {
  @XmlAttribute
  private float value;

  @XmlAttribute
  private boolean randomValue;

  @XmlAttribute
  private float minValue;

  @XmlAttribute
  private float maxValue;

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

  @XmlTransient
  public float getValue() {
    return this.value;
  }

  @XmlTransient
  public boolean isRandomValue() {
    return this.randomValue;
  }

  @XmlTransient
  public float getMinValue() {
    return this.minValue;
  }

  @XmlTransient
  public float getMaxValue() {
    return this.maxValue;
  }

  public void setValue(float value) {
    this.value = value;
  }

  public void setRandomValue(boolean randomValue) {
    this.randomValue = randomValue;
  }

  public void setMinValue(float minValue) {
    this.minValue = minValue;
  }

  public void setMaxValue(float maxValue) {
    this.maxValue = maxValue;
  }

  @XmlTransient
  public float getRandomNumber() {
    return (float) (this.getMinValue() + (Math.random() * (this.getMaxValue() - this.getMinValue() + 1)));
  }

  public static int randomInRange(int min, int max) {
    return min + (int) (Math.random() * (max - min + 1));
  }
}