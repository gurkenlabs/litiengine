package com.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import com.litiengine.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.litiengine.entities.CollisionBox;
import com.litiengine.entities.Creature;
import com.litiengine.entities.IMobileEntity;

public class PhysicsTests {
  @BeforeEach
  public void init() {
    Game.init(Game.COMMADLINE_ARG_NOGUI);
    Game.physics().clear();
  }
  
  @AfterEach
  public void clear() {
    Game.physics().clear();
  }

  @Test
  public void testBasicCollisionDetection() {
    Creature ent = new Creature();
    ent.setSize(16, 16);
    ent.setCollision(true);
    ent.setCollisionBoxWidth(16);
    ent.setCollisionBoxHeight(16);
    ent.setLocation(10, 10);

    Game.physics().add(ent);
    Game.physics().update();

    assertFalse(Game.physics().collides(9, 9));
    assertFalse(Game.physics().collides(27, 27));
    assertTrue(Game.physics().collides(10.00001, 10.00001));
    assertTrue(Game.physics().collides(25.99999, 25.99999));

    Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
    Rectangle2D rect2 = new Rectangle2D.Double(10, 10, 0, 0);
    Rectangle2D rect3 = new Rectangle2D.Double(10, 10, 1, 1);
    Rectangle2D rect4 = new Rectangle2D.Double(8, 8, 3, 3);
    Rectangle2D rect5 = new Rectangle2D.Double(25.99999, 10, 10, 20);

    assertFalse(Game.physics().collides(rect1));
    assertFalse(Game.physics().collides(rect2));
    assertTrue(Game.physics().collides(rect3));
    assertTrue(Game.physics().collides(rect4));
    assertTrue(Game.physics().collides(rect5));
  }

  @Test
  public void testPointCollides() {
    IMobileEntity ent = mock(IMobileEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);
    when(ent.getCollisionType()).thenReturn(Collision.DYNAMIC);

    Game.physics().add(ent);
    Game.physics().update();

    assertTrue(Game.physics().collides(5, 5));
    assertTrue(Game.physics().collides(5, 5, Collision.ANY));
    assertTrue(Game.physics().collides(5, 5, Collision.DYNAMIC));
    assertFalse(Game.physics().collides(5, 5, Collision.STATIC));
    assertFalse(Game.physics().collides(5, 5, Collision.NONE));

    Game.physics().remove(ent);
    Game.physics().update();

    assertFalse(Game.physics().collides(5, 5));
  }

  @Test
  public void testRaycastCollides() {
    IMobileEntity ent = mock(IMobileEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);
    when(ent.getCollisionType()).thenReturn(Collision.DYNAMIC);

    Game.physics().add(ent);
    Game.physics().add(new CollisionBox(5, 5, 10, 10));
    Game.physics().update();

    assertTrue(Game.physics().collides(new Line2D.Double(0, 0, 5, 5)));
    assertTrue(Game.physics().collides(new Line2D.Double(10, 10, 5, 5)));
    assertFalse(Game.physics().collides(new Line2D.Double(15.1, 15.0, 15, 15)));
  }

  @Test
  public void testRectangleCollides() {
    IMobileEntity ent = mock(IMobileEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);
    when(ent.getCollisionType()).thenReturn(Collision.DYNAMIC);

    Game.physics().add(ent);
    Game.physics().update();

    assertTrue(Game.physics().collides(new Rectangle2D.Double(9, 9, 5, 5)));

    assertTrue(Game.physics().collides(new Rectangle2D.Double(9, 9, 5, 5), Collision.DYNAMIC));
    assertTrue(Game.physics().collides(new Rectangle2D.Double(9, 9, 5, 5), Collision.ANY));
    assertFalse(Game.physics().collides(new Rectangle2D.Double(9, 9, 5, 5), Collision.STATIC));
    assertFalse(Game.physics().collides(new Rectangle2D.Double(9, 9, 5, 5), Collision.NONE));

    assertFalse(Game.physics().collides(new Rectangle2D.Double(10.1, 10.1, 5, 5)));
  }
}
