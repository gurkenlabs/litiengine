package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.Color;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "color")
public class ParticleColor implements Serializable {
  private static final long serialVersionUID = -5962060934939835282L;

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

  public static ParticleColor decode(final String particleColorString) {
    if (particleColorString == null) {
      return null;
    }

    String[] split = particleColorString.split("|");
    if (split.length < 2) {
      return null;
    }
    Color rgba;
    try {
      Color solid = Color.decode(split[0]);
      rgba = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), Integer.parseInt(split[1]));
    } catch (NumberFormatException e) {
      return null;
    }

    return new ParticleColor(rgba);
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
    if (this.getAlpha() == 0) {
      return new Color(this.getRed(), this.getGreen(), this.getBlue());
    }

    return new Color(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
  }

  public String toHexString() {
    int rgb = this.toColor().getRGB();
    if (rgb == 0) {
      return "#000000";
    }

    return "#" + Integer.toHexString(rgb).substring(2);
  }

  @Override
  public String toString() {
    return this.toHexString() + "|" + this.getAlpha();
  }
}