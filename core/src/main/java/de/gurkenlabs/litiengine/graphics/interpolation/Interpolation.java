package de.gurkenlabs.litiengine.graphics.interpolation;

import java.awt.geom.AffineTransform;

public abstract class Interpolation {

  private final String name;

  public Interpolation(String name) {
    this.name = name;
  }

  public abstract AffineTransformOperation of(AffineTransform tx);

  public final String name() {
    return name;
  }

  @Override
  public final boolean equals(Object o) {
    if (o instanceof Interpolation) {
      return name().equals(((Interpolation) o).name());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public final int getInterpolationType() {
    return Interpolations.indexOf(this);
  }

}
