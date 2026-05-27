package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Abstract base class for GUI slider components that allow a user to pick a numerical value within a configurable range and step size.
 * <p>
 * Subclasses provide the concrete orientation (e.g. horizontal / vertical) and implement the slider geometry through {@link #renderBar(Graphics2D)},
 * {@link #renderTicks(Graphics2D)} and the location/mouse mapping helpers.
 * </p>
 */
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


  /**
   * Constructs a new slider with the given bounds and value range.
   *
   * @param x        the x position of the slider
   * @param y        the y position of the slider
   * @param width    the width of the slider
   * @param height   the height of the slider
   * @param minValue the minimum selectable value
   * @param maxValue the maximum selectable value
   * @param stepSize the step size between adjacent selectable values
   */
  protected Slider(final double x, final double y, final double width, final double height, final float minValue, final float maxValue,
    final float stepSize) {
    super(x, y, width, height);
    setFocusable(true);
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
      setSelected(true);
      inferValueFromMouseLocation();
      getSliderComponent().setLocation(getRelativeSliderLocation());
    });

    onMouseDragged(e -> {
      inferValueFromMouseLocation();
      getSliderComponent().setLocation(getRelativeSliderLocation());
    });

    Input.keyboard().onKeyTyped(this::handleTypedKey);

  }

  /**
   * Gets the first arrow button (e.g. decrease).
   *
   * @return the first button component
   */
  public ImageComponent getButton1() {
    return button1;
  }

  /**
   * Sets the first arrow button (typically used to decrease the slider value).
   *
   * @param button1 the button component to register
   */
  protected void setButton1(final ImageComponent button1) {
    this.button1 = button1;
    this.button1.onClicked(e -> {
      setSelected(true);
      this.setCurrentValue(getCurrentValue() - getStepSize());
      getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
    });
    this.getComponents().add(this.getButton1());
  }

  /**
   * Gets the second arrow button (e.g. increase).
   *
   * @return the second button component
   */
  public ImageComponent getButton2() {
    return button2;
  }

  /**
   * Sets the second arrow button (typically used to increase the slider value).
   *
   * @param button2 the button component to register
   */
  protected void setButton2(final ImageComponent button2) {
    this.button2 = button2;
    this.button2.onClicked(e -> {
      setSelected(true);
      this.setCurrentValue(getCurrentValue() + getStepSize());
      getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
    });
    getComponents().add(getButton2());
  }

  /**
   * Gets the spritesheet used to render the arrow buttons.
   *
   * @return the button spritesheet
   */
  public Spritesheet getButtonSpritesheet() {
    return buttonSprite;
  }

  /**
   * Sets the spritesheet used to render the arrow buttons.
   *
   * @param buttonSprite the spritesheet to use
   */
  public void setButtonSpritesheet(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  /**
   * Gets the list of registered change listeners.
   *
   * @return the change listener list
   */
  public List<Consumer<Float>> getChangeConsumer() {
    return changeConsumer;
  }

  /**
   * Gets the currently selected value.
   *
   * @return the current value
   */
  public float getCurrentValue() {
    return currentValue;
  }

  /**
   * Sets the currently selected value. The value is clamped to the configured min/max range and registered change listeners are notified.
   *
   * @param newValue the new value
   */
  public void setCurrentValue(final float newValue) {
    this.currentValue = Math.clamp(newValue, getMinValue(), getMaxValue());
    getChangeConsumer().forEach(consumer -> consumer.accept(getCurrentValue()));
  }

  /**
   * Gets the relative size factor used when rendering the slider's tick marks.
   *
   * @return the tick size factor
   */
  public float getTickSize() {
    return tickSize;
  }

  /**
   * Sets the relative size factor used when rendering the slider's tick marks.
   *
   * @param tickSize the tick size factor
   */
  public void setTickSize(float tickSize) {
    this.tickSize = tickSize;
  }

  /**
   * Computes the number of discrete steps between {@link #getMinValue()} and {@link #getMaxValue()} given the current step size.
   *
   * @return the number of selectable steps
   */
  public int getSteps() {
    return (int) ((getMaxValue() - getMinValue()) / getStepSize()) + 1;
  }

  /**
   * Gets the maximum selectable value.
   *
   * @return the maximum value
   */
  public float getMaxValue() {
    return maxValue;
  }

  /**
   * Sets the maximum selectable value and refreshes the slider dimensions.
   *
   * @param maxValue the maximum value
   */
  public void setMaxValue(final float maxValue) {
    this.maxValue = maxValue;
    updateSliderDimensions();
  }

  /**
   * Gets the minimum selectable value.
   *
   * @return the minimum value
   */
  public float getMinValue() {
    return minValue;
  }

  /**
   * Sets the minimum selectable value and refreshes the slider dimensions.
   *
   * @param minValue the minimum value
   */
  public void setMinValue(final float minValue) {
    this.minValue = minValue;
    updateSliderDimensions();

  }

  /**
   * Computes the relative location of the slider thumb within the slider for the current value.
   *
   * @return the relative location of the slider thumb
   */
  public abstract Point2D getRelativeSliderLocation();

  /**
   * Computes a normalized value (typically in {@code [0, 1]}) corresponding to the current mouse location relative to the slider.
   *
   * @return the relative mouse value
   */
  protected abstract float getRelativeMouseValue();

  /**
   * Gets the slider thumb component.
   *
   * @return the slider thumb component
   */
  public ImageComponent getSliderComponent() {
    return sliderComponent;
  }

  /**
   * Sets the slider thumb component.
   *
   * @param slider the slider thumb component to register
   */
  protected void setSliderComponent(final ImageComponent slider) {
    this.sliderComponent = slider;
    getComponents().add(getSliderComponent());
  }

  /**
   * Updates the geometry of the slider in response to a change in min/max value or step size.
   */
  protected abstract void updateSliderDimensions();

  @Override public void render(Graphics2D g) {
    g.setColor(getAppearance().getBackgroundColor2());
    renderBar(g);
    if (isShowingTicks()) {
      renderTicks(g);
    }
    super.render(g);
  }

  /**
   * Renders the slider bar (track).
   *
   * @param g the graphics context to draw to
   */
  protected abstract void renderBar(Graphics2D g);

  /**
   * Renders the slider tick marks.
   *
   * @param g the graphics context to draw to
   */
  protected abstract void renderTicks(Graphics2D g);

  /**
   * Determines whether the slider value should be increased when the given key is typed.
   *
   * @param keyCode the typed key code
   * @return {@code true} if the value should be increased
   */
  protected boolean shouldIncreaseOnKey(final int keyCode) {
    return false;
  }

  /**
   * Determines whether the slider value should be decreased when the given key is typed.
   *
   * @param keyCode the typed key code
   * @return {@code true} if the value should be decreased
   */
  protected boolean shouldDecreaseOnKey(final int keyCode) {
    return false;
  }

  /**
   * Gets the spritesheet used to render the slider thumb.
   *
   * @return the slider spritesheet
   */
  public Spritesheet getSliderSpritesheet() {
    return sliderSprite;
  }

  /**
   * Sets the spritesheet used to render the slider thumb.
   *
   * @param sliderSprite the spritesheet to use
   */
  public void setSliderSpritesheet(Spritesheet sliderSprite) {
    this.sliderSprite = sliderSprite;
  }

  /**
   * Gets the step size between adjacent selectable values.
   *
   * @return the step size
   */
  public float getStepSize() {
    return stepSize;
  }

  /**
   * Sets the step size between adjacent selectable values and refreshes the slider dimensions.
   *
   * @param stepSize the step size
   */
  public void setStepSize(final float stepSize) {
    this.stepSize = stepSize;
    updateSliderDimensions();
  }

  /**
   * Returns whether the slider is currently being dragged by the user.
   *
   * @return {@code true} if the slider thumb is being dragged
   */
  public boolean isDragging() {
    return isDragging;
  }

  /**
   * Returns whether the slider is rendering tick marks.
   *
   * @return {@code true} if tick marks are shown
   */
  public boolean isShowingTicks() {
    return showTicks;
  }

  /**
   * Sets whether the slider should render tick marks.
   *
   * @param showTicks {@code true} to show tick marks
   */
  public void setShowTicks(boolean showTicks) {
    this.showTicks = showTicks;
  }

  /**
   * Registers a callback that is invoked whenever the slider value changes.
   *
   * @param c the change consumer to register
   */
  public void onChange(final Consumer<Float> c) {
    this.getChangeConsumer().add(c);
  }

  /**
   * Infers the current value from the current mouse location and updates the slider accordingly.
   */
  protected void inferValueFromMouseLocation() {
    float frac = (getMinValue() + getRelativeMouseValue() * getSteps()) / getSteps();
    int currentStep = (int) (frac * getSteps());
    setCurrentValue(currentStep * getStepSize());
  }

  private boolean canHandleKeyboardInput() {
    return !isSuspended() && isVisible() && isEnabled() && hasInputFocus();
  }

  private void handleTypedKey(final KeyEvent event) {
    if (!canHandleKeyboardInput()) {
      return;
    }

    if (shouldDecreaseOnKey(event.getKeyCode())) {
      setCurrentValue(getCurrentValue() - getStepSize());
      return;
    }

    if (shouldIncreaseOnKey(event.getKeyCode())) {
      setCurrentValue(getCurrentValue() + getStepSize());
    }
  }

  @Override public void prepare() {
    super.prepare();
    getSliderComponent().setLocation(getRelativeSliderLocation());
  }
}
