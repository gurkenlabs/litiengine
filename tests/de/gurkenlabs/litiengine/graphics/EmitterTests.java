package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.entities.EntityInfo;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.util.ColorHelper;

public class EmitterTests {

  @Test
  public void testInitializationByAnnotation() {
    TestEmitter testEmitter = new TestEmitter();

    assertEquals(false, testEmitter.isActivateOnInit());
    assertEquals(false, testEmitter.isActivated());
    assertEquals(false, testEmitter.isPaused());

    assertEquals(2500, testEmitter.getTimeToLive());
    assertEquals(500, testEmitter.data().getMaxParticles());
    assertEquals(1000, testEmitter.data().getParticleTTL().getMaxValue());
    assertEquals(100, testEmitter.data().getParticleTTL().getMinValue());

    assertEquals(10, testEmitter.data().getUpdateRate());
    assertEquals(Quality.HIGH, testEmitter.data().getRequiredQuality());
    assertEquals(15, testEmitter.data().getSpawnAmount());
    assertEquals(20, testEmitter.data().getSpawnRate());
    assertEquals(50, testEmitter.getWidth());
    assertEquals(50, testEmitter.getHeight());

    assertEquals(Align.CENTER, testEmitter.data().getOriginAlign());
    assertEquals(Valign.MIDDLE, testEmitter.data().getOriginValign());
    assertEquals(25, testEmitter.getOrigin().getX());
    assertEquals(25, testEmitter.getOrigin().getY());
  }

  @Test
  public void testRandomValueGeneration() {
    TestEmitter testEmitter = new TestEmitter();

    Color[] colors = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW };
    testEmitter.data().setColors(colors);

    assertTrue(Arrays.asList(colors).contains(ColorHelper.decode(Game.random().choose(testEmitter.data().getColors()))));
  }

  @EmitterInfo(activateOnInit = false, duration = 2500, maxParticles = 500, originAlign = Align.CENTER, originValign = Valign.MIDDLE, particleMaxTTL = 1000, particleMinTTL = 100, particleUpdateRate = 10, requiredQuality = Quality.HIGH, spawnAmount = 15, spawnRate = 20)
  @EntityInfo(width = 50, height = 50)
  class TestEmitter extends Emitter {

    @Override
    protected Particle createNewParticle() {
      return null;
    }
  }
}
