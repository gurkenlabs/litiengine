package de.gurkenlabs.litiengine.gui;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

public abstract class Slider extends GuiComponent {
  private ImageComponent button1, button2, slider;
  private final Spritesheet buttonSprite, sliderSprite;
  private final List<Consumer<Float>> changeConsumer;
  private float currentValue;
  private boolean isDragging;
  private final float minValue, maxValue;
  private final boolean showArrowButtons;
  private float stepSize;

  public Slider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue, final float stepSize, final Spritesheet buttonSprite, final Spritesheet sliderSprite, final boolean showArrowButtons) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.stepSize = stepSize;
    this.buttonSprite = buttonSprite;
    this.sliderSprite = sliderSprite;
    this.showArrowButtons = showArrowButtons;
  }

  public boolean arrowButtonsShown() {
    return this.showArrowButtons;
  }

  public ImageComponent getButton1() {
    return this.button1;
  }

  public ImageComponent getButton2() {
    return this.button2;
  }

  public Spritesheet getButtonSprite() {
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
    return this.slider;
  }

  public Spritesheet getSliderSprite() {
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

  @Override
  public void prepare() {
    super.prepare();
    this.setCurrentValue((this.getMinValue() + this.getMaxValue()) / 2);
    this.onChange(e -> {
      this.slider.setPosition(this.getRelativeSliderPosition());
    });
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

  @Override
  protected void initializeComponents() {

  }

  protected void setButton1(final ImageComponent button1) {
    this.button1 = button1;
    this.button1.onClicked(e -> {
      this.setCurrentValue(this.getCurrentValue() - this.getStepSize());
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));
    });
    this.getComponents().add(button1);
  }

  protected void setButton2(final ImageComponent button2) {
    this.button2 = button2;
    this.button2.onClicked(e -> {
      this.setCurrentValue(this.getCurrentValue() + this.getStepSize());
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));

    });
    this.getComponents().add(button2);

  }

  protected void setSlider(final ImageComponent slider) {
    this.slider = slider;
    this.slider.onMousePressed(e -> {
      this.isDragging = true;
    });
    Input.MOUSE.onDragged(e -> {
      if (this.isDragging()) {
        this.setValueRelativeToMousePosition();
        this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getCurrentValue()));
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
