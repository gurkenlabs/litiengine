package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class Slider extends GuiComponent {

  private final List<Consumer<Float>> changeConsumer = new CopyOnWriteArrayList<>();
  private ImageComponent button1;
  private ImageComponent button2;
  private ImageComponent sliderComponent;
  private Spritesheet buttonSprite;
  private Spritesheet sliderSprite;
  private float currentValue;
  private boolean isDragging;

  private boolean showTicks;
  private float minValue;
  private float maxValue;
  private float stepSize;

  private float tickSize;


  protected Slider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue,
    final float stepSize) {
    super(x, y, width, height);
    setMinValue(minValue);
    setCurrentValue(getMinValue());
    setMaxValue(maxValue);
    setStepSize(stepSize);
    setShowTicks(true);
    setTickSize(.7f);

    getSliderComponent().getAppearance().setTransparentBackground(false);
    getSliderComponent().getAppearanceHovered().setTransparentBackground(false);
    onChange(e -> getSliderComponent().setLocation(getRelativeSliderLocation()));
    onClicked(e -> {
      inferValueFromMouseLocation();
      getSliderComponent().setLocation(getRelativeSliderLocation());
    });

    onMouseDragged(e -> {
      inferValueFromMouseLocation();
      getSliderComponent().setLocation(getRelativeSliderLocation());
    });

  }

  public ImageComponent getButton1() {
    return button1;
  }

  protected void setButton1(final ImageComponent button1) {
    this.button1 = button1;
    this.button1.onClicked(e -> {
      this.setCurrentValue(getCurrentValue() - getStepSize());
      getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
    });
    this.getComponents().add(this.getButton1());
  }

  public ImageComponent getButton2() {
    return button2;
  }

  protected void setButton2(final ImageComponent button2) {
    this.button2 = button2;
    this.button2.onClicked(e -> {
      this.setCurrentValue(getCurrentValue() + getStepSize());
      getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
    });
    getComponents().add(getButton2());
  }

  public Spritesheet getButtonSpritesheet() {
    return buttonSprite;
  }

  public void setButtonSpritesheet(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public List<Consumer<Float>> getChangeConsumer() {
    return changeConsumer;
  }

  public float getCurrentValue() {
    return currentValue;
  }

  public void setCurrentValue(final float newValue) {
    this.currentValue = Math.clamp(newValue, getMinValue(), getMaxValue());
    getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
  }

  public float getTickSize() {
    return tickSize;
  }

  public void setTickSize(float tickSize) {
    this.tickSize = tickSize;
  }

  public int getSteps() {
    return (int) ((getMaxValue() - getMinValue()) / getStepSize()) + 1;
  }

  public float getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(final float maxValue) {
    this.maxValue = maxValue;
    updateSliderDimensions();
  }

  public float getMinValue() {
    return minValue;
  }

  public void setMinValue(final float minValue) {
    this.minValue = minValue;
    updateSliderDimensions();

  }

  public abstract Point2D getRelativeSliderLocation();

  protected abstract float getRelativeMouseValue();

  public ImageComponent getSliderComponent() {
    return sliderComponent;
  }

  protected void setSliderComponent(final ImageComponent slider) {
    this.sliderComponent = slider;
    getComponents().add(getSliderComponent());
  }

  protected abstract void updateSliderDimensions();

  @Override public void render(Graphics2D g) {
    g.setColor(getAppearance().getBackgroundColor2());
    renderBar(g);
    if (isShowingTicks()) {
      renderTicks(g);
    }
    super.render(g);
  }

  protected abstract void renderBar(Graphics2D g);

  protected abstract void renderTicks(Graphics2D g);

  public Spritesheet getSliderSpritesheet() {
    return sliderSprite;
  }

  public void setSliderSpritesheet(Spritesheet sliderSprite) {
    this.sliderSprite = sliderSprite;
  }

  public float getStepSize() {
    return stepSize;
  }

  public void setStepSize(final float stepSize) {
    this.stepSize = stepSize;
    updateSliderDimensions();
  }

  public boolean isDragging() {
    return isDragging;
  }

  public boolean isShowingTicks() {
    return showTicks;
  }

  public void setShowTicks(boolean showTicks) {
    this.showTicks = showTicks;
  }

  public void onChange(final Consumer<Float> c) {
    this.getChangeConsumer().add(c);
  }

  protected void inferValueFromMouseLocation() {
    float frac = (getMinValue() + getRelativeMouseValue() * getSteps()) / getSteps();
    int currentStep = (int) (frac * getSteps());
    setCurrentValue(currentStep * getStepSize());
  }

  @Override public void prepare() {
    super.prepare();
    getSliderComponent().setLocation(getRelativeSliderLocation());
  }
}
