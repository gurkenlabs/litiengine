package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation contains movement-related properties for an entity implementation.
 * It can be used to specify default properties such as acceleration, deceleration,
 * whether the entity should turn while moving, and the velocity.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MovementInfo {

  /**
   * Specifies the default acceleration of the entity.
   * Defaults to 0.
   *
   * @return the default acceleration of the entity
   */
  int acceleration() default 0;

  /**
   * Specifies the default deceleration of the entity.
   * Defaults to 0.
   *
   * @return the default deceleration of the entity
   */
  int deceleration() default 0;

  /**
   * Specifies whether the entity should turn while moving.
   * Defaults to true.
   *
   * @return true if the entity should turn while moving, false otherwise
   */
  boolean turnOnMove() default true;

  /**
   * Specifies the default velocity of the entity in pixels per second.
   * Defaults to 100.
   *
   * @return the default velocity of the entity
   */
  float velocity() default 100;
}
