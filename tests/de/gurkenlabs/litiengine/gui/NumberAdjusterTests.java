package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;

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

        NumberAdjuster number = new NumberAdjuster(0,0,150,300,0,400,1,4);

        BigDecimal lowerBound = number.getLowerBound();
        BigDecimal currentValue = new BigDecimal("-456.0");
        BigDecimal currentValue2 = new BigDecimal("0");
        BigDecimal currentValue3 = new BigDecimal("456.0");

        number.setLowerBound(currentValue);
        assertFalse( lowerBound.compareTo(currentValue) < 0);

        number.setLowerBound(currentValue2);
        assertTrue( lowerBound.compareTo(currentValue2) == 0);

        number.setLowerBound(currentValue3);
        assertTrue( lowerBound.compareTo(currentValue3) < 0);

    }

}