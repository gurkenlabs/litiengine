package de.gurkenlabs.utiliti.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SnapTests {
  @Test
  public void testPixelSnapping() {
    assertEquals(1, Snap.x(1.1, 16, false, true));
    assertEquals(2, Snap.x(1.9, 16, false, true));

    assertEquals(9, Snap.y(9.1, 16, false, true));
    assertEquals(10, Snap.y(9.91, 16, false, true));
  }
  
  @Test
  public void testGridSnapping() {
    assertEquals(0, Snap.x(1.1, 16, true, false));
    assertEquals(16, Snap.x(14, 16, true, false));

    assertEquals(0, Snap.y(7, 16, true, false));
    assertEquals(16, Snap.y(9.91, 16, true, false));
    
    // enabling pixel snapping while grid snapping is active should not make any difference
    assertEquals(0, Snap.x(1.1, 16, true, true));
    assertEquals(16, Snap.x(14, 16, true, true));

    assertEquals(0, Snap.y(7, 16, true, true));
    assertEquals(16, Snap.y(9.91, 16, true, true));
  }
}
