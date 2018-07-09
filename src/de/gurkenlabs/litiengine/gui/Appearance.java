package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Appearance {
  private final List<Consumer<Appearance>> changedConsumer;

  private Color foreColor;
  private Color backgroundColor1;
  private Color backgroundColor2;
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
    if (obj instanceof Appearance) {
      return this.hashCode() == obj.hashCode();
    }

    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getForeColor(), this.getBackgroundColor1(), this.getBackgroundColor2(), this.isHorizontalBackgroundGradient(), this.isTransparentBackground());
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