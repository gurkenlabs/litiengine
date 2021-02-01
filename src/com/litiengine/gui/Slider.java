package com.litiengine.gui;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.litiengine.graphics.Spritesheet;
import com.litiengine.input.Input;

public abstract class Slider extends GuiComponent {
  private ImageComponent button1;
  private ImageComponent button2;
  private ImageComponent sliderComponent;

  private Spritesheet buttonSprite;
  private Spritesheet sliderSprite;
  private final List<Consumer<Float>> changeConsumer;
  private float currentValue;
  private boolean isDragging;
  private final float minValue;
  private final float maxValue;
  private float stepSize;

  public Slider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.stepSize = stepSize;
    this.onChange(e -> this.sliderComponent.setLocation(this.getRelativeSliderPosition()));
  }

  public ImageComponent getButton1() {
    return this.button1;
  }

  public ImageComponent getButton2() {
    return this.button2;
  }

  public Spritesheet getButtonSpritesheet() {
    return this.buttonSprite;
  }

  public List<Consumer<Float>> getChangeConsumer() {
    return this.changeConsumer;
  }

  public float getCurrentValue() {
    return this.currentValue;
  }

  public float getMaxValue() {
    return this.maxValue;
  }

  public float getMinValue() {
    return this.minValue;
  }

  public abstract Point2D getRelativeSliderPosition();

  public ImageComponent getSliderComponent() {
    return this.sliderComponent;
  }

  public Spritesheet getSliderSpritesheet() {
    return this.sliderSprite;
  }

  public float getStepSize() {
    return this.stepSize;
  }

  public boolean isDragging() {
    return this.isDragging;
  }

  public void onChange(final Consumer<Float> c) {
    this.getChangeConsumer().add(c);
  }

  public void setButtonSpritesheet(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public void setSliderSpritesheet(Spritesheet sliderSprite) {
    this.sliderSprite = sliderSprite;
  }

  public void setCurrentValue(final float newValue) {
    if (newValue >= this.getMinValue() && newValue <= this.getMaxValue()) {
      this.currentValue = newValue;
    } else if (newValue < this.getMinValue()) {
      this.currentValue = this.getMinValue();
    } else if (newValue > this.getMaxValue()) {
      this.currentValue = this.getMaxValue();
    }
  }

  public void setStepSize(final float stepSize) {
    this.stepSize = stepSize;
  }

  public abstract void setValueRelativeToMousePosition();

  protected void setButton1(final ImageComponent button1) {
    this.button1 = button1;
    this.button1.onClicked(e -> {
      this.setCurrentValue(this.getCurrentValue() - this.getStepSize());
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));
    });
    this.getComponents().add(this.getButton1());
  }

  protected void setButton2(final ImageComponent button2) {
    this.button2 = button2;
    this.button2.onClicked(e -> {
      this.setCurrentValue(this.getCurrentValue() + this.getStepSize());
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));

    });
    this.getComponents().add(this.getButton2());

  }

  protected void setSliderComponent(final ImageComponent slider) {
    this.sliderComponent = slider;
    this.sliderComponent.onMousePressed(e -> this.isDragging = true);
    Input.mouse().onDragged(e -> {
      if (this.isDragging()) {
        this.setValueRelativeToMousePosition();
        this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));
      }
    });

    Input.mouse().onReleased(e -> {
      if (this.isDragging()) {
        this.isDragging = false;
      }
    });
    this.getComponents().add(this.getSliderComponent());
  }
}
