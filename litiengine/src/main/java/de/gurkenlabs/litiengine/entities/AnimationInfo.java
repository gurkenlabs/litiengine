package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The AnimationInfo annotation provides metadata for animation properties of an entity.
 * It can be applied to types (classes or interfaces) and is inherited by subclasses.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AnimationInfo {
  /**
   * Specifies the prefixes for the sprite animations.
   *
   * @return an array of sprite prefixes
   */
  String[] spritePrefix();

  /**
   * Specifies the animations to be used when the entity dies.
   * Defaults to an empty array.
   *
   * @return an array of death animation names
   */
  String[] deathAnimations() default {};
}
