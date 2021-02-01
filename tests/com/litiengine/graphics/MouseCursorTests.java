package com.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

public class MouseCursorTests {
  @Test
  public void testDefaultValues() {
    MouseCursor cursor = new MouseCursor();

    assertEquals(0, cursor.getOffsetX());
    assertEquals(0, cursor.getOffsetY());

    assertNull(cursor.getImage());
    assertNull(cursor.getTransform());

    assertFalse(cursor.isVisible());
  }

  @Test
  public void testSetters() {
    MouseCursor cursor = new MouseCursor();

    cursor.set(new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB));

    // by default center the image on the cursor position
    assertEquals(0, cursor.getOffsetX());
    assertEquals(0, cursor.getOffsetY());

    // as soon as an image is set, the cursor should be visible by default
    assertTrue(cursor.isVisible());

    cursor.setOffset(15, 15);

    assertEquals(15, cursor.getOffsetX());
    assertEquals(15, cursor.getOffsetY());

    cursor.setVisible(false);
    assertFalse(cursor.isVisible());
    
    AffineTransform trans = AffineTransform.getTranslateInstance(1, 2);
    cursor.setTransform(trans);
    
    assertEquals(trans, cursor.getTransform());
  }
}
