package de.gurkenlabs.utiliti.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.utiliti.components.Editor;

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
  
  @Test
  public void testGridSnappingDivision() {
    float aThird = (float) MathUtilities.round(16.0/3.0, 2);
    float twoThirds = (float) MathUtilities.round((16.0/3.0)*2.0, 2);

    Editor.preferences().setSnapDivision(3);

    assertEquals(0, Snap.x(1.1, 16, true, false));
    assertEquals(aThird, Snap.x(6, 16, true, false));
    assertEquals(twoThirds, Snap.x(11.1, 16, true, false));
    assertEquals(16, Snap.x(14, 16, true, false));

    assertEquals(0, Snap.y(2, 16, true, false));
    assertEquals(aThird, Snap.y(7.99, 16, true, false));
    assertEquals(twoThirds, Snap.y(9.91, 16, true, false));
    assertEquals(16, Snap.y(15, 16, true, false));

    // enabling pixel snapping while grid snapping is active should not make any difference
    assertEquals(0, Snap.x(1.1, 16, true, true));
    assertEquals(aThird, Snap.x(6, 16, true, true));
    assertEquals(twoThirds, Snap.x(11.1, 16, true, true));
    assertEquals(16, Snap.x(14, 16, true, true));

    assertEquals(0, Snap.y(2, 16, true, true));
    assertEquals(aThird, Snap.y(7.99, 16, true, true));
    assertEquals(twoThirds, Snap.y(9.91, 16, true, true));
    assertEquals(16, Snap.y(14, 16, true, true));

    Editor.preferences().setSnapDivision(2);

    assertEquals(0, Snap.x(1.1, 16, true, false));
    assertEquals(16/2, Snap.x(11.99, 16, true, false));
    assertEquals(16, Snap.x(14, 16, true, false));

    assertEquals(0, Snap.y(3.99, 16, true, false));
    assertEquals(16/2, Snap.y(9.91, 16, true, false));
    assertEquals(16, Snap.y(12, 16, true, false));

    // enabling pixel snapping while grid snapping is active should not make any difference
    assertEquals(0, Snap.x(1.1, 16, true, true));
    assertEquals(16/2, Snap.x(11.99, 16, true, true));
    assertEquals(16, Snap.x(14, 16, true, true));

    assertEquals(0, Snap.y(3.99, 16, true, true));
    assertEquals(16/2, Snap.y(9.91, 16, true, true));
    assertEquals(16, Snap.y(12, 16, true, true));

    // reset to no divisions
    Editor.preferences().setSnapDivision(1);
  }
}
