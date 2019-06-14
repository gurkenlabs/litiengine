package de.gurkenlabs.utiliti.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.utiliti.handlers.Transform.ResizeAnchor;

public class TransformTests {
  @Test
  public void testAnchorUpdate() {
    Transform.updateAnchors();
    
    assertEquals(0, Transform.getAnchors().size());
  }
  
  @Test
  public void testResizeTransform() {
    
    Rectangle2D down = ResizeAnchor.DOWN.getNewBounds(10, 10, 20, 20, 10, 10);
    
    assertEquals(down.getX(), 20);
    assertEquals(down.getY(), 20);
    assertEquals(down.getWidth(), 10);
    assertEquals(down.getHeight(), 20);
    
    Rectangle2D up = ResizeAnchor.UP.getNewBounds(10, -10, 20, 20, 10, 10);
    
    assertEquals(up.getX(), 20);
    assertEquals(up.getY(), 10);
    assertEquals(up.getWidth(), 10);
    assertEquals(up.getHeight(), 20);
    
    Rectangle2D left = ResizeAnchor.LEFT.getNewBounds(-10, 10, 20, 20, 10, 10);
    
    assertEquals(left.getX(), 10);
    assertEquals(left.getY(), 20);
    assertEquals(left.getWidth(), 20);
    assertEquals(left.getHeight(), 10);
    
    Rectangle2D right = ResizeAnchor.RIGHT.getNewBounds(10, 10, 20, 20, 10, 10);
    
    assertEquals(right.getX(), 20);
    assertEquals(right.getY(), 20);
    assertEquals(right.getWidth(), 20);
    assertEquals(right.getHeight(), 10);
    
    Rectangle2D downRight = ResizeAnchor.DOWNRIGHT.getNewBounds(10, 10, 20, 20, 10, 10);
    
    assertEquals(downRight.getX(), 20);
    assertEquals(downRight.getY(), 20);
    assertEquals(downRight.getWidth(), 20);
    assertEquals(downRight.getHeight(), 20);
    
    Rectangle2D downLeft = ResizeAnchor.DOWNLEFT.getNewBounds(-10, 10, 20, 20, 10, 10);
    
    assertEquals(downLeft.getX(), 10);
    assertEquals(downLeft.getY(), 20);
    assertEquals(downLeft.getWidth(), 20);
    assertEquals(downLeft.getHeight(), 20);
    
    Rectangle2D upRight = ResizeAnchor.UPRIGHT.getNewBounds(10, -10, 20, 20, 10, 10);
    
    assertEquals(upRight.getX(), 20);
    assertEquals(upRight.getY(), 10);
    assertEquals(upRight.getWidth(), 20);
    assertEquals(upRight.getHeight(), 20);
    
    Rectangle2D upLeft = ResizeAnchor.UPLEFT.getNewBounds(-10, -10, 20, 20, 10, 10);
    
    assertEquals(upLeft.getX(), 10);
    assertEquals(upLeft.getY(), 10);
    assertEquals(upLeft.getWidth(), 20);
    assertEquals(upLeft.getHeight(), 20);
  }
}
