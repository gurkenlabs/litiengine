package com.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.litiengine.configuration.Quality;
import com.litiengine.graphics.emitters.particles.ParticleType;
import com.litiengine.graphics.emitters.xml.EmitterData;
import com.litiengine.Align;
import com.litiengine.Valign;

/**
 * This annotation contains default values for the initialization of an emitter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EmitterInfo {

  boolean activateOnInit() default true;

  int duration() default EmitterData.DEFAULT_DURATION;

  int maxParticles() default EmitterData.DEFAULT_MAXPARTICLES;

  int particleMaxTTL() default EmitterData.DEFAULT_MAX_PARTICLE_TTL;

  int particleMinTTL() default EmitterData.DEFAULT_MIN_PARTICLE_TTL;

  int particleUpdateRate() default EmitterData.DEFAULT_UPDATERATE;

  int spawnAmount() default EmitterData.DEFAULT_SPAWNAMOUNT;

  int spawnRate() default EmitterData.DEFAULT_SPAWNRATE;

  Align originAlign() default Align.CENTER;

  Valign originValign() default Valign.MIDDLE;

  Quality requiredQuality() default Quality.VERYLOW;

  ParticleType particleType() default ParticleType.RECTANGLE;
}
