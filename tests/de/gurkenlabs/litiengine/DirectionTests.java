package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mockStatic;

public class DirectionTests {
    @Test
    public void fromAngle_Down() {
        // assert
        assertEquals(Direction.DOWN, Direction.fromAngle(0.0));
        assertEquals(Direction.DOWN, Direction.fromAngle(0.1));
        assertEquals(Direction.DOWN, Direction.fromAngle(44.9));

        assertEquals(Direction.DOWN, Direction.fromAngle(315.0));
        assertEquals(Direction.DOWN, Direction.fromAngle(315.1));
        assertEquals(Direction.DOWN, Direction.fromAngle(359.9));
        assertEquals(Direction.DOWN, Direction.fromAngle(360.0));
    }

    @Test
    public void fromAngle_Right() {
        // assert
        assertEquals(Direction.RIGHT, Direction.fromAngle(45.0));
        assertEquals(Direction.RIGHT, Direction.fromAngle(45.1));

        assertEquals(Direction.RIGHT, Direction.fromAngle(134.9));
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

    @Test
    public void testGetOpposite_ofLeft(){
        // arrange
        Direction left = Direction.LEFT;

        // act
        Direction opposite = left.getOpposite();

        // assert
        assertEquals(Direction.RIGHT, opposite);
    }

    @Test
    public void testGetOpposite_ofRight(){
        // arrange
        Direction right = Direction.RIGHT;

        // act
        Direction opposite = right.getOpposite();

        // assert
        assertEquals(Direction.LEFT, opposite);
    }

    @Test
    public void testGetOpposite_ofUp(){
        // arrange
        Direction up = Direction.UP;

        // act
        Direction opposite = up.getOpposite();

        // assert
        assertEquals(Direction.DOWN, opposite);
    }

    @Test
    public void testGetOpposite_ofDown(){
        // arrange
        Direction down = Direction.DOWN;

        // act
        Direction opposite = down.getOpposite();

        // assert
        assertEquals(Direction.UP, opposite);
    }

    @Test
    public void testGetOpposite_ofUndefined(){
        // arrange
        Direction undefined = Direction.UNDEFINED;

        // act
        Direction opposite = undefined.getOpposite();

        // assert
        assertEquals(Direction.UNDEFINED, opposite);
    }
}
