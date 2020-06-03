package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LightParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TextParticle;
import de.gurkenlabs.litiengine.physics.Collision;

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
    assertDoesNotThrow(() -> new LeftLineParticle(10, 10, Color.WHITE, 300));
    assertDoesNotThrow(() -> new LightParticle(100, 100, 10, 10, Color.WHITE, 300));
    assertDoesNotThrow(() -> new RectangleParticle(10, 10, Color.WHITE, 300));
    assertDoesNotThrow(() -> new RightLineParticle(10, 10, Color.WHITE, 300));
    assertDoesNotThrow(() -> new TextParticle(null, Color.WHITE, 300));
    assertDoesNotThrow(() -> new TextParticle("test", Color.WHITE, 300));
  }

  @Test
  public void testDeltaSize() {
    RectangleParticle part = new RectangleParticle(10, 10, Color.WHITE, 300);
    part.setCollisionType(Collision.NONE);
    part.setDeltaHeight(0.1f);
    part.setDeltaWidth(0.1f);

    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(10.1f, part.getWidth());
    assertEquals(10.1f, part.getHeight());
  }

  @Test
  public void testDeltaLocation() {
    RectangleParticle part = new RectangleParticle(10, 10, Color.WHITE, 300);
    part.setCollisionType(Collision.NONE);
    part.setDeltaX(0.1f);
    part.setDeltaY(0.1f);
    part.setDeltaIncX(0.01f);
    part.setDeltaIncY(0.01f);
    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(0.1f, part.getX());
    assertEquals(0.1f, part.getY());

    part.update(new Point2D.Double(0, 0), 1);

    assertEquals(0.21f, part.getX(), 0.0001);
    assertEquals(0.21f, part.getY(), 0.0001);
  }
}
