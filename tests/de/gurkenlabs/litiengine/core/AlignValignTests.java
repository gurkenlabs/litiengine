package de.gurkenlabs.litiengine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AlignValignTests {

  @ParameterizedTest
  @EnumSource(Align.class)
  void testAlign(Align align) {
    assertEquals(align, Align.get(align.name()));
    assertEquals(align, Align.get(align.name().toLowerCase()));
  }

  @ParameterizedTest
  @EnumSource(Valign.class)
  void testValign(Valign valign) {
    assertEquals(valign, Valign.get(valign.name()));
    assertEquals(valign, Valign.get(valign.name().toLowerCase()));
  }

  @Test
  void testAlignGetWithEmptyAlignString() {
    assertEquals(Align.CENTER, Align.get(""));
    assertEquals(Align.CENTER, Align.get(null));
  }

  @Test
  void testGet() {
    assertEquals(Valign.DOWN, Valign.get(""));
    assertEquals(Valign.DOWN, Valign.get(null));
  }

  @Test
  void testAlignGetWithInvalidAlignString() {
    assertEquals(Align.CENTER, Align.get("INVALID_ALIGN"));
  }
}
