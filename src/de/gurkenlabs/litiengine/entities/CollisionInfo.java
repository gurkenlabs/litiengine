package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.physics.Collision;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollisionInfo {
  Align align() default Align.CENTER;

  boolean collision();

  float collisionBoxHeight() default 0;

  float collisionBoxWidth() default 0;

  Valign valign() default Valign.DOWN;
  
  Collision collisionType() default Collision.DYNAMIC;
}
