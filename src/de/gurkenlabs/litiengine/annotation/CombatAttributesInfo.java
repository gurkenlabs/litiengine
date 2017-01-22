/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
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

  /**
   * Attack speed.
   *
   * @return the float
   */
  float attackSpeed() default 1.0f;

  /**
   * Damage multiplier.
   *
   * @return the float
   */
  float damageMultiplier() default 1.0f;

  /**
   * Health.
   *
   * @return the int
   */
  short health() default 100;

  /**
   * Health regeneration per second.
   *
   * @return the int
   */
  byte healthRegenerationPerSecond() default 1;

  /**
   * Level.
   *
   * @return the int
   */
  byte level() default 1;

  /**
   * Max experience.
   *
   * @return the int
   */
  int maxExperience() default 100;

  /**
   * Max level.
   *
   * @return the int
   */
  byte maxLevel() default 20;

  /**
   * Max shield.
   *
   * @return the int
   */
  short maxShield() default 0;

  /**
   * Shield.
   *
   * @return the int
   */
  short shield() default 0;

  /**
   * Velocity factor.
   *
   * @return the float
   */
  float velocityFactor() default 1.0f;
  
  int vision() default 100;
}
