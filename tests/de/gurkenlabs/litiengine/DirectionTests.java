package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.stream.Stream;

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

    @ParameterizedTest(name="fromAngle_Up angle={0}")
    @CsvSource({
            "135.0d", "135.1d", "224.9d"
    })
    public void fromAngle_Up(double angle) {
        // assert
        assertEquals(Direction.UP, Direction.fromAngle(angle));
    }

    @ParameterizedTest(name="fromAngle_Left angle={0}")
    @CsvSource({
            "225.0d", "225.1d", "314.9d"
    })
    public void fromAngle_Left(double angle) {
        // assert
        assertEquals(Direction.LEFT, Direction.fromAngle(angle));
    }

    @ParameterizedTest(name="fromAngle_UsuallyNoUndefined angle={0}")
    @CsvSource({
            "-360.1d", "-360.0d", "-359.9d", "-0.1", "360.1", "719.9", "720.0", "720.1"
    })
    public void fromAngle_UsuallyNoUndefined(double angle) {
        // assert
        assertEquals(Direction.DOWN, Direction.fromAngle(angle));
    }

    @ParameterizedTest(name="fromAngle_ForceUndefined value={0}, angle={1}")
    @CsvSource({
            "-0.1d, 10.0d", "360.1d, 10.0d"
    })
    public void fromAngle_ForceUndefined(double value, double angle ) {
        // arrange
        MockedStatic<GeometricUtilities> geomUtilsMockStatic = mockStatic(GeometricUtilities.class);

        // assert
        geomUtilsMockStatic.when(() -> GeometricUtilities.normalizeAngle(anyDouble())).thenReturn(value);
        assertEquals(Direction.UNDEFINED, Direction.fromAngle(angle));

        // cleanup
        geomUtilsMockStatic.close();
    }

    @ParameterizedTest
    @MethodSource("getOppositeParameters")
    public void testGetOpposite(Direction initialDirection, Direction expectedOppositeDirection){
        // act
        Direction actualOpposite = initialDirection.getOpposite();

        // assert
        assertEquals(expectedOppositeDirection, actualOpposite);
    }

    private static Stream<Arguments> getOppositeParameters(){
        // arrange
        return Stream.of(
                Arguments.of(Direction.LEFT, Direction.RIGHT),
                Arguments.of(Direction.RIGHT, Direction.LEFT),
                Arguments.of(Direction.UP, Direction.DOWN),
                Arguments.of(Direction.DOWN, Direction.UP),
                Arguments.of(Direction.UNDEFINED, Direction.UNDEFINED)
        );
    }
}
