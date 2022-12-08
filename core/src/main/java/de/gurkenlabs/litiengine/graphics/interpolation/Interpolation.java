package de.gurkenlabs.litiengine.graphics.interpolation;

import java.awt.geom.AffineTransform;

@FunctionalInterface
public interface Interpolation {

  public abstract AffineTransformOperation<? extends Interpolation> of(AffineTransform tx);

}
