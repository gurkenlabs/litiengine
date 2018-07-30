package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;

public class EmitterTests {

  @Test
  public void testInitializationByAnnotation() {
    TestEmitter testEmitter = new TestEmitter();
    
    assertEquals(false, testEmitter.isActivateOnInit());
    assertEquals(false, testEmitter.isActivated());
    assertEquals(false, testEmitter.isPaused());
    
    assertEquals(2500, testEmitter.getTimeToLive());
    assertEquals(500, testEmitter.getMaxParticles());
    assertEquals(1000, testEmitter.getParticleMaxTTL());
    assertEquals(100, testEmitter.getParticleMinTTL());
    assertEquals(10, testEmitter.getParticleUpdateRate());
    assertEquals(Quality.HIGH, testEmitter.getRequiredQuality());
    assertEquals(15, testEmitter.getSpawnAmount());
    assertEquals(20, testEmitter.getSpawnRate());
    assertEquals(50, testEmitter.getWidth());
    assertEquals(50, testEmitter.getHeight());

    assertEquals(Align.CENTER, testEmitter.getOriginAlign());
    assertEquals(Valign.MIDDLE, testEmitter.getOriginValign());
    assertEquals(25, testEmitter.getOrigin().getX());
    assertEquals(25, testEmitter.getOrigin().getY());
  }
  
  @EmitterInfo(
      activateOnInit = false,
      emitterTTL = 2500,
      maxParticles = 500,
      originAlign = Align.CENTER,
      originVAlign = Valign.MIDDLE,
      particleMaxTTL = 1000,
      particleMinTTL = 100,
      particleUpdateRate = 10,
      requiredQuality = Quality.HIGH,
      spawnAmount = 15,
      spawnRate = 20)
  @EntityInfo(width = 50, height = 50)
  class TestEmitter extends Emitter{

    @Override
    protected Particle createNewParticle() {
      return null;
    }
  }
}
