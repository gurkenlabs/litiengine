package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.gurkenlabs.tiled.tmx.MapOrientation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameInfo {
  public String cooperation() default "";

  public String description() default "";

  public String[] developers() default {};

  public String icon() default "";

  public String logo() default "";

  public String name();

  public MapOrientation orientation() default MapOrientation.orthogonal;

  public float renderScale() default 3.0f;

  public String subTitle() default "";

  public float version();
}
