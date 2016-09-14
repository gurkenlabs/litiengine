package de.gurkenlabs.litiengine.gui;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class NumberAdjuster extends TextFieldComponent {
  private ImageComponent button1, button2;
  private Spritesheet buttonSprite;
  public static Icon ARROW_UP = new Icon(FontLoader.getIconFontThree(), "\uE84B");
  public static Icon ARROW_DOWN = new Icon(FontLoader.getIconFontThree(), "\uE84A");
  BigDecimal step, lowerBound, upperBound, currentValue;
  private final List<Consumer<BigDecimal>> valueChangeConsumers;

  public NumberAdjuster(double x, double y, double width, double height, Spritesheet textBackground, Spritesheet buttonBackground, double lowerBound, double upperBound, double startValue, double stepSize) {
    super(x, y, width, height, textBackground, startValue + "");
    this.buttonSprite = buttonBackground;
    this.valueChangeConsumers = new CopyOnWriteArrayList<>();
    this.lowerBound = BigDecimal.valueOf(lowerBound);
    this.upperBound = BigDecimal.valueOf(upperBound);
    this.setCurrentValue(BigDecimal.valueOf(startValue));
    this.step = BigDecimal.valueOf(stepSize);
    this.setFormat(DOUBLE_FORMAT);

  }

  @Override
  protected void initializeComponents() {

  }

  public Spritesheet getButtonSprite() {
    return buttonSprite;
  }

  public void setButtonSprite(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  @Override
  public void prepare() {
    this.button1 = new ImageComponent(this.getX() + this.getWidth(), this.getY(), this.getHeight() / 2, this.getHeight() / 2, this.getButtonSprite(), ARROW_UP.getText(), null);
    this.button2 = new ImageComponent(this.getX() + this.getWidth(), this.getY() + this.getHeight() / 2, this.getHeight() / 2, this.getHeight() / 2, this.getButtonSprite(), ARROW_DOWN.getText(), null);
    this.button1.setFont(ARROW_UP.getFont());
    this.button2.setFont(ARROW_UP.getFont());

    this.getComponents().add(button1);
    this.getComponents().add(button2);
    super.prepare();
    this.button1.onClicked(c -> {
      if (this.getCurrentValue().compareTo(this.getUpperBound().subtract(this.getStepSize())) <= 0) {
        this.setCurrentValue(this.getCurrentValue().add(this.getStepSize()));
      } else {
        this.setCurrentValue(this.getUpperBound());
      }

    });
    this.button2.onClicked(c -> {
      if (this.getCurrentValue().compareTo(this.getLowerBound().add(this.getStepSize())) >= 0) {
        this.setCurrentValue(this.getCurrentValue().subtract(this.getStepSize()));
      } else {
        this.setCurrentValue(this.getLowerBound());
      }
    });
    this.onChangeConfirmed(e -> {
      try {
        this.setCurrentValue(BigDecimal.valueOf(Double.parseDouble(this.getText())));
      } catch (Exception ex) {
        System.out.println("only numerical values allowed!");
      }
    });
  }

  public BigDecimal getLowerBound() {
    return this.lowerBound;
  }

  public void setLowerBound(BigDecimal lowerBound) {
    this.lowerBound = lowerBound;
  }

  public BigDecimal getUpperBound() {
    return this.upperBound;
  }

  public BigDecimal getStepSize() {
    return this.step;
  }

  public void setStepSize(BigDecimal stepSize) {
    this.step = stepSize;
  }

  public void setUpperBound(BigDecimal upperBound) {
    this.upperBound = upperBound;
  }

  public BigDecimal getCurrentValue() {
    return this.currentValue;
  }

  public void setCurrentValue(BigDecimal newValue) {
    if (newValue.compareTo(this.getUpperBound()) <= 0 && newValue.compareTo(this.getLowerBound()) >= 0) {
      this.currentValue = newValue;
      this.setText(this.getCurrentValue() + "");
      this.valueChangeConsumers.forEach(c -> c.accept(this.getCurrentValue()));

    }
  }

  public void onValueChange(Consumer<BigDecimal> cons) {
    this.valueChangeConsumers.add(cons);
  }
}
