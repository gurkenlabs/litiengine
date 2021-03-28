package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mockStatic;

public class DirectionTests {

    @ParameterizedTest(name="fromAngle_Down angle={0}")
    @CsvSource({
            "0.0d", "0.1d", "44.9d", "315.0d", "315.1d", "359.9d", "360.0d"
    })
    public void fromAngle_Down(double angle) {
        // assert
        assertEquals(Direction.DOWN, Direction.fromAngle(angle));
    }

    @ParameterizedTest(name="fromAngle_Right angle={0}")
    @CsvSource({
            "45.0d", "45.1d", "134.9d"
    })
    public void fromAngle_Right(double angle) {
        // assert
        assertEquals(Direction.RIGHT, Direction.fromAngle(angle));
    }

    @Test
    public void fromAngle_Up() {
        // assert
        assertEquals(Direction.UP, Direction.fromAngle(135.0));
        assertEquals(Direction.UP, Direction.fromAngle(135.1));

        assertEquals(Direction.UP, Direction.fromAngle(224.9));
    }

    @Test
    public void fromAngle_Left() {
        // assert
        assertEquals(Direction.LEFT, Direction.fromAngle(225.0));
        assertEquals(Direction.LEFT, Direction.fromAngle(225.1));

        assertEquals(Direction.LEFT, Direction.fromAngle(314.9));
    }

    @Test
    public void fromAngle_UsuallyNoUndefined() {
        // assert
        assertEquals(Direction.DOWN, Direction.fromAngle(-360.1));
        assertEquals(Direction.DOWN, Direction.fromAngle(-360.0));
        assertEquals(Direction.DOWN, Direction.fromAngle(-359.9));

        assertEquals(Direction.DOWN, Direction.fromAngle(-0.1));
        assertEquals(Direction.DOWN, Direction.fromAngle(360.1));

        assertEquals(Direction.DOWN, Direction.fromAngle(719.9));
        assertEquals(Direction.DOWN, Direction.fromAngle(720.0));
        assertEquals(Direction.DOWN, Direction.fromAngle(720.1));
    }

    @Test
    public void fromAngle_ForceUndefined() {
        // arrange
        MockedStatic<GeometricUtilities> geomUtilsMockStatic = mockStatic(GeometricUtilities.class);

        // assert
        geomUtilsMockStatic.when(() -> GeometricUtilities.normalizeAngle(anyDouble())).thenReturn(-0.1);
        assertEquals(Direction.UNDEFINED, Direction.fromAngle(10.0));

        geomUtilsMockStatic.when(() -> GeometricUtilities.normalizeAngle(anyDouble())).thenReturn(360.1);
        assertEquals(Direction.UNDEFINED, Direction.fromAngle(10.0));

        // cleanup
        geomUtilsMockStatic.close();
    }
}
