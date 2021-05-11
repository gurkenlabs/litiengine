package de.gurkenlabs.litiengine.entities.behavior;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AStarGridTest {
  private Rectangle2D rectangle;
  private AStarGrid aStarGrid;
  private AStarNode node;

  @BeforeEach
  public void setup() {
    aStarGrid = new AStarGrid(10, 20, 5);
    rectangle = mock(Rectangle2D.class);
    node = mock(AStarNode.class);
    aStarGrid.getIntersectedNodes(rectangle);
    aStarGrid.updateWalkable(rectangle);
  }

  @Test
  public void testUpdateWalkable() {
    verify(rectangle, times(0)).intersects(rectangle);
  }
}
