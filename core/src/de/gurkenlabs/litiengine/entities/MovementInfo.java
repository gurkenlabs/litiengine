package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MovementInfo {

  int acceleration() default 0;

  int deceleration() default 0;

  boolean turnOnMove() default true;

  /**
   * The velocity in pixels per second.
   *
   * @return the velocity
   */
  float velocity() default 100;
}
