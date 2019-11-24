package de.gurkenlabs.litiengine.gui;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NumberAdjuster extends TextFieldComponent {
  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE84A");
  public static final FontIcon ARROW_UP = new FontIcon(ICON_FONT, "\uE84B");
  private static final Logger log = Logger.getLogger(NumberAdjuster.class.getName());
  private BigDecimal step;
  private BigDecimal lowerBound;
  private BigDecimal upperBound;
  private BigDecimal currentValue;
  private final List<Consumer<BigDecimal>> valueChangeConsumers;

  public NumberAdjuster(final double x, final double y, final double width, final double height,  final double lowerBound, final double upperBound, final double startValue, final double stepSize) {
    super(x, y, width, height, Double.toString(startValue));
    this.valueChangeConsumers = new CopyOnWriteArrayList<>();
    this.lowerBound = BigDecimal.valueOf(lowerBound);
    this.upperBound = BigDecimal.valueOf(upperBound);
    this.setCurrentValue(BigDecimal.valueOf(startValue));
    this.step = BigDecimal.valueOf(stepSize);
    this.setFormat(DOUBLE_FORMAT);
  }

  public void decrement() {
    this.setCurrentValue(this.getCurrentValue().subtract(this.getStepSize()));
  }

  public BigDecimal getCurrentValue() {
    return this.currentValue;
  }

  public BigDecimal getLowerBound() {
    return this.lowerBound;
  }

  public BigDecimal getStepSize() {
    return this.step;
  }

  public BigDecimal getUpperBound() {
    return this.upperBound;
  }

  public void increment() {
    this.setCurrentValue(this.getCurrentValue().add(this.getStepSize()));
  }

  public void onValueChange(final Consumer<BigDecimal> cons) {
    this.valueChangeConsumers.add(cons);
  }

  @Override
  public void prepare() {
    ImageComponent buttonUp = new ImageComponent(this.getX() + this.getWidth(), this.getY(), this.getHeight() / 2, this.getHeight() / 2,  ARROW_UP.getText());
    ImageComponent buttonDown = new ImageComponent(this.getX() + this.getWidth(), this.getY() + this.getHeight() / 2, this.getHeight() / 2, this.getHeight() / 2, ARROW_DOWN.getText());
    buttonUp.setFont(ARROW_UP.getFont());
    buttonDown.setFont(ARROW_UP.getFont());

    this.getComponents().add(buttonUp);
    this.getComponents().add(buttonDown);
    super.prepare();
    buttonUp.onClicked(c -> this.increment());
    buttonDown.onClicked(c -> this.decrement());
    this.onChangeConfirmed(e -> {
      try {
        this.setCurrentValue(BigDecimal.valueOf(Double.parseDouble(this.getText())));
      } catch (final Exception ex) {
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
    });
  }

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

  public void setLowerBound(final BigDecimal lowerBound) {
    this.lowerBound = lowerBound;
    if (this.getCurrentValue().compareTo(this.getLowerBound()) < 0) {
      this.setCurrentValue(this.getLowerBound());
    }
  }

  public void setStepSize(final BigDecimal stepSize) {
    this.step = stepSize;
  }

  public void setUpperBound(final BigDecimal upperBound) {
    this.upperBound = upperBound;
    if (this.getCurrentValue().compareTo(this.getUpperBound()) > 0) {
      this.setCurrentValue(this.getUpperBound());
    }
  }
}
