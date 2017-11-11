package de.gurkenlabs.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface ConfigurationGroupInfo.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ConfigurationGroupInfo {

  /**
   * Prefix.
   *
   * @return the string
   */
  String prefix() default "";

  boolean debug() default false;
}
