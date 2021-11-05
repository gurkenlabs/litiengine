package de.gurkenlabs.litiengine.graphics.emitters.particles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.GameTime;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

class ParticleTests {

  private Particle particle;

  @BeforeAll
  public static void setup() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.terminateGame();
  }

  @BeforeEach
  public void setUp() {
    // arrange
    particle = new TextParticle("test");
  }

  @Test
  void initializeParticleTypes() {
    assertDoesNotThrow(() -> new RectangleParticle(10, 10));
    assertDoesNotThrow(() -> new LineParticle(10, 10));
    assertDoesNotThrow(() -> new TextParticle(null));
    assertDoesNotThrow(() -> new TextParticle("test"));
  }

  @Test
  void testUpdate_hasCollision() {
    // arrange
    Particle testParticle = spy(particle);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, atLeast(3))
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  void testUpdate_ttlExpired() {
    // arrange
    Particle testParticle = spy(particle);
    when(testParticle.getAliveTime()).thenReturn(100000L);
    when(testParticle.getTimeToLive()).thenReturn(10);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, never())
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  void testUpdate_hasNoCollision() {
    // arrange
    Particle testParticle = spy(particle);
    testParticle.setX(10);
    testParticle.setY(42);
    testParticle.setVelocityX(5);
    testParticle.setVelocityY(10);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, atLeast(5))
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  void testUpdate_colliding() {
    // arrange
    Particle testParticle = spy(particle);
    PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
    GameTime gameTime = mock(GameTime.class);
    when(physicsEngine.collides(any(Line2D.class), any(Collision.class))).thenReturn(true);
    when(gameTime.now()).thenReturn(100000L);

    try (MockedStatic<Game> gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::physics).thenReturn(physicsEngine);
      gameMockedStatic.when(Game::time).thenReturn(gameTime);
      testParticle.setVelocityX(5);
      testParticle.setVelocityY(5);
      testParticle.setContinuousCollision(true);
      testParticle.setCollisionType(Collision.ANY);

      // act
      testParticle.update(new Point2D.Double(1, 1), 1.2f);
      testParticle.update(new Point2D.Double(1, 1), 1.2f);

      // assert
      verify(testParticle, atMost(5))
          .applyUpdateRatioToMember(
              any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
    }
  }

  @Test
  void testApplyUpdateRatioToMember() {
    // act
    particle.applyUpdateRatioToMember(particle::getWidth, particle::setWidth, 1.2f, 1.0f);

    // assert
    assertEquals(2.2f, particle.getWidth());
  }

  @Test
  void testHasRayCastCollision_hasHit() {
    // arrange
    particle.setX(1);
    particle.setY(1);

    // act
    boolean result = particle.hasRayCastCollision(new Point2D.Double(1, 1), 1.0f);

    // assert
    assertTrue(result);
  }

  @Test
  void testHasRayCastCollision_hasCollision() {
    // arrange
    PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
    when(physicsEngine.collides(any(Line2D.class), any(Collision.class))).thenReturn(true);

    try (MockedStatic<Game> gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::physics).thenReturn(physicsEngine);
      particle.setX(10);
      particle.setY(42);
      particle.setVelocityX(5);
      particle.setVelocityY(5);
      particle.setContinuousCollision(true);
      particle.setCollisionType(Collision.ANY);

      // act
      boolean result = particle.hasRayCastCollision(new Point2D.Double(42, 42), 1f);

      // assert
      assertTrue(result);
    }
  }

  @ParameterizedTest(name = "testHasRayCastCollision_hasNoCollision velocityX={0}, velocityY={1}")
  @CsvSource({"5, 5", "0, 5", "5, 0"})
  void testHasRayCastCollision_hasNoCollision(float velocityX, float velocityY) {
    // arrange
    particle.setX(10);
    particle.setY(42);
    particle.setVelocityX(velocityX);
    particle.setVelocityY(velocityY);

    // act
    boolean result = particle.hasRayCastCollision(new Point2D.Double(42, 42), 1f);

    // assert
    assertFalse(result);
  }

  @Test
  void testDeltaSize() {
    RectangleParticle part = new RectangleParticle(10, 10);
    part.setCollisionType(Collision.NONE);
    part.setDeltaHeight(0.1f);
    part.setDeltaWidth(0.1f);

    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(10.1f, part.getWidth());
    assertEquals(10.1f, part.getHeight());
  }

  @Test
  void testDeltaLocation() {
    RectangleParticle part = new RectangleParticle(10, 10);
    part.setCollisionType(Collision.NONE);
    part.setVelocityX(0.1f);
    part.setVelocityY(0.1f);
    part.setAccelerationX(0.01f);
    part.setAccelerationY(0.01f);
    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(0.1f, part.getX());
    assertEquals(0.1f, part.getY());

    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(0.21f, part.getX(), 0.0001);
    assertEquals(0.21f, part.getY(), 0.0001);
  }
}
