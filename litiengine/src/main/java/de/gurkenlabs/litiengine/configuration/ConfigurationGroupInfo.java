package de.gurkenlabs.litiengine.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide metadata for configuration groups. This annotation can be used to specify a prefix and debug mode for configuration groups.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ConfigurationGroupInfo {

  /**
   * Specifies the prefix for the configuration group.
   *
   * @return the prefix for the configuration group.
   */
  String prefix() default "";

  /**
   * Specifies whether debug mode is enabled for the configuration group.
   *
   * @return true if debug mode is enabled, false otherwise.
   */
  boolean debug() default false;
}
