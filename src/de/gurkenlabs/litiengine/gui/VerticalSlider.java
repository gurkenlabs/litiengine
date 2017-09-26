package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public class VerticalSlider extends Slider {
  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE804");
  public static final FontIcon ARROW_UP = new FontIcon(ICON_FONT, "\uE807");
  private double minSliderY;
  private double maxSliderY;
  private double sliderHeight;

  public VerticalSlider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize, final Spritesheet buttonSprite, final Spritesheet sliderSprite, final Sound hoverSound, final boolean showArrowButtons) {
    super(x, y, width, height, minValue, maxValue, stepSize, buttonSprite, sliderSprite, showArrowButtons);

  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.getX(), this.minSliderY + this.getCurrentValue() / (this.getMaxValue() - this.getMinValue()) * (this.maxSliderY - this.minSliderY));
  }

  @Override
  public void prepare() {
    if (this.arrowButtonsShown()) {
      this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_UP.getText(), null));
      this.getButton1().getAppearance().setFont(ARROW_UP.getFont());
      this.getButton1().getAppearanceDisabled().setFont(ARROW_UP.getFont());
      this.getButton1().getAppearanceHovered().setFont(ARROW_UP.getFont());

      this.setButton2(new ImageComponent(this.getX(), this.getY() + this.getHeight() - this.getWidth(), this.getWidth(), this.getWidth(), this.getButtonSprite(), ARROW_DOWN.getText(), null));
      this.getButton2().getAppearance().setFont(ARROW_DOWN.getFont());
      this.getButton2().getAppearanceDisabled().setFont(ARROW_DOWN.getFont());
      this.getButton2().getAppearanceHovered().setFont(ARROW_DOWN.getFont());

      this.getComponents().add(this.getButton1());
      this.getComponents().add(this.getButton2());
    }
    this.sliderHeight = (this.getHeight() - this.getWidth() * 2) * 1 / 6;
    this.minSliderY = this.getY() + this.getWidth();
    this.maxSliderY = this.getY() + this.getHeight() - (this.getWidth() + this.sliderHeight);
    this.setSlider(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getWidth(), this.sliderHeight, this.getSliderSprite(), "", null));
    this.getComponents().add(this.getSliderComponent());
    super.prepare();
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
}