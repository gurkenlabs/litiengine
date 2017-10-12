package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

public class HorizontalSlider extends Slider {
  public static final FontIcon ARROW_LEFT = new FontIcon(ICON_FONT, "\uE805");
  public static final FontIcon ARROW_RIGHT = new FontIcon(ICON_FONT, "\uE806");
  private final double minSliderX;
  private final double maxSliderX;

  public HorizontalSlider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize, final Spritesheet buttonSprite, final Spritesheet sliderSprite, final boolean showArrowButtons) {
    super(x, y, width, height, minValue, maxValue, stepSize, buttonSprite, sliderSprite, showArrowButtons);
    this.minSliderX = this.getX() + this.getHeight();
    this.maxSliderX = this.getX() + this.getWidth() - this.getHeight() * 3;
  }

  @Override
  public Point2D getRelativeSliderPosition() {
    return new Point2D.Double(this.minSliderX + this.getCurrentValue() / (this.getMaxValue() - this.getMinValue()) * (this.maxSliderX - this.minSliderX), this.getY());
  }

  @Override
  public void prepare() {
    if (this.arrowButtonsShown()) {
      this.setButton1(new ImageComponent(this.getX(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSprite(), ARROW_LEFT.getText(), null));
      this.getButton1().getAppearance().setFont(ARROW_LEFT.getFont());
      this.getButton1().getAppearanceDisabled().setFont(ARROW_LEFT.getFont());
      this.getButton1().getAppearanceHovered().setFont(ARROW_LEFT.getFont());
      this.setButton2(new ImageComponent(this.getX() + this.getWidth() - this.getHeight(), this.getY(), this.getHeight(), this.getHeight(), this.getButtonSprite(), ARROW_RIGHT.getText(), null));
      this.getButton2().getAppearance().setFont(ARROW_RIGHT.getFont());
      this.getButton2().getAppearanceDisabled().setFont(ARROW_RIGHT.getFont());
      this.getButton2().getAppearanceHovered().setFont(ARROW_RIGHT.getFont());
      this.getComponents().add(this.getButton1());
      this.getComponents().add(this.getButton2());
    }
    this.setSlider(new ImageComponent(this.getRelativeSliderPosition().getX(), this.getRelativeSliderPosition().getY(), this.getHeight() * 2, this.getHeight(), this.getSliderSprite(), "", null));
    this.getComponents().add(this.getSliderComponent());
    super.prepare();
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
