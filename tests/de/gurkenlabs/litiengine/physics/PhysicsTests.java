package de.gurkenlabs.litiengine.physics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.entities.MovableCombatEntity;

public class PhysicsTests {

  @Test
  public void TestBasicCollisionDetection() {
    IMovableCombatEntity ent = new MovableCombatEntity();
    ent.setSize(16, 16);
    ent.setCollision(true);
    ent.setCollisionBoxWidth(16);
    ent.setCollisionBoxHeight(16);
    ent.setLocation(10, 10);

    try (GameLoop loop = new GameLoop(30)) {
      IPhysicsEngine engine = new PhysicsEngine();
      engine.add(ent);

      loop.start();
      loop.attach(new IUpdateable() {
        @Override
        public void update(IGameLoop loop) {
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
        }
      });
    }
  }
}
