package de.gurkenlabs.litiengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameRandomTests {

    private GameRandom gameRandom;
    @BeforeEach
    public void initSeed(){
        gameRandom = new GameRandom();
        gameRandom.setSeed(1337);
    }

    @ParameterizedTest(name = "testNextInt partition={0} min={1} bound={2} expectedValue={3}")
    @CsvSource({
            "minEqualsBound, 42, 42, 42",
            "minSmallerThanBound, 40, 42, 41"
    })
    public void testNextInt_partitions(String caption, int min, int bound, int expectedValue){
        // act
        double actualValue = this.gameRandom.nextInt(min, bound);

        // assert
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testNextInt_minGreaterThanBound(){
        // arrange, act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextInt(42, 40));
    }

    @ParameterizedTest(name = "testNextFloat partition={0} min={1} bound={2} expectedValue={3}")
    @CsvSource({
            "minEqualsBound, 28.1, 28.1, 28.1",
            "minSmallerThanBound, 28.7, 30.5, 29.887875"
    })
    public void testNextFloat_partitions(String caption, float min, float bound, float expectedValue){
        // act
        float actualValue = this.gameRandom.nextFloat(min, bound);

        // assert
        assertEquals(expectedValue, actualValue, 0.000001);
    }

    @Test
    public void testNextFloat_minGreaterThanBound(){
        // arrange, act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextFloat(28.9f, 27.1f));
    }

    @ParameterizedTest(name = "testNextFloat_withSingleArgument partition={0} bound={1} expectedValue={2}")
    @CsvSource({
            "bound == 0, 0, 0",
            "bound > 0, 4192910.168351, 2767026.2"
    })
    public void testNextFloat_withSingleArgument(String caption, float bound, float expectedValue){
        // act
        float actualValue = this.gameRandom.nextFloat(bound);

        // assert
        assertEquals(expectedValue, actualValue, 0.000001);
    }

    @Test
    public void testNextFloat_withSingleArgumentBelowZero(){
        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextFloat(-100000));
    }

    @ParameterizedTest(name = "testNextLong partition={0} min={1} bound={2} expectedValue={3}")
    @CsvSource({
            "minEqualsBound, 128643212354, 128643212354, 128643212354",
            "minSmallerThanBound, 128643212354, 297536421382, -1716035126107589342"
    })
    public void testNextLong_partitions(String caption, long min, long bound, long expectedValue){
        // arrange
        long actualValue = this.gameRandom.nextLong(min, bound);

        // act, assert
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testNextLong_minGreaterThanBound(){
        // arrange, act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextLong(297536421382l, 297536421381l));
    }

    @Test
    public void chooseStringArray_ThrowsOnNull() {
        // act, assert
        assertThrows(IllegalArgumentException.class, () -> gameRandom.choose((String[]) null));
    }

    @Test
    public void chooseStringArray_ThrowsOnEmpty() {
        // arrange
        final String[] arr = new String[] {};

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> gameRandom.choose(arr));
    }

    @Test
    public void chooseIntegerCollection_null(){
        // arrange
        Collection<Integer> collection = null;

        // act
        Integer result = this.gameRandom.choose(collection);

        // assert
        assertNull(result);
    }

    @Test
    public void chooseIntegerCollection_empty(){
        // arrange
        Collection<Integer> collection = new ArrayList<Integer>();

        // act
        Integer result = this.gameRandom.choose(collection);

        // assert
        assertNull(result);
    }

    @Test
    public void chooseIntegerArray_null(){
        // arrange
        Integer[] integers = null;

        // act
        Integer result = this.gameRandom.choose(integers);

        // assert
        assertNull(result);

    }

    @Test
    public void chooseIntegerArray_empty(){
        // arrange
        Integer[] integers = new Integer[0];

        // act
        Integer result = this.gameRandom.choose(integers);

        // assert
        assertNull(result);
    }

    @Test
    public void chooseIntArray_null(){
        // arrange
        int[] integers = null;

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(integers));
    }

    @Test
    public void chooseIntArray_empty(){
        // arrange
        int[] integers = new int[0];

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(integers));
    }

    @Test
    public void chooseLongArray_null(){
        // arrange
        long[] longs = null;

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(longs));
    }

    @Test
    public void chooseLongArray_empty(){
        // arrange
        long[] longs = new long[0];

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(longs));
    }

    @Test
    public void chooseDoubleArray_null(){
        // arrange
        double[] doubles = null;

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(doubles));
    }

    @Test
    public void chooseDoubleArray_empty(){
        // arrange
        double[] doubles = new double[0];

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(doubles));
    }

}
