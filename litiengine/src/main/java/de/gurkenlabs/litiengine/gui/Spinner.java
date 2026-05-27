package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.input.Input;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A numeric text-field component with attached up/down arrow buttons that adjust the current value by a configurable
 * {@linkplain #getStepSize() step size} within a bounded range. The value can also be edited directly via the text field or adjusted using the
 * up/down arrow keys.
 */
public class Spinner extends TextFieldComponent {
  /**
   * Down-arrow icon shown on the decrement button.
   */
  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE84A");
  /** Up-arrow icon shown on the increment button. */
  public static final FontIcon ARROW_UP = new FontIcon(ICON_FONT, "\uE84B");
  private static final Logger log = Logger.getLogger(Spinner.class.getName());
  private BigDecimal step;
  private BigDecimal lowerBound;
  private BigDecimal upperBound;
  private BigDecimal currentValue;
  private final List<Consumer<BigDecimal>> valueChangeConsumers;

  /**
   * Constructs a new spinner.
   *
   * @param x          the x coordinate of the component
   * @param y          the y coordinate of the component
   * @param width      the width of the component
   * @param height     the height of the component
   * @param lowerBound the lowest allowed value
   * @param upperBound the highest allowed value
   * @param startValue the initial value
   * @param stepSize   the step size between adjacent values
   */
  public Spinner(
      final double x,
      final double y,
      final double width,
      final double height,
      final double lowerBound,
      final double upperBound,
      final double startValue,
      final double stepSize) {
    super(x, y, width, height, Double.toString(startValue));
    this.valueChangeConsumers = new CopyOnWriteArrayList<>();
    this.lowerBound = BigDecimal.valueOf(lowerBound);
    this.upperBound = BigDecimal.valueOf(upperBound);
    this.setCurrentValue(BigDecimal.valueOf(startValue));
    this.step = BigDecimal.valueOf(stepSize);
    this.setFormat(DOUBLE_FORMAT);
    Input.keyboard().onKeyTyped(KeyEvent.VK_UP, e -> this.handleAdjustmentInput(true));
    Input.keyboard().onKeyTyped(KeyEvent.VK_DOWN, e -> this.handleAdjustmentInput(false));
  }

  /**
   * Decreases the current value by one {@linkplain #getStepSize() step}, clamping to the lower bound.
   */
  public void decrement() {
    this.setCurrentValue(this.getCurrentValue().subtract(this.getStepSize()));
  }

  /**
   * Gets the current value of the spinner.
   *
   * @return the current value
   */
  public BigDecimal getCurrentValue() {
    return this.currentValue;
  }

  /**
   * Gets the lowest allowed value of the spinner.
   *
   * @return the lower bound
   */
  public BigDecimal getLowerBound() {
    return this.lowerBound;
  }

  /**
   * Gets the step size between adjacent spinner values.
   *
   * @return the step size
   */
  public BigDecimal getStepSize() {
    return this.step;
  }

  /**
   * Gets the highest allowed value of the spinner.
   *
   * @return the upper bound
   */
  public BigDecimal getUpperBound() {
    return this.upperBound;
  }

  /**
   * Increases the current value by one {@linkplain #getStepSize() step}, clamping to the upper bound.
   */
  public void increment() {
    this.setCurrentValue(this.getCurrentValue().add(this.getStepSize()));
  }

  /**
   * Registers a callback invoked whenever the spinner value changes.
   *
   * @param cons the change consumer
   */
  public void onValueChange(final Consumer<BigDecimal> cons) {
    this.valueChangeConsumers.add(cons);
  }

  @Override
  public void prepare() {
    ImageComponent buttonUp =
        new ImageComponent(
            this.getX() + this.getWidth(),
            this.getY(),
            this.getHeight() / 2,
            this.getHeight() / 2,
            ARROW_UP.getText());
    ImageComponent buttonDown =
        new ImageComponent(
            this.getX() + this.getWidth(),
            this.getY() + this.getHeight() / 2,
            this.getHeight() / 2,
            this.getHeight() / 2,
            ARROW_DOWN.getText());
    buttonUp.setFont(ARROW_UP.getFont());
    buttonDown.setFont(ARROW_UP.getFont());

    this.getComponents().add(buttonUp);
    this.getComponents().add(buttonDown);
    super.prepare();
    buttonUp.onClicked(
        c -> {
          this.setSelected(true);
          this.increment();
        });
    buttonDown.onClicked(
        c -> {
          this.setSelected(true);
          this.decrement();
        });
    this.onChangeConfirmed(
        e -> {
          try {
            this.setCurrentValue(BigDecimal.valueOf(Double.parseDouble(this.getText())));
          } catch (final Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
          }
        });
  }

  /**
   * Sets the current value, clamped to the configured range, and notifies registered change listeners.
   *
   * @param newValue the new value
   */
  public void setCurrentValue(final BigDecimal newValue) {
    if (newValue.compareTo(this.getUpperBound()) > 0) {
      this.currentValue = this.getUpperBound();
    } else if (newValue.compareTo(this.getLowerBound()) < 0) {
      this.currentValue = this.getLowerBound();
    } else {
      this.currentValue = newValue;
    }
    this.setText(this.getCurrentValue() + "");
    this.valueChangeConsumers.forEach(c -> c.accept(this.getCurrentValue()));
  }

  /**
   * Sets the lowest allowed value. If the current value is below the new bound, the current value is clamped to the new bound.
   *
   * @param lowerBound the new lower bound
   */
  public void setLowerBound(final BigDecimal lowerBound) {
    this.lowerBound = lowerBound;
    if (this.getCurrentValue().compareTo(this.getLowerBound()) < 0) {
      this.setCurrentValue(this.getLowerBound());
    }
  }

  /**
   * Sets the step size between adjacent spinner values.
   *
   * @param stepSize the step size
   */
  public void setStepSize(final BigDecimal stepSize) {
    this.step = stepSize;
  }

  /**
   * Sets the highest allowed value. If the current value is above the new bound, the current value is clamped to the new bound.
   *
   * @param upperBound the new upper bound
   */
  public void setUpperBound(final BigDecimal upperBound) {
    this.upperBound = upperBound;
    if (this.getCurrentValue().compareTo(this.getUpperBound()) > 0) {
      this.setCurrentValue(this.getUpperBound());
    }
  }

  private void handleAdjustmentInput(final boolean increment) {
    if (!this.canHandleInput()) {
      return;
    }

    if (increment) {
      this.increment();
    } else {
      this.decrement();
    }
  }
}
