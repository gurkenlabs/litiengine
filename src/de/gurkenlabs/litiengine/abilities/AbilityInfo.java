package de.gurkenlabs.litiengine.abilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This attribute provides initial values for entity attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AbilityInfo {

  CastType castType() default CastType.INSTANT;

  int cooldown() default 0;

  String description() default "";

  int duration() default 0;

  int impact() default 0;

  int impactAngle() default 360;

  boolean multiTarget() default false;

  String name() default "";

  AbilityOrigin origin() default AbilityOrigin.COLLISIONBOX_CENTER;

  int range() default 0;

  int value() default 0;
}
