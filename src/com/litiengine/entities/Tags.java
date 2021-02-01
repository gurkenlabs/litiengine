package com.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This attribute provides initial values for Entity tags. Annotate an Entity with one or multiple Tags to add them automatically when the constructor
 * is called. {@code Tags} is just intended as a container class, use the repeatable {@code Tag} annotation instead.
 * 
 * @see IEntity#addTag(String)
 * @see Tag
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Tags {
  Tag[] value() default {};
}
