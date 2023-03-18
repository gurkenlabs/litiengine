package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BooleanIntegerAdapterTests {

  @Test
  void testUnmarshallTrue() {
    BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
    assertTrue(booleanIntegerAdapter.unmarshal(1));
  }

  @Test
  void testUnmarshallNull() {
    BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
    assertNull(booleanIntegerAdapter.unmarshal(null));
  }

  @Test
  void testMarshallNull() {
    BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
    assertNull(booleanIntegerAdapter.marshal(null));
  }

  @Test
  void testMarshallTrue() {
    BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
    assertEquals(1, booleanIntegerAdapter.marshal(true));
  }

  @Test
  void testMarshallFalse() {
    BooleanIntegerAdapter booleanIntegerAdapter = new BooleanIntegerAdapter();
    assertEquals(0, booleanIntegerAdapter.marshal(false));
  }
}
