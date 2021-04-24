package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MouseCursorTests {

  private MouseCursor cursor;

  @BeforeEach
  public void setUp(){
    cursor = new MouseCursor();
  }

  @Test
  public void testDefaultValues() {
    assertEquals(0, cursor.getOffsetX());
    assertEquals(0, cursor.getOffsetY());

    assertNull(cursor.getImage());
    assertNull(cursor.getTransform());

    assertFalse(cursor.isVisible());
  }

  @Test
  public void testSet(){
    // act
    cursor.set(new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB));

    // assert
    // by default center the image on the cursor position
    assertEquals(0, cursor.getOffsetX());
    assertEquals(0, cursor.getOffsetY());
  }

  @Test
  public void testSetIsVisible(){
    // act
    cursor.set(new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB));

    // assert
    // as soon as an image is set, the cursor should be visible by default
    assertTrue(cursor.isVisible());
  }

  @Test
  public void testSetOffset(){
    // act
    cursor.setOffset(15, 15);

    // assert
    assertEquals(15, cursor.getOffsetX());
    assertEquals(15, cursor.getOffsetY());
  }

  @Test
  public void testSetVisible(){
    // act
    cursor.setVisible(false);

    // assert
    assertFalse(cursor.isVisible());
  }

  @Test
  public void testSetTransform(){
    // arrange
    AffineTransform trans = AffineTransform.getTranslateInstance(1, 2);

    // act
    cursor.setTransform(trans);

    // assert
    assertEquals(trans, cursor.getTransform());
  }
}
