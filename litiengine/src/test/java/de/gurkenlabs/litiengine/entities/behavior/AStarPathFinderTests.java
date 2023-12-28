package de.gurkenlabs.litiengine.entities.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AStarPathFinderTests {
  private AStarPathFinder pathFinder;

  @BeforeEach
  public void setUp() {
    // Create a mock AStarGrid for testing
    AStarGrid grid = Mockito.mock(AStarGrid.class);
    Mockito.when(grid.getSize()).thenReturn(new Dimension(10, 10));

    pathFinder = new AStarPathFinder(grid);
  }

  @Test
  void testConstructorWithAStarGrid() {
    assertNotNull(pathFinder);
    assertNotNull(pathFinder.getGrid());
  }

  @Test
  void testConstructorWithSizeAndGridNodeSize() {
    AStarPathFinder pathFinder = new AStarPathFinder(new Dimension(10, 10), 32);
    assertNotNull(pathFinder);
    assertNotNull(pathFinder.getGrid());
  }

  @Test
  void testConstructorWithIMapAndGridNodeSize() {
    IMap map = Mockito.mock(IMap.class);
    Mockito.when(map.getSizeInPixels()).thenReturn(new Dimension(640, 480));
    Mockito.when(map.getTileSize()).thenReturn(new Dimension(32, 32));

    AStarPathFinder pathFinder = new AStarPathFinder(map, 32);
    assertNotNull(pathFinder);
    assertNotNull(pathFinder.getGrid());
  }

  @Test
  void testConstructorWithIMap() {
    IMap map = Mockito.mock(IMap.class);
    Mockito.when(map.getSizeInPixels()).thenReturn(new Dimension(640, 480));
    Mockito.when(map.getTileSize()).thenReturn(new Dimension(32, 32));

    AStarPathFinder pathFinder = new AStarPathFinder(map);
    assertNotNull(pathFinder);
    assertNotNull(pathFinder.getGrid());
  }

  @Test
  void testFindPathWithDirectPath() {
    IMobileEntity entity = Mockito.mock(IMobileEntity.class);
    Mockito.when(entity.getCollisionBoxCenter()).thenReturn(new Point2D.Double(10, 10));

    Path path = pathFinder.findPath(entity, new Point2D.Double(50, 50));

    assertNotNull(path);
    assertFalse(path.getPoints().isEmpty());
    assertEquals(new Point2D.Double(10, 10), path.getStart());
    assertEquals(new Point2D.Double(50, 50), path.getTarget());
  }

  @Test
  void testFindPathWithAStarPath() {
    IMobileEntity entity = Mockito.mock(IMobileEntity.class);
    Mockito.when(entity.getCollisionBoxCenter()).thenReturn(new Point2D.Double(10, 10));

    Path path = pathFinder.findPath(entity, new Point2D.Double(90, 90));

    assertNotNull(path);
    assertFalse(path.getPoints().isEmpty());
    assertEquals(new Point2D.Double(10, 10), path.getStart());
    assertEquals(new Point2D.Double(90, 90), path.getTarget());
  }
}
