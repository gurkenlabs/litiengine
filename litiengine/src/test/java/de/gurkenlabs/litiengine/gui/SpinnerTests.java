package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
public class SpinnerTests {

  @BeforeAll
  public static void initialize() {
    // init required Game environment
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // init Keyboard
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

  @Test
  void testSetCurrentValue() {
    Spinner number = new Spinner(0, 0, 150, 300, 0, 400, 1, 4);

    BigDecimal newValue = new BigDecimal("123456.0");
    BigDecimal newValueNeg = new BigDecimal("-123456.0");

    BigDecimal upperBound = number.getUpperBound();
    BigDecimal lowerBound = number.getLowerBound();

    number.setCurrentValue(newValue);
    number.setCurrentValue(newValueNeg);

    assertNotEquals(newValueNeg, upperBound);
    assertNotEquals(newValue, lowerBound);
  }

  @Test
  void testSetLowerBound() {
    // arrange
    Spinner number = new Spinner(0, 0, 150, 300, 0, 400, 1, 4);
    BigDecimal currentValue1 = new BigDecimal("-456.0");
    BigDecimal currentValue2 = new BigDecimal("0");
    BigDecimal currentValue3 = new BigDecimal("456.0");

    // act, assert
    number.setLowerBound(currentValue1);
    assertEquals(currentValue1, number.getLowerBound());

    number.setLowerBound(currentValue2);
    assertEquals(currentValue2, number.getLowerBound());

    number.setLowerBound(currentValue3);
    assertEquals(currentValue3, number.getLowerBound());
  }

  @Test
  void testSetUpperBound() {
    // arrange
    Spinner number = new Spinner(0, 0, 150, 300, 0, 400, 1, 4);
    BigDecimal currentValue1 = new BigDecimal("456.0");
    BigDecimal currentValue2 = new BigDecimal("0");
    BigDecimal currentValue3 = new BigDecimal("-456.0");

    // act, assert
    number.setUpperBound(currentValue1);
    assertEquals(currentValue1, number.getUpperBound());

    number.setUpperBound(currentValue2);
    assertEquals(currentValue2, number.getUpperBound());

    number.setUpperBound(currentValue3);
    assertEquals(currentValue3, number.getUpperBound());
  }
}
