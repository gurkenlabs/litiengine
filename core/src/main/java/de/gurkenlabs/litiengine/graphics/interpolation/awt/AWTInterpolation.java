package de.gurkenlabs.litiengine.graphics.interpolation.awt;

import java.awt.geom.AffineTransform;

import de.gurkenlabs.litiengine.graphics.interpolation.AffineTransformOperation;
import de.gurkenlabs.litiengine.graphics.interpolation.Interpolation;

import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.AffineTransformOp.TYPE_BILINEAR;
import static java.awt.image.AffineTransformOp.TYPE_BICUBIC;

public enum AWTInterpolation implements Interpolation {
  NEAREST_NEIGHBOR(TYPE_NEAREST_NEIGHBOR),
  BILINEAR(TYPE_BILINEAR),
  BICUBIC(TYPE_BICUBIC);

  private final int type;

  private AWTInterpolation(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  @Override
  public AffineTransformOperation<? extends Interpolation> of(AffineTransform tx) {
    return new AWTTransformOperation(tx, this);
  }
}
