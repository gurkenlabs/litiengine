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

    assertEquals(20, down.getX());
    assertEquals(20, down.getY());
    assertEquals(10, down.getWidth());
    assertEquals(20, down.getHeight());

    Rectangle2D up = ResizeAnchor.UP.getNewBounds(10, -10, 20, 20, 10, 10);

    assertEquals(20, up.getX());
    assertEquals(10, up.getY());
    assertEquals(10, up.getWidth());
    assertEquals(20, up.getHeight());

    Rectangle2D left = ResizeAnchor.LEFT.getNewBounds(-10, 10, 20, 20, 10, 10);

    assertEquals(10, left.getX());
    assertEquals(20, left.getY());
    assertEquals(20, left.getWidth());
    assertEquals(10, left.getHeight());

    Rectangle2D right = ResizeAnchor.RIGHT.getNewBounds(10, 10, 20, 20, 10, 10);

    assertEquals(20, right.getX());
    assertEquals(20, right.getY());
    assertEquals(20, right.getWidth());
    assertEquals(10, right.getHeight());

    Rectangle2D downRight = ResizeAnchor.DOWNRIGHT.getNewBounds(10, 10, 20, 20, 10, 10);

    assertEquals(20, downRight.getX());
    assertEquals(20, downRight.getY());
    assertEquals(20, downRight.getWidth());
    assertEquals(20, downRight.getHeight());

    Rectangle2D downLeft = ResizeAnchor.DOWNLEFT.getNewBounds(-10, 10, 20, 20, 10, 10);

    assertEquals(10, downLeft.getX());
    assertEquals(20, downLeft.getY());
    assertEquals(20, downLeft.getWidth());
    assertEquals(20, downLeft.getHeight());

    Rectangle2D upRight = ResizeAnchor.UPRIGHT.getNewBounds(10, -10, 20, 20, 10, 10);

    assertEquals(20, upRight.getX());
    assertEquals(10, upRight.getY());
    assertEquals(20, upRight.getWidth());
    assertEquals(20, upRight.getHeight());

    Rectangle2D upLeft = ResizeAnchor.UPLEFT.getNewBounds(-10, -10, 20, 20, 10, 10);

    assertEquals(10, upLeft.getX());
    assertEquals(10, upLeft.getY());
    assertEquals(20, upLeft.getWidth());
    assertEquals(20, upLeft.getHeight());
  }

  @Test
  public void ensureThatResizeTransformDoesNotOvershoot() {
    Rectangle2D up = ResizeAnchor.UP.getNewBounds(10, 100, 20, 20, 10, 10);

    assertEquals(20, up.getX());
    assertEquals(30, up.getY());
    assertEquals(10, up.getWidth());
    assertEquals(0, up.getHeight());
    
    Rectangle2D upRight = ResizeAnchor.UPRIGHT.getNewBounds(10, 100, 20, 20, 10, 10);

    assertEquals(20, upRight.getX());
    assertEquals(30, upRight.getY());
    assertEquals(20, upRight.getWidth());
    assertEquals(0, upRight.getHeight());
    
    Rectangle2D upLeft = ResizeAnchor.UPLEFT.getNewBounds(-100, 100, 20, 20, 10, 10);

    assertEquals(-80, upLeft.getX());
    assertEquals(30, upLeft.getY());
    assertEquals(110, upLeft.getWidth());
    assertEquals(0, upLeft.getHeight());
  }
}
