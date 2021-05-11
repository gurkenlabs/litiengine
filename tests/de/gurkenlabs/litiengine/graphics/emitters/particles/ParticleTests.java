package de.gurkenlabs.litiengine.graphics.emitters.particles;

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
import de.gurkenlabs.litiengine.GameTime;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

public class ParticleTests {

  private Particle particle;

  @BeforeEach
  public void setUp() {
    // arrange
    particle = new TextParticle("test");
  }

  @Test
  public void testUpdate_hasCollision() {
    // arrange
    Particle testParticle = spy(particle);
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, atLeast(3))
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  public void testUpdate_ttlExpired() {
    // arrange
    Particle testParticle = spy(particle);
    when(testParticle.getAliveTime()).thenReturn(100000l);
    when(testParticle.getTimeToLive()).thenReturn(10);
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, never())
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  public void testUpdate_hasNoCollision() {
    // arrange
    Particle testParticle = spy(particle);
    testParticle.setX(10);
    testParticle.setY(42);
    testParticle.setVelocityX(5);
    testParticle.setVelocityY(10);
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // act
    testParticle.update(new Point2D.Double(1, 1), 1.2f);

    // assert
    verify(testParticle, atLeast(5))
        .applyUpdateRatioToMember(
            any(Supplier.class), any(Consumer.class), any(float.class), any(float.class));
  }

  @Test
  public void testUpdate_colliding() {
    // arrange
    Particle testParticle = spy(particle);
    PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
    GameTime gameTime = mock(GameTime.class);
    when(physicsEngine.collides(any(Line2D.class), any(Collision.class))).thenReturn(true);
    when(gameTime.now()).thenReturn(100000l);

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
  public void testApplyUpdateRatioToMember() {
    // act
    particle.applyUpdateRatioToMember(particle::getWidth, particle::setWidth, 1.2f, 1.0f);

    // assert
    assertEquals(2.2f, particle.getWidth());
  }

  @Test
  public void testHasRayCastCollision_hasHit() {
    // arrange
    particle.setX(1);
    particle.setY(1);

    // act
    boolean result = particle.hasRayCastCollision(new Point2D.Double(1, 1), 1.0f);

    // assert
    assertTrue(result);
  }

  @Test
  public void testHasRayCastCollision_hasCollision() {
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
  public void testHasRayCastCollision_hasNoCollision(float velocityX, float velocityY) {
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
}
