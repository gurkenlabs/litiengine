package de.gurkenlabs.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CollisionResolvingTests {
  final double EPSILON = 1e-6;

  @BeforeEach
  public void init() {
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterEach
  public void clear() {
    Game.physics().clear();
  }

  @Test
  public void testBasicMovement() {
    Creature ent = getNewCreature();

    Game.physics().add(ent);

    // large rectangle at the bottom of the entity

    Game.physics().add(new CollisionBox(0, 25, 100, 10));

    // move 10 px to the right
    Game.physics().update();
    Game.physics().move(ent, 90, 10);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // move back 10 px to the left
    Game.physics().move(ent, 270, 10);

    assertEquals(10, ent.getX(), EPSILON);

    // move 5 px up where no collision is
    Game.physics().move(ent, 180, 5);

    assertEquals(5, ent.getY(), EPSILON);
    assertEquals(10, ent.getX(), EPSILON);
  }

  @Test
  public void testCollidingHorizontalMovement() {
    Creature ent = getNewCreature();

    Game.physics().add(ent);

    // large rectangle at the bottom of the entity
    Game.physics().add(new CollisionBox(0, 20, 100, 10));

    // move 10 px to the right
    Game.physics().update();
    Game.physics().move(ent, 90, 10);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // move back 10 px to the left
    Game.physics().move(ent, 270, 10);

    assertEquals(10, ent.getX(), EPSILON);

    // now "slide" along the rectangle to the bottom right
    Game.physics().move(ent, 45, 14.14213562373095);

    assertEquals(20, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom left
    Game.physics().move(ent, 315, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(10, ent.getY(), EPSILON);
  }

  @Test
  public void testCollidingVerticalMovement() {
    Creature ent = getNewCreature();

    Game.physics().add(ent);

    // large rectangle at the right of the entity
    Game.physics().add(new CollisionBox(20, 0, 10, 100));

    // move 10 px down
    Game.physics().update();
    Game.physics().move(ent, 0, 10);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(20, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom right

    Game.physics().move(ent, 45, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(30, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the top left
    Game.physics().move(ent, 135, 14.14213562373095);

    assertEquals(10, ent.getX(), EPSILON);
    assertEquals(20, ent.getY(), EPSILON);
  }

  @Test
  public void testFailingPhysics() {
    Creature ent = getNewCreature();

    Game.physics().add(ent);

    // large rectangle at the bottom of the entity
    Game.physics().add(new CollisionBox(0, 20, 50, 30));

    // another rectangle that creates an angle on the right side
    Game.physics().add(new CollisionBox(50, 20, 10, 100));

    // first relocate the entity
    ent.setLocation(45, 10);

    // move 10 px down
    Game.physics().move(ent, 0, 10);

    // the movement should have been denied
    assertEquals(45.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // now "slide" along the rectangle to the bottom right
    Game.physics().move(ent, 45, 14.14213562373095);

    assertEquals(55.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // now "slide" back
    Game.physics().move(ent, 315, 14.14213562373095);

    assertEquals(45.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // first relocate the entity
    ent.setLocation(55, 10);

    // now "slide" again
    Game.physics().move(ent, 45, 14.14213562373095);

    // the entity just went through the corner
    assertEquals(65.0, ent.getX(), EPSILON);
    assertEquals(20.0, ent.getY(), EPSILON);

    // first relocate the entity
    ent.setLocation(49, 10);

    // now "slide" again
    Game.physics().move(ent, 45, 14.14213562373095);

    assertEquals(10.0, ent.getY(), EPSILON);
    assertEquals(59.0, ent.getX(), EPSILON);
  }

  // TODO
  @Test
  public void testMultipleIntersection() {
    Creature ent = getNewCreature();

    Game.physics().add(ent);

    // thin rectangle at the bottom of the entity
    Game.physics().add(new CollisionBox(0, 25, 50, 10));

    // small square to top the right corner of the thin rectangle
    Game.physics().add(new CollisionBox(50, 20, 5, 5));

    // large rectangle at the right of the square
    Game.physics().add(new CollisionBox(55, 0, 50, 100));

    // first relocate the entity
    ent.setLocation(45, 10);

    // move 10 px down
    Game.physics().move(ent, 0, 10);

    // the movement should have been denied
    assertEquals(45.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // move along the square to the left
    Game.physics().move(ent, -90, 10);

    assertEquals(35.0, ent.getX(), EPSILON);
    assertEquals(10.0, ent.getY(), EPSILON);

    // move along the square downwards
    Game.physics().move(ent, 0, 10);

    assertEquals(35.0, ent.getX(), EPSILON);
    assertEquals(15.0, ent.getY(), EPSILON);

    // move along the thin rectangle to the right
    Game.physics().move(ent, 90, 15);

    assertEquals(40.0, ent.getX(), EPSILON);
    assertEquals(15.0, ent.getY(), EPSILON);
  }

  @ParameterizedTest(name="testCollisionWithMapBounds_xCoordinate angle={0}, distance={1}, expectedX={2}")
  @CsvSource({
          "270.0d, 20.0d, -10.0d",
          "90.0d, 50.0d, 30.0d",
  })
  public void testCollisionWithMapBounds_xCoordinate(double angle, double distance, double expectedX) {
    Creature ent = getNewCreature();
    ent.setWidth(30);
    ent.setHeight(30);
    ent.setCollisionBoxAlign(Align.CENTER);
    ent.setCollisionBoxValign(Valign.MIDDLE);

    Game.physics().setBounds(new Rectangle2D.Double(0, 0, 50, 50));
    Game.physics().add(ent);
    Game.physics().move(ent, angle, distance);
    double actualX = ent.getX();
    assertEquals(expectedX, actualX, EPSILON);
  }

  @ParameterizedTest(name="testCollisionWithMapBounds_yCoordinate angle={0}, distance={1}, expectedY={2}")
  @CsvSource({
          "180.0d, 20.0d, -10.0d",
          "0, 50.0d, 30.0d",
  })
  public void testCollisionWithMapBounds_yCoordinate(double angle, double distance, double expectedY) {
    Creature ent = getNewCreature();
    ent.setWidth(30);
    ent.setHeight(30);
    ent.setCollisionBoxAlign(Align.CENTER);
    ent.setCollisionBoxValign(Valign.MIDDLE);

    Game.physics().setBounds(new Rectangle2D.Double(0, 0, 50, 50));
    Game.physics().add(ent);
    Game.physics().move(ent, angle, distance);
    double actualY = ent.getY();
    assertEquals(expectedY, actualY, EPSILON);
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
