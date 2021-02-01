package com.litiengine.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Appearance {
  private final List<Consumer<Appearance>> changedConsumer;

  private Color foreColor;
  private Color backgroundColor1;
  private Color backgroundColor2;
  private Color borderColor;
  private Stroke borderStyle;
  private float borderRadius;
  private boolean horizontalBackgroundGradient;
  private boolean transparentBackground;

  public Appearance() {
    this.changedConsumer = new CopyOnWriteArrayList<>();
  }

  public Appearance(Color foreColor) {
    this();
    this.foreColor = foreColor;
    this.setTransparentBackground(true);
  }

  public Appearance(Color foreColor, Color backColor) {
    this();
    this.foreColor = foreColor;
    this.backgroundColor1 = backColor;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Appearance)) {
      return false;
    }
    Appearance other = (Appearance) obj;
    return this.transparentBackground == other.transparentBackground
        && this.horizontalBackgroundGradient == other.horizontalBackgroundGradient
        && Float.floatToIntBits(this.borderRadius) == Float.floatToIntBits(other.borderRadius)
        && Objects.equals(this.borderColor, other.borderColor)
        && Objects.equals(this.borderStyle, other.borderStyle)
        && Objects.equals(this.backgroundColor1, other.backgroundColor1)
        && Objects.equals(this.backgroundColor2, other.backgroundColor2)
        && Objects.equals(this.foreColor, other.foreColor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.transparentBackground, this.horizontalBackgroundGradient, this.borderRadius,
        this.borderColor, this.borderStyle, this.backgroundColor1, this.backgroundColor2, this.foreColor);
  }

  public Color getForeColor() {
    return this.foreColor;
  }

  public Color getBackgroundColor1() {
    return this.backgroundColor1;
  }

  public Color getBackgroundColor2() {
    return this.backgroundColor2;
  }

  public Paint getBackgroundPaint(double width, double height) {
    if (this.isTransparentBackground()) {
      return null;
    }
    if (this.backgroundColor1 == null) {
      return this.backgroundColor2;
    } else if (this.backgroundColor2 == null) {
      return this.backgroundColor1;
    }

    if (this.horizontalBackgroundGradient) {
      return new GradientPaint(0, 0, this.backgroundColor1, (float) (width / 2.0), 0, this.backgroundColor2);
    } else {
      return new GradientPaint(0, 0, this.backgroundColor1, 0, (float) (height / 2.0), this.backgroundColor2);
    }
  }

  public Color getBorderColor() {
    return this.borderColor;
  }

  public Stroke getBorderStyle() {
    return this.borderStyle;
  }

  public float getBorderRadius() {
    return this.borderRadius;
  }

  public boolean isHorizontalBackgroundGradient() {
    return this.horizontalBackgroundGradient;
  }

  public boolean isTransparentBackground() {
    return transparentBackground;
  }

  public void setForeColor(Color foreColor) {
    this.foreColor = foreColor;
    this.fireOnChangeEvent();
  }

  public void setBackgroundColor1(Color backColor1) {
    this.backgroundColor1 = backColor1;
    this.fireOnChangeEvent();
  }

  public void setBackgroundColor2(Color backColor2) {
    this.backgroundColor2 = backColor2;
    this.fireOnChangeEvent();
  }

  public void setBorderColor(Color color) {
    this.borderColor = color;
  }

  public void setBorderStyle(Stroke style) {
    this.borderStyle = style;
  }

  public void setBorderRadius(float radius) {
    this.borderRadius = radius;
  }

  public void setHorizontalBackgroundGradient(boolean horizontal) {
    this.horizontalBackgroundGradient = horizontal;
    this.fireOnChangeEvent();
  }

  public void setTransparentBackground(boolean transparentBackground) {
    this.transparentBackground = transparentBackground;
    this.fireOnChangeEvent();
  }

  public void onChange(Consumer<Appearance> cons) {
    this.changedConsumer.add(cons);
  }

  public void update(Appearance updateAppearance) {
    this.setBackgroundColor1(updateAppearance.getBackgroundColor1());
    this.setBackgroundColor2(updateAppearance.getBackgroundColor2());
    this.setForeColor(updateAppearance.getForeColor());
    this.setHorizontalBackgroundGradient(updateAppearance.isHorizontalBackgroundGradient());
    this.setTransparentBackground(updateAppearance.isTransparentBackground());
  }

  protected void fireOnChangeEvent() {
    for (Consumer<Appearance> cons : this.changedConsumer) {
      cons.accept(this);
    }
  }
}