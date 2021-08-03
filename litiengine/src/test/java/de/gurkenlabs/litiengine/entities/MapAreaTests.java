package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;

public class MapAreaTests {
  @Test
  public void testConstructorDefault() {
    // act
    MapArea mapArea = new MapArea();

    // assert
    assertNotNull(mapArea);
    assertEquals(0, mapArea.getMapId()); // default
    assertNull(mapArea.getName());
  }

  @Test
  public void testConstructorWithArgs() {
    // act
    MapArea mapArea = new MapArea(1, 2, 3, 4);

    // assert
    assertEquals(0, mapArea.getMapId());
    assertNull(mapArea.getName());
    assertEquals(1, mapArea.getX());
    assertEquals(2, mapArea.getY());
    assertEquals(3, mapArea.getWidth());
    assertEquals(4, mapArea.getHeight());
  }

  @Test
  public void testConstructorWithId() {
    // act
    MapArea mapArea = new MapArea(42, "cool name", 9, 8, 7, 6);

    // assert
    assertEquals(42, mapArea.getMapId());
    assertEquals("cool name", mapArea.getName());
    assertEquals(9, mapArea.getX());
    assertEquals(8, mapArea.getY());
    assertEquals(7, mapArea.getWidth());
    assertEquals(6, mapArea.getHeight());
    assertEquals(new Point2D.Double(9, 8), mapArea.getLocation());
  }
}
