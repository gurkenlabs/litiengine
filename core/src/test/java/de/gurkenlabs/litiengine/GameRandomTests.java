package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GameRandomTests {

  private GameRandom gameRandom;

  @BeforeEach
  public void initSeed() {
    gameRandom = new GameRandom();
    gameRandom.setSeed(1337);
  }

  @Test
  void testSampleArrayEmpty() {
    Integer[] array = new Integer[] {};
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.sample(array, 1, false));
  }

  @Test
  void testSampleCollectionEmpty() {
    List<Integer> list = Arrays.asList();
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.sample(list, 1, false));
  }

  @Test
  void testSampleCollectionReplacementFalse() {
    List<Integer> list = Arrays.asList(1, 2);
    assertEquals(Arrays.asList(1, 2), this.gameRandom.sample(list, 2, false));
  }

  @Test
  void testSampleCollectionReplacementTrue() {
    List<Integer> list = Arrays.asList(1, 2);
    assertEquals(Arrays.asList(2, 1), this.gameRandom.sample(list, 2, true));
  }

  @ParameterizedTest(name = "testNextInt partition={0} min={1} bound={2} expectedValue={3}")
  @CsvSource({"minEqualsBound, 42, 42, 42", "minSmallerThanBound, 40, 42, 41"})
  void testNextInt_partitions(String caption, int min, int bound, int expectedValue) {
    // act
    double actualValue = this.gameRandom.nextInt(min, bound);

    // assert
    assertEquals(expectedValue, actualValue);
  }

  @Test
  void testNextInt_minGreaterThanBound() {
    // arrange, act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextInt(42, 40));
  }

  @ParameterizedTest(name = "testNextFloat partition={0} min={1} bound={2} expectedValue={3}")
  @CsvSource({"minEqualsBound, 28.1, 28.1, 28.1", "minSmallerThanBound, 28.7, 30.5, 29.887875"})
  void testNextFloat_partitions(
      String caption, float min, float bound, float expectedValue) {
    // act
    float actualValue = this.gameRandom.nextFloat(min, bound);

    // assert
    assertEquals(expectedValue, actualValue, 0.000001);
  }

  @Test
  void testNextFloat_minGreaterThanBound() {
    // arrange, act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextFloat(28.9f, 27.1f));
  }

  @ParameterizedTest(
      name = "testNextFloat_withSingleArgument partition={0} bound={1} expectedValue={2}")
  @CsvSource({"bound == 0, 0, 0", "bound > 0, 4192910.168351, 2767026.2"})
  void testNextFloat_withSingleArgument(String caption, float bound, float expectedValue) {
    // act
    float actualValue = this.gameRandom.nextFloat(bound);

    // assert
    assertEquals(expectedValue, actualValue, 0.000001);
  }

  @Test
  void testNextFloat_withSingleArgumentBelowZero() {
    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextFloat(-100000));
  }

  @ParameterizedTest(name = "testNextLong partition={0} min={1} bound={2} expectedValue={3}")
  @CsvSource({
    "minEqualsBound, 128643212354, 128643212354, 128643212354",
    "minSmallerThanBound, 128643212354, 297536421382, -1716035126107589342"
  })
  void testNextLong_partitions(String caption, long min, long bound, long expectedValue) {
    // arrange
    long actualValue = this.gameRandom.nextLong(min, bound);

    // act, assert
    assertEquals(expectedValue, actualValue);
  }

  @Test
  void testNextLong_minGreaterThanBound() {
    // arrange, act, assert
    assertThrows(
        IllegalArgumentException.class,
        () -> this.gameRandom.nextLong(297536421382l, 297536421381l));
  }

  @Test
  void chooseStringArray_ThrowsOnNull() {
    // act, assert
    assertThrows(IllegalArgumentException.class, () -> gameRandom.choose((String[]) null));
  }

  @Test
  void chooseStringArray_ThrowsOnEmpty() {
    // arrange
    final String[] arr = new String[] {};

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> gameRandom.choose(arr));
  }

  @Test
  void chooseIntegerCollection_null() {
    // arrange
    Collection<Integer> collection = null;

    // act
    Integer result = this.gameRandom.choose(collection);

    // assert
    assertNull(result);
  }

  @Test
  void chooseIntegerCollection_empty() {
    // arrange
    Collection<Integer> collection = new ArrayList<Integer>();

    // act
    Integer result = this.gameRandom.choose(collection);

    // assert
    assertNull(result);
  }

  @Test
  void chooseIntegerArray_null() {
    // arrange
    Integer[] integers = null;

    // act
    Integer result = this.gameRandom.choose(integers);

    // assert
    assertNull(result);
  }

  @Test
  void chooseIntegerArray_empty() {
    // arrange
    Integer[] integers = new Integer[0];

    // act
    Integer result = this.gameRandom.choose(integers);

    // assert
    assertNull(result);
  }

  @Test
  void chooseIntArray_null() {
    // arrange
    int[] integers = null;

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(integers));
  }

  @Test
  void chooseIntArray_empty() {
    // arrange
    int[] integers = new int[0];

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(integers));
  }

  @Test
  void chooseLongArray_null() {
    // arrange
    long[] longs = null;

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(longs));
  }

  @Test
  void chooseLongArray_empty() {
    // arrange
    long[] longs = new long[0];

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(longs));
  }

  @Test
  void chooseDoubleArray_null() {
    // arrange
    double[] doubles = null;

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(doubles));
  }

  @Test
  void chooseDoubleArray_empty() {
    // arrange
    double[] doubles = new double[0];

    // act, assert
    assertThrows(IllegalArgumentException.class, () -> this.gameRandom.choose(doubles));
  }

  @Test
  void testNextColorNull() {
    GameRandom gameRandom = new GameRandom();
    assertNull(gameRandom.nextColor(null, 0.1f, 0.5f));
  }

  @ParameterizedTest
  @MethodSource("getColor")
  void testNextColor(Color color, float variance, float alpha, Color expected) {
    Color result = this.gameRandom.nextColor(color, variance, alpha);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> getColor() {
    return Stream.of(
        Arguments.of(Color.RED, 0.1f, 0.5f, new Color(238, 0, 0)),
        Arguments.of(Color.GREEN, 0.1f, 0.5f, Color.GREEN),
        Arguments.of(Color.BLUE, 0.1f, 0.5f, Color.BLUE),
        Arguments.of(Color.RED, 0f, 0f, Color.RED),
        Arguments.of(Color.GREEN, 0f, 0f, Color.GREEN),
        Arguments.of(Color.BLUE, 0f, 0f, Color.BLUE));
  }
}
