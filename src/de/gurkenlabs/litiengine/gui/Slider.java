package de.gurkenlabs.litiengine.gui;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

public abstract class Slider extends GuiComponent {
  private float minValue, maxValue, currentValue;
  private ImageComponent button1, button2, slider;
  private final Spritesheet buttonSprite, sliderSprite;
  private final Sound hoverSound;
  private boolean showArrowButtons, isDragging;

  public Slider(double x, double y, double width, double height, float minValue, float maxValue, Spritesheet buttonSprite, Spritesheet sliderSprite, Sound hoverSound, boolean showArrowButtons) {
    super(x, y, width, height);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.setCurrentValue((this.getMinValue() + this.getMaxValue()) / 2);
    this.buttonSprite = buttonSprite;
    this.sliderSprite = sliderSprite;
    this.hoverSound = hoverSound;
    this.showArrowButtons = showArrowButtons;
  }

  public boolean isDragging() {
    return this.isDragging;
  }

  public Sound getHoverSound() {
    return this.hoverSound;
  }

  public boolean arrowButtonsShown() {
    return this.showArrowButtons;
  }

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public abstract Point2D getRelativeSliderPosition();

  public abstract void setValueRelativeToMousePosition();

  public Spritesheet getSliderSprite() {
    return this.sliderSprite;
  }

  public float getMinValue() {
    return this.minValue;
  }

  public float getMaxValue() {
    return this.maxValue;
  }

  public float getCurrentValue() {
    return this.currentValue;
  }

  public ImageComponent getButton1() {
    return this.button1;
  }

  public ImageComponent getButton2() {
    return this.button2;
  }

  public ImageComponent getSlider() {
    return this.slider;
  }

  public void setCurrentValue(float newValue) {
    if (newValue < this.getMinValue()) {
      this.currentValue = this.getMinValue();
    } else if (newValue > this.getMaxValue()) {
      this.currentValue = this.getMaxValue();
    } else {
      this.currentValue = newValue;
    }
  }

  protected void setButton1(ImageComponent button1) {
    this.button1 = button1;
    this.button1.onClicked(e -> {
      this.setCurrentValue(this.currentValue - (this.getMaxValue() - this.getMinValue()) / 50);
      this.slider.setPosition(this.getRelativeSliderPosition());
    });
    this.getComponents().add(button1);
  }

  protected void setButton2(ImageComponent button2) {
    this.button2 = button2;
    this.button2.onClicked(e -> {
      this.setCurrentValue(this.currentValue + (this.getMaxValue() - this.getMinValue()) / 50);
      this.slider.setPosition(this.getRelativeSliderPosition());
    });
    this.getComponents().add(button2);

  }

  protected void setSlider(ImageComponent slider) {
    this.slider = slider;
    this.slider.onMousePressed(e -> {
      this.isDragging = true;
    });
    Input.MOUSE.onDragged(e -> {
      if (this.isDragging()) {
        this.setValueRelativeToMousePosition();
        this.slider.setPosition(this.getRelativeSliderPosition());
      }
    });

    Input.MOUSE.onReleased(e -> {
      if (this.isDragging()) {
        this.isDragging = false;
      }
    });

    this.getComponents().add(slider);

  }
}
