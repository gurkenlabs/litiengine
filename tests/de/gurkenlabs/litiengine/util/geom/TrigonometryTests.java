package de.gurkenlabs.litiengine.util.geom;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrigonometryTests {

    @ParameterizedTest(name = "{0}: ({1}, {2}) = {3}")
    @CsvSource({
            "'On-point y=0', 0f, 1f, 0",
            "'On-point x=0', 1f, 0f, 1.5707964f",
            "'On-point y=0 x=0', 0f, 0f, 0f",
            "'Off-point y negative x positive', -0.0000001f, 0.0000001f, -0.7853982f",
            "'Off-point y positive x negative', 0.0000001f, -0.0000001f, 2.3561945f",
            "'Off-point y positive x positive', 0.0000001f, 0.0000001f, 0.7853982f",
            "'Off-point y negative x negative', -0.0000001f, -0.0000001f, -2.3561945f",
    })
    public void testATan2AtBoundaries(String boundary, float y, float x, float expectedResult){
        assertEquals(expectedResult, Trigonometry.atan2(y, x));
    }
}
