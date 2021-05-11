package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ListUtilitiesTests {
  @Test
  public void testContainsInstance() {
    List<Object> list = Arrays.asList("foo", 5L, 4.2);
    assertTrue(ListUtilities.containsInstance(list, String.class));
    assertTrue(ListUtilities.containsInstance(list, Long.class));
    assertTrue(ListUtilities.containsInstance(list, Double.class));
    assertFalse(ListUtilities.containsInstance(list, Boolean.class));
    assertFalse(ListUtilities.containsInstance(list, Byte.class));
    assertFalse(ListUtilities.containsInstance(list, Short.class));
    assertFalse(ListUtilities.containsInstance(list, Integer.class));
    assertFalse(ListUtilities.containsInstance(list, Float.class));
  }

  @Test
  public void testIntList() {
    List<Integer> list = ListUtilities.getIntList(5, 87, 23, 0, -54);
    assertEquals(Arrays.asList(5, 87, 23, 0, -54), list);
  }
}
