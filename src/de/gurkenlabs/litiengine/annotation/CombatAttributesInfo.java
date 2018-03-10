package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This attribute provides initial values for combat entity attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CombatAttributesInfo {

  float attackSpeed() default 1.0f;

  float damageMultiplier() default 1.0f;

  int health() default 100;

  byte healthRegenerationPerSecond() default 1;

  byte level() default 1;

  int maxExperience() default 100;

  byte maxLevel() default 20;

  short maxShield() default 0;

  short shield() default 0;

  float velocityFactor() default 1.0f;

  int vision() default 100;
}
