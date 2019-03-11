package de.gurkenlabs.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.Creature;

@SuppressWarnings("deprecation")
public class CollisionResolvingTests {
  final double EPSILON = 0.02;

  @Test
  public void testBasicMovement() {
    Creature ent = getNewCreature();

    PhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);

    // large rectangle at the bottom of the entity
    Rectangle2D rect1 = new Rectangle2D.Double(0, 25, 100, 10);
    engine.add(rect1);

    // move 10 px to the right
    engine.update();
    engine.move(ent, 90, 10);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // move back 10 px to the left
    engine.move(ent, 270, 10);

    assertEquals(10, ent.getX(), EPSILON);

    // move 5 px up where no collision is
    engine.move(ent, 180, 5);

    assertEquals(5, ent.getY(), EPSILON);
    assertEquals(10, ent.getX(), EPSILON);
  }

  @Test
  public void testCollidingHorizontalMovement() {
    Creature ent = getNewCreature();

    PhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);

    // large rectangle at the bottom of the entity
    Rectangle2D rect1 = new Rectangle2D.Double(0, 20, 100, 10);
    engine.add(rect1);

    // move 10 px to the right
    engine.update();
    engine.move(ent, 90, 10);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // move back 10 px to the left
    engine.move(ent, 270, 10);

    assertEquals(10, ent.getX(), EPSILON);

    // now "slide" along the rectangle to the bottom right
    engine.move(ent, 45, 14.14213562373095);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom left
    engine.move(ent, 315, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);
  }

  @Test
  public void testCollidingVerticalMovement() {
    Creature ent = getNewCreature();

    PhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);

    // large rectangle at the right of the entity
    Rectangle2D rect1 = new Rectangle2D.Double(20, 0, 10, 100);
    engine.add(rect1);

    // move 10 px down
    engine.update();
    engine.move(ent, 0, 10);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(20, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom right

    engine.move(ent, 45, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(30, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the top left
    engine.move(ent, 135, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(20, ent.getY(), EPSILON);
  }

  @Test
  public void testFailingPhysics() {
    Creature ent = getNewCreature();

    PhysicsEngine engine = new PhysicsEngine();
    engine.add(ent);

    // large rectangle at the bottom of the entity
    Rectangle2D rect1 = new Rectangle2D.Double(0, 20, 50, 30);

    // another rectangle that creates an angle on the right side
    Rectangle2D rect2 = new Rectangle2D.Double(50, 20, 10, 100);
    engine.add(rect1);
    engine.add(rect2);

    // first relocate the entity
    ent.setLocation(45, 10);

    // move 10 px down
    engine.move(ent, 0, 10);

    // the movement should have been denied
    assertEquals(45.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom right
    engine.move(ent, 45, 14.14213562373095);

    assertEquals(55.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // now "slide" back
    engine.move(ent, 315, 14.14213562373095);

    assertEquals(45.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // first relocate the entity
    ent.setLocation(55, 10);

    // now "slide" again
    engine.move(ent, 45, 14.14213562373095);

    // the entity just went through the corner
    assertEquals(65.0, ent.getX(), EPSILON);
    assertEquals(20.0, ent.getY(), EPSILON);

    // first relocate the entity
    ent.setLocation(49, 10);

    // now "slide" again
    engine.move(ent, 45, 14.14213562373095);

    assertEquals(10.0, ent.getY(), EPSILON);
    assertEquals(59.0, ent.getX(), EPSILON);
  }

  @Test
  public void testCollisiionWithMapBounds() {
    Creature ent = getNewCreature();
    ent.setWidth(30);
    ent.setHeight(30);
    ent.setCollisionBoxAlign(Align.CENTER);
    ent.setCollisionBoxValign(Valign.MIDDLE);

    PhysicsEngine engine = new PhysicsEngine();
    engine.setBounds(new Rectangle2D.Double(0, 0, 50, 50));
    engine.add(ent);
    
    // move back 20 px to the left
    engine.move(ent, 270, 20);

    assertEquals(-10, ent.getX(), EPSILON);
    
    // move back 20 px up
    engine.move(ent, 180, 20);

    assertEquals(-10, ent.getY(), EPSILON);
    
    // move back 50 px right
    engine.move(ent, 90, 50);
    
    assertEquals(30, ent.getX(), EPSILON);
    
    // move 50 px down
    engine.move(ent, 0, 50);
    
    assertEquals(30, ent.getY(), EPSILON);
  }

  private static Creature getNewCreature() {
    Creature ent = new Creature();
    ent.setX(10);
    ent.setY(10);
    ent.setWidth(10);
    ent.setHeight(10);
    ent.setCollisionBoxHeight(10);
    ent.setCollisionBoxWidth(10);
    ent.setCollision(true);

    return ent;
  }
}
