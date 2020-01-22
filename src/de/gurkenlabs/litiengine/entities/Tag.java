package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This attribute provides initial values for Entity tags. Annotate an Entity with one or multiple Tags to add them automatically when the constructor
 * is called.
 * 
 * @see IEntity#addTag(String)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(Tags.class)
public @interface Tag {
  String value();
}