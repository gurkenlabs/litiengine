package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * This is an updated version with enhancements made by Daniel Migowski, Andre Bogus, and David
 * Koelle. Updated by David Koelle in 2017.
 */
class AlphanumComparatorTests implements Comparator<String> {

  @Override
  public int compare(String o1, String o2) {
    return 0;
  }

  @Test
  void testCompareTo_NULL() {
    String s1 = null;
    String s2 = null;

    assertEquals(0, AlphanumComparator.compareTo(s1, s2));
  }

  @ParameterizedTest(name = "testCompareTo_EmptyString s1={0}, s2={1}, expected={2}")
  @CsvSource({"'','', 0", "'',test, -4", "test,'', 4"})
  void testCompareTo_EmptyString(String s1, String s2, int expected) {
    assertEquals(expected, AlphanumComparator.compareTo(s1, s2));
  }

  @ParameterizedTest(name = "testCompareTo_NumericCharacters s1={0}, s2={1}, expected={2}")
  @CsvSource({"123, 123, 0", "123, a456, -48", "123, 456, -3", "70, 19, 6", "123, test, -67"})
  void testCompareTo_NumericCharacters(String s1, String s2, int expected) {
    assertEquals(expected, AlphanumComparator.compareTo(s1, s2));
  }

  @ParameterizedTest(name = "testCompareTo_NumericStrings s1={0}, s2={1}, expected={2}")
  @CsvSource({"100test, 100test, 0", "100test, 70, 1"})
  void testCompareTo_NumericStrings(String s1, String s2, int expected) {
    assertEquals(expected, AlphanumComparator.compareTo(s1, s2));
  }

  @ParameterizedTest(name = "testCompareTo_Strings s1={0}, s2={1}, expected={2}")
  @CsvSource({"test, test, 0", "test, Atest, 51"})
  void testCompareTo_Strings(String s1, String s2, int expected) {
    assertEquals(expected, AlphanumComparator.compareTo(s1, s2));
  }
}
