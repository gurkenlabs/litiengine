package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public class VerticalSlider extends Slider {
  private double minSliderY, maxSliderY, sliderHeight;
  public static Icon ARROW_UP = new Icon(FontLoader.getIconFontThree(), "\uE807");
  public static Icon ARROW_DOWN = new Icon(FontLoader.getIconFontThree(), "\uE804");

  public VerticalSlider(double x, double y, double width, double height, float minValue, float maxValue, float stepSize, Spritesheet buttonSprite, Spritesheet sliderSprite, Sound hoverSound, boolean showArrowButtons) {
    super(x, y, width, height, minValue, maxValue, stepSize, buttonSprite, sliderSprite, showArrowButtons);

  }

  @Override
  public void prepare() {
    if (this.arrowButtonsShown()) {
      this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_UP.getText(), null));
      this.getButton1().setFont(ARROW_UP.getFont());
      this.setButton2(new ImageComponent(this.getX(), this.getY() + this.getHeight() - this.getWidth(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_DOWN.getText(), null));
      this.getButton2().setFont(ARROW_DOWN.getFont());
      this.getComponents().add(this.getButton1());
      this.getComponents().add(this.getButton2());
    }
    this.sliderHeight = (this.getHeight() - this.getWidth() * 2) * 1 / 6;
    this.minSliderY = this.getY() + this.getWidth();
    this.maxSliderY = this.getY() + this.getHeight() - (this.getWidth() + sliderHeight);
    this.setSlider(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getWidth(), sliderHeight, this.getSliderSprite(), "", null));
    this.getComponents().add(this.getSliderComponent());
    super.prepare();
  }

  @Override
  public void render(Graphics2D g) {
    Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getWidth() / 8)));
    g.setColor(this.getTextColor());
    g.drawLine((int) (this.getX() + this.getWidth() / 2), (int) minSliderY, (int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() - this.getWidth()));
    g.setStroke(oldStroke);
    super.render(g);
  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.getX(), this.minSliderY + (this.getCurrentValue() / ((this.getMaxValue() - this.getMinValue()))) * (this.maxSliderY - minSliderY));
  }

  @Override
  protected void initializeComponents() {
  }

  @Override
  public void setValueRelativeToMousePosition() {
    double mouseY = Input.MOUSE.getLocation().getY();
    if (mouseY >= this.minSliderY && mouseY <= this.maxSliderY) {
      double relativeMouseX = mouseY - this.minSliderY;
      double percentage = relativeMouseX / (this.maxSliderY - this.minSliderY);
      this.setCurrentValue((float) (this.getMinValue() + percentage * (this.getMaxValue() - this.getMinValue())));
    }
  }

}
