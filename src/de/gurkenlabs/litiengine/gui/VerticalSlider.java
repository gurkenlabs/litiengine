package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public class VerticalSlider extends Slider {
  private double minSliderY, maxSliderY;
  public static Icon ARROW_UP = new Icon(FontLoader.getIconFontThree(), "");
  public static Icon ARROW_DOWN = new Icon(FontLoader.getIconFontThree(), "");

  public VerticalSlider(double x, double y, double width, double height, float minValue, float maxValue, Spritesheet buttonSprite, Spritesheet sliderSprite, Sound hoverSound, boolean showArrowButtons) {
    super(x, y, width, height, minValue, maxValue, buttonSprite, sliderSprite, hoverSound, showArrowButtons);
    this.minSliderY = this.getY() + this.getWidth();
    this.maxSliderY = this.getY() + this.getHeight() - this.getWidth() * 3;
  }

  @Override
  public void prepare() {
    if (this.arrowButtonsShown()) {
      this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_UP.getText(), null, this.getHoverSound()));
      this.getButton1().setFont(ARROW_UP.getFont());
      this.setButton2(new ImageComponent(this.getX(), this.getY() + this.getHeight() - this.getWidth(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_DOWN.getText(), null, null));
      this.getButton2().setFont(ARROW_DOWN.getFont());
    }
    this.setSlider(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getWidth(), this.getWidth() * 2, this.getSliderSprite(), "", null, null));
    super.prepare();
  }

  @Override
  public void render(Graphics2D g) {
    g.setStroke(new BasicStroke((float) (this.getWidth() / 8)));
    g.setColor(this.getTextColor());
    g.drawLine((int) (this.getX() + this.getWidth() / 2), (int) minSliderY, (int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() - this.getWidth()));
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
