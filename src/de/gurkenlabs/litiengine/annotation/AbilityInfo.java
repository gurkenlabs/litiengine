/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Auto-generated Javadoc
/**
 * This attribute provides initial values for entity attributes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AbilityInfo {

  /**
   * Cooldown.
   *
   * @return the int
   */
  int cooldown() default 0;

  /**
   * Value.
   *
   * @return the int
   */
  String description() default "";

  /**
   * Duration.
   *
   * @return the int
   */
  int duration() default 0;

  /**
   * Impact.
   *
   * @return the int
   */
  int impact() default 0;

  /**
   * Impact angle.
   *
   * @return the int
   */
  int impactAngle() default 360;

  /**
   * Multi target.
   *
   * @return true, if successful
   */
  boolean multiTarget() default false;

  /**
   * Ability type.
   *
   * @return the ability type
   */
  String name() default "";

  /**
   * Range.
   *
   * @return the int
   */
  int range() default 0;

  /**
   * Value.
   *
   * @return the int
   */
  int value() default 0;

}
