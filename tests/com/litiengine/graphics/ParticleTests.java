package com.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;

import com.litiengine.Game;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.litiengine.GameTest;
import com.litiengine.graphics.emitters.particles.LineParticle;
import com.litiengine.graphics.emitters.particles.RectangleParticle;
import com.litiengine.graphics.emitters.particles.TextParticle;
import com.litiengine.physics.Collision;

public class ParticleTests {
  @BeforeAll
  public static void setup() {
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.resetGame();
  }

  @Test
  public void initializeParticleTypes() {
    assertDoesNotThrow(() -> new RectangleParticle(10, 10));
    assertDoesNotThrow(() -> new LineParticle(10, 10));
    assertDoesNotThrow(() -> new TextParticle(null));
    assertDoesNotThrow(() -> new TextParticle("test"));
  }

  @Test
  public void testDeltaSize() {
    RectangleParticle part = new RectangleParticle(10, 10);
    part.setCollisionType(Collision.NONE);
    part.setDeltaHeight(0.1f);
    part.setDeltaWidth(0.1f);

    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(10.1f, part.getWidth());
    assertEquals(10.1f, part.getHeight());
  }

  @Test
  public void testDeltaLocation() {
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
