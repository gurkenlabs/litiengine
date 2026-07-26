package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Represents the appearance settings for a GUI component.
 */
public class Appearance {
  /**
   * A list of consumers to be notified when the appearance changes.
   */
  private final List<Consumer<Appearance>> changedConsumer;
  /**
   * The foreground color.
   */
  private Color foreColor;
  /**
   * The first background color.
   */
  private Color backgroundColor1;
  /**
   * The second background color.
   */
  private Color backgroundColor2;
  /**
   * The border color.
   */
  private Color borderColor;
  /**
   * The border style.
   */
  private Stroke borderStyle;
  /**
   * The border radius.
   */
  private float borderRadius;
  /**
   * Indicates if the background gradient is horizontal.
   */
  private boolean horizontalBackgroundGradient;
  /**
   * Indicates if the background is transparent.
   */
  private boolean transparentBackground;

  /**
   * Constructs a new Appearance with default settings.
   */
  public Appearance() {
    this.changedConsumer = new CopyOnWriteArrayList<>();
  }

  /**
   * Constructs a new Appearance with the specified foreground color.
   *
   * @param foreColor the foreground color
   */
  public Appearance(Color foreColor) {
    this();
    this.foreColor = foreColor;
    this.setTransparentBackground(true);
  }

  /**
   * Constructs a new Appearance with the specified foreground and background colors.
   *
   * @param foreColor the foreground color
   * @param backColor the background color
   */
  public Appearance(Color foreColor, Color backColor) {
    this();
    this.foreColor = foreColor;
    this.backgroundColor1 = backColor;
  }

  /**
   * Compares this Appearance object to the specified object for equality.
   *
   * @param obj the object to compare with
   * @return true if the specified object is equal to this Appearance, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Appearance app)) {
      return false;
    }
    return this.transparentBackground == app.transparentBackground
      && this.horizontalBackgroundGradient == app.horizontalBackgroundGradient
      && Float.floatToIntBits(this.borderRadius) == Float.floatToIntBits(app.borderRadius)
      && Objects.equals(this.borderColor, app.borderColor)
      && Objects.equals(this.borderStyle, app.borderStyle)
      && Objects.equals(this.backgroundColor1, app.backgroundColor1)
      && Objects.equals(this.backgroundColor2, app.backgroundColor2)
      && Objects.equals(this.foreColor, app.foreColor);
  }

  /**
   * Returns a hash code value for this Appearance object.
   *
   * @return a hash code value for this Appearance object
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      this.transparentBackground,
      this.horizontalBackgroundGradient,
      this.borderRadius,
      this.borderColor,
      this.borderStyle,
      this.backgroundColor1,
      this.backgroundColor2,
      this.foreColor);
  }

  /**
   * Gets the foreground color.
   *
   * @return the foreground color
   */
  public Color getForeColor() {
    return foreColor;
  }

  /**
   * Gets the first background color.
   *
   * @return the first background color
   */
  public Color getBackgroundColor1() {
    return this.backgroundColor1;
  }

  /**
   * Gets the second background color.
   *
   * @return the second background color
   */
  public Color getBackgroundColor2() {
    return this.backgroundColor2;
  }

  /**
   * Gets the background paint for the specified dimensions.
   *
   * @param width  the width of the component
   * @param height the height of the component
   * @return the background paint, or null if the background is transparent
   */
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
      return new GradientPaint(
        0, 0, this.backgroundColor1, (float) (width / 2.0), 0, this.backgroundColor2);
    } else {
      return new GradientPaint(
        0, 0, this.backgroundColor1, 0, (float) (height / 2.0), this.backgroundColor2);
    }
  }

  /**
   * Gets the border color.
   *
   * @return the border color
   */
  public Color getBorderColor() {
    return this.borderColor;
  }

  /**
   * Gets the border style.
   *
   * @return the border style
   */
  public Stroke getBorderStyle() {
    return this.borderStyle;
  }

  /**
   * Gets the border radius.
   *
   * @return the border radius
   */
  public float getBorderRadius() {
    return this.borderRadius;
  }

  /**
   * Checks if the background gradient is horizontal.
   *
   * @return true if the background gradient is horizontal, false otherwise
   */
  public boolean isHorizontalBackgroundGradient() {
    return this.horizontalBackgroundGradient;
  }

  /**
   * Checks if the background is transparent.
   *
   * @return true if the background is transparent, false otherwise
   */
  public boolean isTransparentBackground() {
    return transparentBackground;
  }

  /**
   * Sets the foreground color and triggers a change event.
   *
   * @param foreColor the new foreground color
   */
  public void setForeColor(Color foreColor) {
    this.foreColor = foreColor;
    this.fireOnChangeEvent();
  }

  /**
   * Sets the first background color and triggers a change event.
   *
   * @param backColor1 the new first background color
   */
  public void setBackgroundColor1(Color backColor1) {
    this.backgroundColor1 = backColor1;
    this.fireOnChangeEvent();
  }

  /**
   * Sets the second background color and triggers a change event.
   *
   * @param backColor2 the new second background color
   */
  public void setBackgroundColor2(Color backColor2) {
    this.backgroundColor2 = backColor2;
    this.fireOnChangeEvent();
  }

  /**
   * Sets the border color.
   *
   * @param color the new border color
   */
  public void setBorderColor(Color color) {
    this.borderColor = color;
  }

  /**
   * Sets the border style.
   *
   * @param style the new border style
   */
  public void setBorderStyle(Stroke style) {
    this.borderStyle = style;
  }

  /**
   * Sets the border radius.
   *
   * @param radius the new border radius
   */
  public void setBorderRadius(float radius) {
    this.borderRadius = radius;
  }

  /**
   * Sets whether the background gradient is horizontal and triggers a change event.
   *
   * @param horizontal true if the background gradient is horizontal, false otherwise
   */
  public void setHorizontalBackgroundGradient(boolean horizontal) {
    this.horizontalBackgroundGradient = horizontal;
    this.fireOnChangeEvent();
  }

  /**
   * Sets whether the background is transparent and triggers a change event.
   *
   * @param transparentBackground true if the background is transparent, false otherwise
   */
  public void setTransparentBackground(boolean transparentBackground) {
    this.transparentBackground = transparentBackground;
    this.fireOnChangeEvent();
  }

  /**
   * Adds a consumer to be notified when the appearance changes.
   *
   * @param cons the consumer to be added
   */
  public void onChange(Consumer<Appearance> cons) {
    this.changedConsumer.add(cons);
  }

  /**
   * Updates the appearance settings with the values from the specified Appearance object.
   *
   * @param updateAppearance the Appearance object containing the new settings
   */
  public void update(Appearance updateAppearance) {
    this.setBackgroundColor1(updateAppearance.getBackgroundColor1());
    this.setBackgroundColor2(updateAppearance.getBackgroundColor2());
    this.setForeColor(updateAppearance.getForeColor());
    this.setBorderColor(updateAppearance.getBorderColor());
    this.setBorderRadius(updateAppearance.getBorderRadius());
    this.setBorderStyle(updateAppearance.getBorderStyle());
    this.setHorizontalBackgroundGradient(updateAppearance.isHorizontalBackgroundGradient());
    this.setTransparentBackground(updateAppearance.isTransparentBackground());
  }

  /**
   * Triggers the change event for all registered consumers.
   */
  protected void fireOnChangeEvent() {
    for (Consumer<Appearance> cons : this.changedConsumer) {
      cons.accept(this);
    }
  }
}
