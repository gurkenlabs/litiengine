package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.entities.EntityInfo;
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
    assertTrue(testEmitter.getRandomParticleTTL() >= 100);
    assertTrue(testEmitter.getRandomParticleTTL() <= 1000);
    
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
  
  @Test
  public void testRandomValueGeneration() {
    TestEmitter testEmitter = new TestEmitter();
    assertTrue(testEmitter.getRandomParticleTTL() >= 100);
    assertTrue(testEmitter.getRandomParticleTTL() <= 1000);
    
    Color [] colors = new Color [] {Color.RED, Color.ORANGE, Color.YELLOW};
    testEmitter.setColors(colors);
    
    assertTrue(Arrays.asList(colors).contains(testEmitter.getRandomParticleColor()));
  }
  
  @EmitterInfo(
      activateOnInit = false,
      duration = 2500,
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
    
    @Override
    public int getRandomParticleTTL() {
      return super.getRandomParticleTTL();
    }
    
    @Override
    public Color getRandomParticleColor() {
      return super.getRandomParticleColor();
    }
  }
}
