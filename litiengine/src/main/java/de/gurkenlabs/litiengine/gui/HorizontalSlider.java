/**
 * Represents a horizontal slider GUI component. A horizontal slider allows the user to select a value from a range by dragging a slider thumb along a
 * horizontal bar. This implementation also supports rendering tick marks, custom left and right arrow buttons, and updating slider dimensions
 * dynamically.
 */
package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A GUI component that represents a horizontal slider with customizable appearance and behavior.
 */
public class HorizontalSlider extends Slider {

  /**
   * Font icon for the left arrow button.
   */
  public static final FontIcon ARROW_LEFT = new FontIcon(ICON_FONT, "\uE805");

  /**
   * Font icon for the right arrow button.
   */
  public static final FontIcon ARROW_RIGHT = new FontIcon(ICON_FONT, "\uE806");

  /**
   * Initializes a new instance of the {@code HorizontalSlider} class with the specified properties.
   *
   * @param x        The x-coordinate of the slider's position.
   * @param y        The y-coordinate of the slider's position.
   * @param width    The width of the slider.
   * @param height   The height of the slider.
   * @param minValue The minimum value of the slider.
   * @param maxValue The maximum value of the slider.
   * @param stepSize The step size for the slider.
   */
  public HorizontalSlider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue,
    final float stepSize) {
    super(x, y, width, height, minValue, maxValue, stepSize);
  }

  /**
   * Gets the relative location of the slider thumb based on the current value.
   *
   * @return A {@code Point2D} representing the relative position of the slider thumb.
   */
  @Override public Point2D getRelativeSliderLocation() {
    try {
      float frac = Math.clamp((getCurrentValue() - getMinValue()) / (getMaxValue() - getMinValue()), 0, 1);
      int currentStep = (int) (frac * (getSteps() - 1));
      return new Point2D.Double(getX() + currentStep * getSliderComponent().getWidth(), getY());
    } catch (Exception e) {
      return new Point2D.Double(0, 0);
    }
  }

  /**
   * Updates the dimensions of the slider thumb based on the number of steps.
   */
  @Override protected void updateSliderDimensions() {
    getSliderComponent().setWidth(getWidth() / (getSteps()));
  }

  /**
   * Gets the relative value of the mouse's x-coordinate within the slider's range.
   *
   * @return A {@code float} representing the relative mouse value.
   */
  @Override protected float getRelativeMouseValue() {
    return (float) ((Input.mouse().getLocation().getX() - getX()) / getWidth());
  }

  /**
   * Initializes the components of the slider, including its buttons and thumb.
   */
  @Override protected void initializeComponents() {
    super.initializeComponents();
    setButton1(new ImageComponent(getX() - getHeight(), getY(), getHeight(), getHeight(), getButtonSpritesheet(), ARROW_LEFT.getText(), null));
    getButton1().setFont(ARROW_LEFT.getFont());
    setButton2(new ImageComponent(getX() + getWidth(), getY(), getHeight(), getHeight(), getButtonSpritesheet(), ARROW_RIGHT.getText(), null));
    getButton2().setFont(ARROW_RIGHT.getFont());
    setSliderComponent(
      new ImageComponent(getRelativeSliderLocation().getX(), getRelativeSliderLocation().getY(), getHeight() * 2, getHeight(), getSliderSpritesheet(),
        "", null));
  }

  /**
   * Renders the slider's bar on the specified graphics context.
   *
   * @param g The {@code Graphics2D} context on which to render the bar.
   */
  @Override protected void renderBar(Graphics2D g) {
    ShapeRenderer.renderOutline(g, new Line2D.Double(getX(), getY() + getHeight() / 2d, getX() + getWidth(), getY() + getHeight() / 2d),
      (float) (getHeight() / 10f) * getTickSize());
  }

  /**
   * Renders the tick marks of the slider on the specified graphics context.
   *
   * @param g The {@code Graphics2D} context on which to render the tick marks.
   */
  @Override protected void renderTicks(Graphics2D g) {
    for (int i = 1; i < getSteps(); i++) {
      ShapeRenderer.renderOutline(g,
        new Line2D.Double(getX() + i * getWidth() / getSteps(), getY() + getHeight() / 2d - getTickSize() * getHeight() / 2d,
          getX() + i * getWidth() / getSteps(), getY() + getHeight() / 2d + getTickSize() * getHeight() / 2d),
        (float) (getWidth() / 200f * getTickSize()));
    }
  }
}
