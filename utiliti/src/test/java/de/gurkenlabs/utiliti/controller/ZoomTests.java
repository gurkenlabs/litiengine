package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ZoomTests {
  @Test
  void testZoomIn() {
    for (int i = 0; i < 100; i++) {
      Zoom.in();
    }

    assertEquals(Zoom.getMax(), Zoom.get());
  }

  @Test
  void testZoomOut() {
    for (int i = 0; i < 100; i++) {
      Zoom.out();
    }

    assertEquals(Zoom.getMin(), Zoom.get());
  }

  @Test
  void testZoomMatchToPresets() {
    int matched1 = Zoom.match(1.11111f);
    int matched2 = Zoom.match(101f);
    int matched3 = Zoom.match(1000000f);
    int matched4 = Zoom.match(0.00001f);
    int matched5 = Zoom.match(3.1f);

    assertEquals(1.0f, Zoom.get(matched1));
    assertEquals(100f, Zoom.get(matched2));
    assertEquals(100f, Zoom.get(matched3));
    assertEquals(0.1f, Zoom.get(matched4));
    assertEquals(3f, Zoom.get(matched5));
  }
}
