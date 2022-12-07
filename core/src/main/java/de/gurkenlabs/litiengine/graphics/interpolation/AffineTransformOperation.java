package de.gurkenlabs.litiengine.graphics.interpolation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImageOp;
import java.awt.image.RasterOp;

public abstract class AffineTransformOperation<T extends Interpolation> implements BufferedImageOp, RasterOp {

  private final T interpolation;

  @SuppressWarnings("unchecked")
  public AffineTransformOperation(AffineTransform tx, final Interpolation interpolation) {
    this.interpolation = (T) interpolation;
  }

  public final int getInterpolationType() {
    return interpolation.getInterpolationType();
  }

  public final T getInterpolation() {
    return interpolation;
  }

}
