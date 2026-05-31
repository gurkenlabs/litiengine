package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation contains default values for the initialization of an emitter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EmitterInfo {

  /**
   * Indicates whether the emitter should be activated on initialization.
   *
   * @return true if the emitter should be activated on initialization, false otherwise.
   */
  boolean activateOnInit() default true;

  /**
   * Specifies the duration of the emitter.
   *
   * @return the duration of the emitter in milliseconds.
   */
  int duration() default EmitterAttributes.DEFAULT_DURATION;

  /**
   * Specifies the maximum number of particles the emitter can have.
   *
   * @return the maximum number of particles.
   */
  int maxParticles() default EmitterAttributes.DEFAULT_MAXPARTICLES;

  /**
   * Specifies the maximum time-to-live (TTL) of particles.
   *
   * @return the maximum TTL of particles in milliseconds.
   */
  int particleMaxTTL() default EmitterAttributes.DEFAULT_MAX_PARTICLE_TTL;

  /**
   * Specifies the minimum time-to-live (TTL) of particles.
   *
   * @return the minimum TTL of particles in milliseconds.
   */
  int particleMinTTL() default EmitterAttributes.DEFAULT_MIN_PARTICLE_TTL;

  /**
   * Specifies the update rate of particles.
   *
   * @return the update rate of particles in milliseconds.
   */
  int particleUpdateRate() default EmitterAttributes.DEFAULT_UPDATERATE;

  /**
   * Specifies the amount of particles to spawn.
   *
   * @return the amount of particles to spawn.
   */
  int spawnAmount() default EmitterAttributes.DEFAULT_SPAWNAMOUNT;

  /**
   * Specifies the rate at which particles are spawned.
   *
   * @return the spawn rate of particles in milliseconds.
   */
  int spawnRate() default EmitterAttributes.DEFAULT_SPAWNRATE;

  /**
   * Specifies the horizontal alignment of the emitter's origin.
   *
   * @return the horizontal alignment of the emitter's origin.
   */
  Align originAlign() default Align.CENTER;

  /**
   * Specifies the vertical alignment of the emitter's origin.
   *
   * @return the vertical alignment of the emitter's origin.
   */
  Valign originValign() default Valign.MIDDLE;

  /**
   * Specifies the required quality level for the emitter.
   *
   * @return the required quality level.
   */
  Quality requiredQuality() default Quality.VERYLOW;

  /**
   * Specifies the type of particles emitted.
   *
   * @return the type of particles emitted.
   */
  ParticleType particleType() default ParticleType.RECTANGLE;
}
