package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.physics.Collision;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The CollisionInfo annotation provides metadata for collision properties of an entity.
 * It can be applied to types (classes or interfaces) and is inherited by subclasses.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollisionInfo {
  /**
   * Specifies the horizontal alignment of the entity's collision box.
   * Defaults to {@link Align#CENTER}.
   *
   * @return the horizontal alignment of the collision box
   */
  Align align() default Align.CENTER;

  /**
   * Specifies whether the entity has collision enabled.
   *
   * @return true if collision is enabled, false otherwise
   */
  boolean collision();

  /**
   * Specifies the height of the entity's collision box.
   * Defaults to 0.
   *
   * @return the height of the collision box
   */
  float collisionBoxHeight() default -1;

  /**
   * Specifies the width of the entity's collision box.
   * Defaults to 0.
   *
   * @return the width of the collision box
   */
  float collisionBoxWidth() default -1;

  /**
   * Specifies the vertical alignment of the entity's collision box.
   * Defaults to {@link Valign#DOWN}.
   *
   * @return the vertical alignment of the collision box
   */
  Valign valign() default Valign.DOWN;

  /**
   * Specifies the type of collision for the entity.
   * Defaults to {@link Collision#DYNAMIC}.
   *
   * @return the type of collision
   */
  Collision collisionType() default Collision.DYNAMIC;
}
