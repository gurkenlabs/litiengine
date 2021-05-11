package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TextParticle;
import de.gurkenlabs.litiengine.physics.Collision;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ParticleTests {
  @BeforeAll
  public static void setup() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.terminateGame();
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
