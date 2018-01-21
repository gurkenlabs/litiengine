package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "color")
public class ParticleColor {
  @XmlAttribute
  private int alpha;

  @XmlAttribute
  private int blue;
  @XmlAttribute
  private int green;
  @XmlAttribute
  private int red;

  public ParticleColor() {
  }

  public ParticleColor(final Color color) {
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
    this.alpha = color.getAlpha();
  }

  @XmlTransient
  public int getAlpha() {
    return this.alpha;
  }

  @XmlTransient
  public int getBlue() {
    return this.blue;
  }

  @XmlTransient
  public int getGreen() {
    return this.green;
  }

  @XmlTransient
  public int getRed() {
    return this.red;
  }

  public void setAlpha(final int alpha) {
    this.alpha = alpha;
  }

  public void setBlue(final int blue) {
    this.blue = blue;
  }

  public void setGreen(final int green) {
    this.green = green;
  }

  public void setRed(final int red) {
    this.red = red;
  }

  public Color toColor() {
    return new Color(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
  }

  public String toHexString() {
    return "#" + Integer.toHexString(this.toColor().getRGB()).substring(2);
  }

}