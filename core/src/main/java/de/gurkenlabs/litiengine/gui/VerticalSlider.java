package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.MathUtilities;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class VerticalSlider extends Slider {

  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE804");
  public static final FontIcon ARROW_UP = new FontIcon(ICON_FONT, "\uE807");

  public VerticalSlider(
      final double x,
      final double y,
      final double width,
      final double height,
      final float minValue,
      final float maxValue,
      final float stepSize) {
    super(x, y, width, height, minValue, maxValue, stepSize);
  }

  @Override
  public Point2D getRelativeSliderLocation() {
    try {
      float frac = MathUtilities.clamp(
          (getCurrentValue() - getMinValue()) / (getMaxValue() - getMinValue()), 0, 1);
      int currentStep = (int) (frac * (getSteps() - 1));
      return new Point2D.Double(getX(), getY() + currentStep * getSliderComponent().getHeight());
    } catch (Exception e) {
      return new Point2D.Double(0, 0);
    }
  }


  @Override
  protected void updateSliderDimensions() {
    getSliderComponent().setHeight(getHeight() / (getSteps()));
  }

  @Override
  protected void renderBar(Graphics2D g) {
    ShapeRenderer.renderOutline(g,
        new Line2D.Double(getX() + getWidth() / 2d, getY(),
            getX() + getWidth() / 2d, getY() + getHeight()),
        (float) (getWidth() / 10f) * getTickSize());
  }

  @Override
  protected void renderTicks(Graphics2D g) {
    for (int i = 1; i < getSteps(); i++) {
      ShapeRenderer.renderOutline(g,
          new Line2D.Double(getX() + getWidth() / 2d - getTickSize() * getWidth() / 2d,
              getY() + i * getHeight() / getSteps(),
              getX() + getWidth() / 2d + getTickSize() * getWidth() / 2d,
              getY() + i * getHeight() / getSteps()),
          (float) (getWidth() / 200f * getTickSize()));
    }
  }

  @Override
  protected float getRelativeMouseValue() {
    return (float) ((Input.mouse().getLocation().getY() - getY()) / getHeight());
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    setButton1(
        new ImageComponent(
            getX(),
            getY() - getWidth(),
            getWidth(),
            getWidth(),
            getButtonSpritesheet(),
            ARROW_UP.getText(),
            null));
    getButton1().setFont(ARROW_UP.getFont());
    setButton2(
        new ImageComponent(
            getX(),
            getY() + getHeight(),
            getWidth(),
            getWidth(),
            getButtonSpritesheet(),
            ARROW_DOWN.getText(),
            null));
    getButton2().setFont(ARROW_DOWN.getFont());
    setSliderComponent(
        new ImageComponent(
            getRelativeSliderLocation().getX(),
            getRelativeSliderLocation().getY(),
            getWidth(),
            getWidth() * 2,
            getSliderSpritesheet(),
            "",
            null));
  }
}
