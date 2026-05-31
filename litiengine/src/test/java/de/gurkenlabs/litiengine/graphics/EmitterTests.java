package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.entities.EntityInfo;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.util.ColorHelper;
import java.awt.Color;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EmitterTests {

  @Test
  void testInitializationByAnnotation() {
    TestEmitter testEmitter = new TestEmitter();

    assertEquals(false, testEmitter.isActivateOnInit());
    assertEquals(false, testEmitter.isActivated());
    assertEquals(false, testEmitter.isPaused());

    assertEquals(2500, testEmitter.getTimeToLive());
    assertEquals(500, testEmitter.data().getMaxParticles());
    assertEquals(1000L, testEmitter.data().getParticleTTL().getMax().longValue());
    assertEquals(100L, testEmitter.data().getParticleTTL().getMin().longValue());

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
  void testRandomValueGeneration() {
    TestEmitter testEmitter = new TestEmitter();

    Color[] colors = new Color[] {Color.RED, Color.ORANGE, Color.YELLOW};
    testEmitter.data().setColors(colors);

    assertTrue(
        Arrays.asList(colors)
            .contains(ColorHelper.decode(Game.random().choose(testEmitter.data().getColors()))));
  }

  @Test
  void testRangeAttributeGetMin() {
    // when min > max, getRandomNumber should always return min
    RangeAttribute<Float> attr = new RangeAttribute<>();
    attr.setMax(1f);
    attr.setMin(10f);

    assertEquals(10f, attr.getRandomNumber().floatValue());
  }

  @Test
  void testRangeAttributeGetRandomBetweenMinAndMax() {
    RangeAttribute<Float> attr = new RangeAttribute<>(1f, 1f, 10f);

    Number random = attr.getRandomNumber();
    assertNotNull(random);
    assertTrue(random.doubleValue() >= 1f);
    assertTrue(random.doubleValue() < 10f);
  }

  @EmitterInfo(
      activateOnInit = false,
      duration = 2500,
      maxParticles = 500,
      originAlign = Align.CENTER,
      originValign = Valign.MIDDLE,
      particleMaxTTL = 1000,
      particleMinTTL = 100,
      particleUpdateRate = 10,
      requiredQuality = Quality.HIGH,
      spawnAmount = 15,
      spawnRate = 20)
  @EntityInfo(width = 50, height = 50)
  class TestEmitter extends Emitter {

    @Override
    protected Particle createNewParticle() {
      return null;
    }
  }
}
