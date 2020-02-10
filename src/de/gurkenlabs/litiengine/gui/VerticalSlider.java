package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.input.Input;

public class VerticalSlider extends Slider {
  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE804");
  public static final FontIcon ARROW_UP = new FontIcon(ICON_FONT, "\uE807");
  private double minSliderY;
  private double maxSliderY;

  public VerticalSlider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize) {
    super(x, y, width, height, minValue, maxValue, stepSize);
  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.getX(), this.minSliderY + this.getCurrentValue() / (this.getMaxValue() - this.getMinValue()) * (this.maxSliderY - this.minSliderY));
  }

  @Override
  public void render(final Graphics2D g) {
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getWidth() / 8)));
    g.setColor(this.getAppearance().getForeColor());
    g.drawLine((int) (this.getX() + this.getWidth() / 2), (int) this.minSliderY, (int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() - this.getWidth()));
    g.setStroke(oldStroke);
    super.render(g);
  }

  @Override
  public void setValueRelativeToMousePosition() {
    final double mouseY = Input.mouse().getLocation().getY();
    if (mouseY >= this.minSliderY && mouseY <= this.maxSliderY) {
      final double relativeMouseX = mouseY - this.minSliderY;
      final double percentage = relativeMouseX / (this.maxSliderY - this.minSliderY);
      this.setCurrentValue((float) (this.getMinValue() + percentage * (this.getMaxValue() - this.getMinValue())));
    }
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getWidth(), ARROW_UP.getText()));
    this.getButton1().setFont(ARROW_UP.getFont());
    this.getButton1().setSpriteSheet(this.getButtonSpritesheet());
    this.setButton2(new ImageComponent(this.getX(), this.getY() + this.getHeight() - this.getWidth(), this.getWidth(), this.getWidth(), ARROW_DOWN.getText()));
    this.getButton2().setFont(ARROW_DOWN.getFont());
    this.getButton2().setSpriteSheet(this.getButtonSpritesheet());

    final double sliderHeight = (this.getHeight() - this.getWidth() * 2) * 1 / 6.0;
    this.minSliderY = this.getY() + this.getWidth();
    this.maxSliderY = this.getY() + this.getHeight() - (this.getWidth() + sliderHeight);
    this.setSliderComponent(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getWidth(), sliderHeight, this.getSliderSpritesheet(), "", null));
    this.getSliderComponent().setSpriteSheet(this.getSliderSpritesheet());
  }
}