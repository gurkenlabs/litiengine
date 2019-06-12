package de.gurkenlabs.utiliti.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ZoomTests {
  @Test
  public void testZoomIn() {
    for (int i = 0; i < 100; i++) {
      Zoom.in();
    }

    assertEquals(Zoom.getMax(), Zoom.get());
  }

  @Test
  public void testZoomOut() {
    for (int i = 0; i < 100; i++) {
      Zoom.out();
    }

    assertEquals(Zoom.getMin(), Zoom.get());
  }

  @Test
  public void testZoomMatchToPresets() {
    float matched1 = Zoom.match(1.11111f);
    float matched2 = Zoom.match(101f);
    float matched3 = Zoom.match(1000000f);
    float matched4 = Zoom.match(0.00001f);
    float matched5 = Zoom.match(3.1f);

    assertEquals(1.0f, matched1);
    assertEquals(100f, matched2);
    assertEquals(100f, matched3);
    assertEquals(0.1f, matched4);
    assertEquals(3f, matched5);
  }
}
