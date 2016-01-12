package de.gurkenlabs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameInfo {
  public String getName();

  public float getVersion();

  public String getSubTitle() default "";

  public String getDescription() default "";

  public String getCooperation() default "";

  public String[] getDevelopers() default {};
}
