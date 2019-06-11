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
}
