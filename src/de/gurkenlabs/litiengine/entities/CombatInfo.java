package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This attribute provides initial values for combat entity attributes. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CombatInfo {
  int hitpoints() default CombatEntity.DEFAULT_HITPOINTS;

  int team() default 0;

  boolean isIndestructible() default false;
}
