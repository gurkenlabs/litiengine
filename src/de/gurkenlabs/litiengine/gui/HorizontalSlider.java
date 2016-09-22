package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public class HorizontalSlider extends Slider {
  private double minSliderX, maxSliderX;
  public static Icon ARROW_RIGHT = new Icon(FontLoader.getIconFontThree(), "\uE806");
  public static Icon ARROW_LEFT = new Icon(FontLoader.getIconFontThree(), "\uE805");

  public HorizontalSlider(double x, double y, double width, double height, float minValue, float maxValue, float stepSize, Spritesheet buttonSprite, Spritesheet sliderSprite, Sound hoverSound, boolean showArrowButtons) {
    super(x, y, width, height, minValue, maxValue, stepSize, buttonSprite, sliderSprite, showArrowButtons);
    this.minSliderX = this.getX() + this.getHeight();
    this.maxSliderX = this.getX() + this.getWidth() - this.getHeight() * 3;
  }

  @Override
  public void prepare() {
    if (this.arrowButtonsShown()) {
      this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSprite(), ARROW_LEFT.getText(), null));
      this.getButton1().setFont(ARROW_LEFT.getFont());
      this.setButton2(new ImageComponent(this.getX() + this.getWidth() - this.getHeight(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSprite(), ARROW_RIGHT.getText(), null));
      this.getButton2().setFont(ARROW_RIGHT.getFont());
      this.getComponents().add(this.getButton1());
      this.getComponents().add(this.getButton2());
    }
    this.setSlider(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getHeight() * 2, this.getHeight(), this.getSliderSprite(), "", null));
    this.getComponents().add(this.getSliderComponent());
    super.prepare();
    this.setTextColor(this.getTextColor());
  }

  @Override
  public void render(Graphics2D g) {
    Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getHeight() / 8)));
    g.setColor(this.getTextColor());
    g.drawLine((int) minSliderX, (int) (this.getY() + this.getHeight() / 2), (int) (this.getX() + this.getWidth() - this.getHeight()), (int) (this.getY() + this.getHeight() / 2));
    g.setStroke(oldStroke);
    super.render(g);

  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.minSliderX + (this.getCurrentValue() / ((this.getMaxValue() - this.getMinValue()))) * (this.maxSliderX - minSliderX), this.getY());
  }

  @Override
  protected void initializeComponents() {

  }

  @Override
  public void setValueRelativeToMousePosition() {
    double mouseX = Input.MOUSE.getLocation().getX();
    if (mouseX >= this.minSliderX && mouseX <= this.maxSliderX) {
      double relativeMouseX = mouseX - this.minSliderX;
      double percentage = relativeMouseX / (this.maxSliderX - this.minSliderX);
      this.setCurrentValue((float) (this.getMinValue() + percentage * (this.getMaxValue() - this.getMinValue())));
    }
  }

}
