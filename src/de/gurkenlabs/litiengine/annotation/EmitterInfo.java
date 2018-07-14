package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;

/**
 * This annotation contains default values for the initialization of an emitter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EmitterInfo {

  boolean activateOnInit() default true;

  int emitterTTL() default 0;

  int maxParticles() default Emitter.DEFAULT_MAXPARTICLES;

  boolean particleFade() default true;

  int particleMaxTTL() default 0;

  int particleMinTTL() default 0;

  int particleUpdateRate() default Emitter.DEFAULT_UPDATERATE;

  int spawnAmount() default Emitter.DEFAULT_SPAWNAMOUNT;

  int spawnRate() default 0;

  Align originAlign() default Align.LEFT;

  Valign originVAlign() default Valign.TOP;

  Quality requiredQuality() default Quality.VERYLOW;
}
