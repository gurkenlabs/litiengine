package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameInfo {
  public String cooperation() default "";

  public String description() default "";

  public String[] developers() default {};

  public String name();

  public String subTitle() default "";

  public float version();

  public String icon() default "";

  public String logo() default "";
}
