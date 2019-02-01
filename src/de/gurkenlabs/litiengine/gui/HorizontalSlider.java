package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.input.Input;

public class HorizontalSlider extends Slider {
  public static final FontIcon ARROW_LEFT = new FontIcon(ICON_FONT, "\uE805");
  public static final FontIcon ARROW_RIGHT = new FontIcon(ICON_FONT, "\uE806");
  private double minSliderX;
  private double maxSliderX;

  public HorizontalSlider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize) {
    super(x, y, width, height, minValue, maxValue, stepSize);
  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.minSliderX + this.getCurrentValue() / (this.getMaxValue() - this.getMinValue()) * (this.maxSliderX - this.minSliderX), this.getY());
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();

    this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSpritesheet(), ARROW_LEFT.getText(), null));
    this.getButton1().setFont(ARROW_LEFT.getFont());
    this.setButton2(new ImageComponent(this.getX() + this.getWidth() - this.getHeight(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSpritesheet(), ARROW_RIGHT.getText(), null));
    this.getButton2().setFont(ARROW_RIGHT.getFont());

    final double sliderWidth = this.getHeight() * 2;
    this.minSliderX = this.getX() + this.getHeight();
    this.maxSliderX = this.getX() + this.getWidth() - this.getHeight() * 3;
    this.setSliderComponent(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), sliderWidth, this.getHeight(), this.getSliderSpritesheet(), "", null));
  }

  @Override
  public void render(final Graphics2D g) {
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getHeight() / 8)));
    g.setColor(this.getAppearance().getForeColor());
    g.drawLine((int) this.minSliderX, (int) (this.getY() + this.getHeight() / 2), (int) (this.getX() + this.getWidth() - this.getHeight()), (int) (this.getY() + this.getHeight() / 2));
    g.setStroke(oldStroke);
    super.render(g);

  }

  @Override
  public void setValueRelativeToMousePosition() {
    final double mouseX = Input.mouse().getLocation().getX();
    if (mouseX >= this.minSliderX && mouseX <= this.maxSliderX) {
      final double relativeMouseX = mouseX - this.minSliderX;
      final double percentage = relativeMouseX / (this.maxSliderX - this.minSliderX);
      this.setCurrentValue((float) (this.getMinValue() + percentage * (this.getMaxValue() - this.getMinValue())));
    }
  }
}
