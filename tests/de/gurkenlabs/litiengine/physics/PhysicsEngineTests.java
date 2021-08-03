package de.gurkenlabs.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PhysicsEngineTests {

  @BeforeEach
  void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterEach
  void tearDown() {
    Game.physics().clear();
  }

  @Test
  void testConstructorNoInstancesAllowed() {
    // arrange, act, assert
    assertThrows(UnsupportedOperationException.class, () -> new PhysicsEngine());
  }

  @ParameterizedTest
  @MethodSource("getRaycastCollisionArguments")
  void testRaycastCollision(Line2D rayLine, double expectedHitX, double expectedHitY) {
    // arrange
    ICollisionEntity sourceEntity = new CollisionBox(1, 1, 5, 5);
    ICollisionEntity targetEntity = new CollisionBox(7, 5, 1, 4);

    Game.physics().add(sourceEntity);
    Game.physics().add(targetEntity);

    // act
    RaycastHit hit = Game.physics().raycast(rayLine, Collision.ANY, sourceEntity);

    // assert
    assertNotNull(hit);
    assertEquals(expectedHitX, hit.getPoint().getX());
    assertEquals(expectedHitY, hit.getPoint().getY());
  }

  @Test
  void testRaycastNoCollisionNoOtherEntity() {
    // arrange
    ICollisionEntity sourceEntity = new CollisionBox(1, 3, 5, 5);
    Line2D line = new Line2D.Double(0, 0, 10, 10);
    Game.physics().add(sourceEntity);

    // act
    RaycastHit hit = Game.physics().raycast(line, Collision.ANY, sourceEntity);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastNoCollisionOtherEntity() {
    // arrange
    ICollisionEntity sourceEntity = new CollisionBox(1, 3, 5, 5);
    ICollisionEntity otherEntity = new CollisionBox(10, 30, 5, 5);
    Line2D line = new Line2D.Double(0, 0, 10, 10);
    Game.physics().add(sourceEntity);
    Game.physics().add(otherEntity);

    // act
    RaycastHit hit = Game.physics().raycast(line, Collision.ANY, sourceEntity);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastLine2DNoHit() {
    // arrange
    Line2D line = new Line2D.Double(0, 0, 10, 10);

    // act
    RaycastHit hit = Game.physics().raycast(line);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastLine2DHit() {
    // arrange
    ICollisionEntity targetEntity = new CollisionBox(1, 3, 5, 5);
    Line2D line = new Line2D.Double(0, 0, 10, 10);
    Game.physics().add(targetEntity);

    // act
    RaycastHit hit = Game.physics().raycast(line);

    // assert
    assertEquals(3, hit.getPoint().getX());
    assertEquals(3, hit.getPoint().getY());
  }

  @Test
  void testRaycastPoint2DPoint2DNoHit() {
    // arrange
    Point2D point1 = new Point2D.Double(0, 0);
    Point2D point2 = new Point2D.Double(10, 10);

    // act
    RaycastHit hit = Game.physics().raycast(point1, point2);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastPoint2DPoint2DHit() {
    // arrange
    ICollisionEntity targetEntity = new CollisionBox(1, 3, 5, 5);
    Point2D point1 = new Point2D.Double(0, 0);
    Point2D point2 = new Point2D.Double(10, 10);
    Game.physics().add(targetEntity);

    // act
    RaycastHit hit = Game.physics().raycast(point1, point2);

    // assert
    assertEquals(3, hit.getPoint().getX());
    assertEquals(3, hit.getPoint().getY());
  }

  @Test
  void testRaycastPoint2DAngleHit() {
    // arrange
    ICollisionEntity targetEntity = new CollisionBox(1, 3, 5, 5);
    Game.physics().add(targetEntity);
    Game.physics().setBounds(new Rectangle2D.Double(0, 0, 100, 100));
    Point2D source = new Point2D.Double(0, 10);

    // act
    RaycastHit hit = Game.physics().raycast(source, 135);

    // assert
    assertEquals(2, hit.getPoint().getX(), 0.0001d);
    assertEquals(8, hit.getPoint().getY(), 0.0001d);
  }

  @Test
  void testRaycastPoint2DAngleNoHit() {
    // arrange
    ICollisionEntity otherEntity = new CollisionBox(1, 3, 5, 5);
    Game.physics().add(otherEntity);
    Game.physics().setBounds(new Rectangle2D.Double(0, 0, 100, 100));
    Point2D source = new Point2D.Double(0, 10);

    // act
    RaycastHit hit = Game.physics().raycast(source, 300);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastLine2DCollisionHit() {
    // arrange
    ICollisionEntity targetEntity = new CollisionBox(1, 3, 5, 5);
    Game.physics().add(targetEntity);
    Line2D line = new Line2D.Double(0, 0, 20, 20);

    // act
    RaycastHit hit = Game.physics().raycast(line, Collision.ANY);

    // assert
    assertEquals(3, hit.getPoint().getX());
    assertEquals(3, hit.getPoint().getY());
  }

  @Test
  void testRaycastLine2DCollisionNoHit() {
    // arrange
    ICollisionEntity otherEntity = new CollisionBox(8, 1, 5, 5);
    Game.physics().add(otherEntity);
    Line2D line = new Line2D.Double(0, 0, 20, 20);

    // act
    RaycastHit hit = Game.physics().raycast(line, Collision.ANY);

    // assert
    assertNull(hit);
  }

  @Test
  void testRaycastLine2DEntityHit() {
    // arrange
    ICollisionEntity sourceEntity = new CollisionBox(1, 3, 5, 5);
    Line2D line = new Line2D.Double(0, 10, 10, 0);
    ICollisionEntity targetEntity = new CollisionBox(8, 1, 5, 5);
    Game.physics().add(targetEntity);

    // act
    RaycastHit hit = Game.physics().raycast(line, sourceEntity);

    // assert
    assertEquals(8, hit.getPoint().getX());
    assertEquals(2, hit.getPoint().getY());
  }

  @Test
  void testRaycastLine2DEntityNoHit() {
    // arrange
    ICollisionEntity sourceEntity = new CollisionBox(1, 3, 5, 5);
    Line2D line = new Line2D.Double(0, 10, 10, 0);
    ICollisionEntity otherEntity = new CollisionBox(18, 1, 5, 5);
    Game.physics().add(otherEntity);

    // act
    RaycastHit hit = Game.physics().raycast(line, sourceEntity);

    // assert
    assertNull(hit);
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getRaycastCollisionArguments() {
    return Stream.of(
        Arguments.of(new Line2D.Double(3.5d, 3.5d, 10, 10), 7, 7),
        Arguments.of(new Line2D.Double(10, 10, 3.5, 3.5d), 8, 8));
  }
}
