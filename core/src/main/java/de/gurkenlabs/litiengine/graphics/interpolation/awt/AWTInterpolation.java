package de.gurkenlabs.litiengine.graphics.interpolation.awt;

import java.awt.geom.AffineTransform;

import de.gurkenlabs.litiengine.graphics.interpolation.AffineTransformOperation;
import de.gurkenlabs.litiengine.graphics.interpolation.Interpolation;

import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.AffineTransformOp.TYPE_BILINEAR;
import static java.awt.image.AffineTransformOp.TYPE_BICUBIC;

public final class AWTInterpolation extends Interpolation {

  private final int interpolationType;

  /*
   * Internal constructor, do not use.
   */
  @Deprecated
  public AWTInterpolation(final int interpolationType) {
    super(getName(interpolationType));
    switch (interpolationType) {
      case TYPE_NEAREST_NEIGHBOR:
      case TYPE_BILINEAR:
      case TYPE_BICUBIC:
        this.interpolationType = interpolationType;
        break;
      default:
        throw new AssertionError("Unknown AWT interpolation type: " + interpolationType);
    }
  }

  @Override
  public AffineTransformOperation<AWTInterpolation> of(AffineTransform tx) {
    return new AWTTransformOperation(tx, this);
  }

  private static final String getName(int interpolationType) {
    switch (interpolationType) {
      case TYPE_NEAREST_NEIGHBOR:
        return "NEAREST_NEIGBOR";
      case TYPE_BILINEAR:
        return "BILINIAR";
      case TYPE_BICUBIC:
        return "BICUBIC";
      default:
        throw new AssertionError("Unknown AWT interpolation type: " + interpolationType);
    }
  }

}
