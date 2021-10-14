package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.MathUtilities;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class HorizontalSlider extends Slider {

  public static final FontIcon ARROW_LEFT = new FontIcon(ICON_FONT, "\uE805");
  public static final FontIcon ARROW_RIGHT = new FontIcon(ICON_FONT, "\uE806");

  public HorizontalSlider(
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
  public Point2D getRelativeSliderPosition() {
    try {
      float frac = MathUtilities.clamp(
          (getCurrentValue() - getMinValue()) / (getMaxValue() - getMinValue()), 0, 1);
      int currentStep = (int) (frac * (getSteps() - 1));
      return new Point2D.Double(getX() + currentStep * getSliderComponent().getWidth(), getY());
    } catch (Exception e) {
      return new Point2D.Double(0, 0);
    }
  }


  @Override
  protected void updateSliderDimensions() {
    getSliderComponent().setWidth(getWidth() / (getSteps()));
  }

  @Override
  protected float getRelativeMouseValue() {
    return (float) ((Input.mouse().getLocation().getX() - getX()) / getWidth());
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    setButton1(
        new ImageComponent(
            getX() - getHeight(),
            getY(),
            getHeight(),
            getHeight(),
            getButtonSpritesheet(),
            ARROW_LEFT.getText(),
            null));
    getButton1().setFont(ARROW_LEFT.getFont());
    setButton2(
        new ImageComponent(
            getX() + getWidth(),
            getY(),
            getHeight(),
            getHeight(),
            getButtonSpritesheet(),
            ARROW_RIGHT.getText(),
            null));
    getButton2().setFont(ARROW_RIGHT.getFont());
    setSliderComponent(
        new ImageComponent(
            getRelativeSliderPosition().getX(),
            getRelativeSliderPosition().getY(),
            getHeight() * 2,
            getHeight(),
            getSliderSpritesheet(),
            "",
            null));
  }


  @Override
  protected void renderBar(Graphics2D g) {
    ShapeRenderer.renderOutline(g,
        new Line2D.Double(getX(), getY() + getHeight() / 2d,
            getX() + getWidth(), getY() + getHeight() / 2d),
        (float) (getHeight() / 10f) * getTickSize());
  }

  @Override
  protected void renderTicks(Graphics2D g) {
    for (int i = 1; i < getSteps(); i++) {
      ShapeRenderer.renderOutline(g,
          new Line2D.Double(getX() + i * getWidth() / getSteps(),
              getY() + getHeight() / 2d - getTickSize() * getHeight() / 2d,
              getX() + i * getWidth() / getSteps(),
              getY() + getHeight() / 2d + getTickSize() * getHeight() / 2d),
          (float) (getWidth() / 200f * getTickSize()));
    }
  }
}
