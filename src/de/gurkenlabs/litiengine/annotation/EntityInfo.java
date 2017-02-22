/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * This annotation contains default values for an entity implementation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EntityInfo {

  /**
   * Height.
   *
   * @return the int
   */
  float height() default 32;

  RenderType renderType() default RenderType.NORMAL;

  /**
   * Width.
   *
   * @return the int
   */
  float width() default 32;
}
