package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NumberAdjusterTests {

    @BeforeAll
    public static void initialize() {
        // init required Game environment
        Game.init(Game.COMMADLINE_ARG_NOGUI);

        // init Keyboard
        Input.InputGameAdapter adapter = new Input.InputGameAdapter();
        adapter.initialized();
    }



    @Test
    public void testSetCurrentValue() {
        NumberAdjuster number = new NumberAdjuster(0,0,150,300,0,400,1,4);

        BigDecimal newValue = new BigDecimal("123456.0");
        BigDecimal newValueNeg = new BigDecimal("-123456.0");

        BigDecimal upperBound = number.getUpperBound();
        BigDecimal lowerBound = number.getLowerBound();

        number.setCurrentValue(newValue);
        number.setCurrentValue(newValueNeg);

        assertNotEquals(newValueNeg,upperBound);
        assertNotEquals(newValue,lowerBound);
    }

    @Test
    public void testSetLowerBound(){
        // arrange
        NumberAdjuster number = new NumberAdjuster(0,0,150,300,0,400,1,4);
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
}