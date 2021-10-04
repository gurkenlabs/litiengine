package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MaterialTests {

  @Test
  public void testEmptyValue() {
    assertEquals(Material.UNDEFINED, Material.get(""));
  }

  @Test
  public void testNullValue() {
    assertEquals(Material.UNDEFINED, Material.get(null));
  }
}
