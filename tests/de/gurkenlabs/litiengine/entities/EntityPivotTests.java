package de.gurkenlabs.litiengine.entities;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityPivotTests {
  @Test
  void getPoint_typeLOCATION_default() {
    // arrange
    Creature executor = mock(Creature.class);
    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.LOCATION, 0d, 0d);

    Point2D point00 = new Point2D.Double(0, 0);

    // act, assert
    assertEquals(point00, pivot.getPoint());
  }

  @Test
  void getPoint_typeLOCATION_specific() {
    // arrange
    Creature executor = mock(Creature.class);
    when(executor.getX()).thenReturn(1d);
    when(executor.getY()).thenReturn(1d);

    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.LOCATION, 0d, 0d);

    Point2D point11 = new Point2D.Double(1, 1);

    // act, assert
    assertEquals(point11, pivot.getPoint());
  }

  @Test
  void getPoint_typeOFFSET_specific() {
    // arrange
    Creature executor = mock(Creature.class);
    when(executor.getX()).thenReturn(-1d);
    when(executor.getY()).thenReturn(-1d);

    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.OFFSET, 0d, 0d);

    Point2D point11 = new Point2D.Double(-1, -1);

    // act, assert
    assertEquals(point11, pivot.getPoint());
  }

  @Test
  void getPoint_typeOFFSET_withOffset() {
    // arrange
    Creature executor = mock(Creature.class);
    when(executor.getX()).thenReturn(1d);
    when(executor.getY()).thenReturn(1d);

    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.OFFSET, 1d, 1d);

    Point2D offPoint = new Point2D.Double(2, 2);

    // act, assert
    assertEquals(offPoint, pivot.getPoint());
  }

  @Test
  void getPoint_typeDIMENSIONCENTER() {
    // arrange
    Creature executor = mock(Creature.class);
    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.DIMENSION_CENTER, 0d, 0d);

    Point2D center = new Point2D.Double(5, 5);
    when(executor.getCenter()).thenReturn(center);

    // act, assert
    assertEquals(center, pivot.getPoint());
  }

  @Test
  void getPoint_typeCOLLISIONBOXCENTER() {
    // arrange
    Creature executor = mock(Creature.class);
    EntityPivot pivot = new EntityPivot(executor, EntityPivotType.COLLISIONBOX_CENTER, 0d, 0d);

    Point2D center = new Point2D.Double(16, 25);
    Rectangle2D collisionBox = new Rectangle2D.Double(10, 19, 12, 12); // getCenter = a + b/2
    when(executor.getCollisionBox()).thenReturn(collisionBox);

    // act, assert
    assertEquals(center, pivot.getPoint());
  }
}
