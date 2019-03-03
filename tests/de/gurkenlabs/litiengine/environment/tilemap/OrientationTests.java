package de.gurkenlabs.litiengine.environment.tilemap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

public class OrientationTests {
  @Test
  public void testOrthogonalMaps() {
    IMap map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ORTHOGONAL);
    when(map.getTileWidth()).thenReturn(9);
    when(map.getTileHeight()).thenReturn(13);
    when(map.getTileSize()).thenReturn(new Dimension(9, 13));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(360, 650), map.getOrientation().getSize(map));
    assertEquals(new Point(54, 52), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);
  }

  @Test
  public void testIsometricMaps() {
    IMap map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ISOMETRIC);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(450, 630), map.getOrientation().getSize(map));
    assertEquals(new Point(260, 77), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);
  }

  @Test
  public void testStaggeredMaps() {
    // test for all combinations of stagger axis+index
    IMap map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ISOMETRIC_STAGGERED);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.X);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.ODD);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(205, 707), map.getOrientation().getSize(map));
    assertEquals(new Point(30, 56), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ISOMETRIC_STAGGERED);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.X);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.EVEN);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(205, 707), map.getOrientation().getSize(map));
    assertEquals(new Point(30, 63), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ISOMETRIC_STAGGERED);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.Y);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.ODD);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(405, 357), map.getOrientation().getSize(map));
    assertEquals(new Point(65, 35), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.ISOMETRIC_STAGGERED);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.Y);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.EVEN);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(405, 357), map.getOrientation().getSize(map));
    assertEquals(new Point(60, 35), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);
  }

  @Test
  public void testHexagonalMaps() {
    // test for all combinations of stagger axis+index
    IMap map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.HEXAGONAL);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.X);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.ODD);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getHexSideLength()).thenReturn(4);
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(283, 707), map.getOrientation().getSize(map));
    assertEquals(new Point(42, 56), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.HEXAGONAL);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.X);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.EVEN);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getHexSideLength()).thenReturn(10); // square hexagons
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(400, 707), map.getOrientation().getSize(map));
    assertEquals(new Point(60, 63), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.HEXAGONAL);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.Y);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.ODD);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getHexSideLength()).thenReturn(10);
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(405, 602), map.getOrientation().getSize(map));
    assertEquals(new Point(65, 50), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);

    map = mock(IMap.class);
    when(map.getOrientation()).thenReturn(MapOrientations.HEXAGONAL);
    when(map.getStaggerAxis()).thenReturn(StaggerAxis.Y);
    when(map.getStaggerIndex()).thenReturn(StaggerIndex.EVEN);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(14);
    when(map.getTileSize()).thenReturn(new Dimension(10, 14));
    when(map.getHexSideLength()).thenReturn(20); // skinny hexagons
    when(map.getWidth()).thenReturn(40);
    when(map.getHeight()).thenReturn(50);
    when(map.getSizeInTiles()).thenReturn(new Dimension(40, 50));

    assertEquals(new Dimension(405, 847), map.getOrientation().getSize(map));
    assertEquals(new Point(60, 65), map.getOrientation().getLocation(6, 3, map));
    testOrientation(map);
  }

  private static void testOrientation(IMap map) {
    Random rand = ThreadLocalRandom.current();
    for (int i = 0; i < 50; i++) { // don't even bother with test points; just randomly sample them
      double x = rand.nextGaussian() * 200.0;
      double y = rand.nextGaussian() * 200.0;
      Point tile = map.getOrientation().getTile(x, y, map);
      Shape tileShape = map.getOrientation().getShape(tile.x, tile.y, map);
      assertTrue(tileShape.contains(x, y));
    }
  }
}
