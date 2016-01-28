package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollisionInfo {
  /**
   * Collision.
   *
   * @return true, if successful
   */
  boolean collision();

  /**
   * Collision box height factor.
   *
   * @return the float
   */
  float collisionBoxHeightFactor() default 0.4f;

  /**
   * Collision box width factor.
   *
   * @return the float
   */
  float collisionBoxWidthFactor() default 0.4f;
}
