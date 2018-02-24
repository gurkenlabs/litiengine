package de.gurkenlabs.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMobileCombatEntity;
import de.gurkenlabs.litiengine.entities.MobileCombatEntity;

public class PhysicsTests {

  @Test
  public void testBasicCollisionDetection() {
    try (GameLoop loop = new GameLoop(30)) {
      IMobileCombatEntity ent = new MobileCombatEntity();
      ent.setSize(16, 16);
      ent.setCollision(true);
      ent.setCollisionBoxWidth(16);
      ent.setCollisionBoxHeight(16);
      ent.setLocation(10, 10);

      IPhysicsEngine engine = new PhysicsEngine();
      engine.add(ent);

      loop.start();
      loop.attach(() -> {
        assertFalse(engine.collides(9, 9));
        assertFalse(engine.collides(27, 27));
        assertTrue(engine.collides(10.00001, 10.00001));
        assertTrue(engine.collides(25.99999, 25.99999));

        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(10, 10, 0, 0);
        Rectangle2D rect3 = new Rectangle2D.Double(10, 10, 1, 1);
        Rectangle2D rect4 = new Rectangle2D.Double(8, 8, 3, 3);
        Rectangle2D rect5 = new Rectangle2D.Double(25.99999, 10, 10, 20);

        assertFalse(engine.collides(rect1));
        assertFalse(engine.collides(rect2));
        assertTrue(engine.collides(rect3));
        assertTrue(engine.collides(rect4));
        assertTrue(engine.collides(rect5));
      });
    }
  }

  @Test
  public void testPointCollides() {
    ICombatEntity ent = mock(ICombatEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);

    IPhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);
    engine.update();

    assertTrue(engine.collides(5, 5));
    assertTrue(engine.collides(5, 5, CollisionType.ALL));
    assertTrue(engine.collides(5, 5, CollisionType.ENTITY));
    assertFalse(engine.collides(5, 5, CollisionType.STATIC));
    assertFalse(engine.collides(5, 5, CollisionType.NONE));

    engine.remove(ent);
    engine.update();

    assertFalse(engine.collides(5, 5));
  }

  @Test
  public void testRaycastCollides() {
    ICombatEntity ent = mock(ICombatEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);

    IPhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);
    engine.add(new Rectangle2D.Double(5, 5, 10, 10));
    engine.update();

    assertNotNull(engine.collides(new Line2D.Double(0, 0, 5, 5)));
    assertNotNull(engine.collides(new Line2D.Double(10, 10, 5, 5)));
    assertNull(engine.collides(new Line2D.Double(15.1, 15.0, 15, 15)));
  }

  @Test
  public void testRectangleCollides() {
    ICombatEntity ent = mock(ICombatEntity.class);
    when(ent.getCollisionBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));
    when(ent.hasCollision()).thenReturn(true);

    IPhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);
    engine.update();

    assertTrue(engine.collides(new Rectangle2D.Double(9, 9, 5, 5)));

    assertTrue(engine.collides(new Rectangle2D.Double(9, 9, 5, 5), CollisionType.ENTITY));
    assertTrue(engine.collides(new Rectangle2D.Double(9, 9, 5, 5), CollisionType.ALL));
    assertFalse(engine.collides(new Rectangle2D.Double(9, 9, 5, 5), CollisionType.STATIC));
    assertFalse(engine.collides(new Rectangle2D.Double(9, 9, 5, 5), CollisionType.NONE));

    assertFalse(engine.collides(new Rectangle2D.Double(10.1, 10.1, 5, 5)));

    assertTrue(engine.collidesWithEntites(new Rectangle2D.Double(9, 9, 5, 5)).contains(ent));
  }
}
