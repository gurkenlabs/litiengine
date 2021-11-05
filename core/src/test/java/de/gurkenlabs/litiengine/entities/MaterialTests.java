package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MaterialTests {

  @Test
  void testEmptyValue() {
    assertEquals(Material.UNDEFINED, Material.get(""));
  }

  @Test
  void testNullValue() {
    assertEquals(Material.UNDEFINED, Material.get(null));
  }
}
