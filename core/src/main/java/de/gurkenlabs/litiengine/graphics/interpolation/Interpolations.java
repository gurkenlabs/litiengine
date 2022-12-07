package de.gurkenlabs.litiengine.graphics.interpolation;

import java.util.concurrent.CopyOnWriteArrayList;

import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.AffineTransformOp.TYPE_BILINEAR;
import static java.awt.image.AffineTransformOp.TYPE_BICUBIC;

import de.gurkenlabs.litiengine.graphics.interpolation.awt.AWTInterpolation;

public final class Interpolations {

  public static final AWTInterpolation NEAREST_NEIGHBOR;
  public static final AWTInterpolation BILINEAR;
  public static final AWTInterpolation BICUBIC;

  private static final CopyOnWriteArrayList<Interpolation> INTERPOLATIONS = new CopyOnWriteArrayList<Interpolation>();
  static {
    registerDefaultInterpolations();
    NEAREST_NEIGHBOR = getInterpolation(TYPE_NEAREST_NEIGHBOR, AWTInterpolation.class);
    BILINEAR = getInterpolation(TYPE_BILINEAR, AWTInterpolation.class);
    BICUBIC = getInterpolation(TYPE_BICUBIC, AWTInterpolation.class);
    if (INTERPOLATIONS.size() != 3) {
      throw new AssertionError("Size is not 3");
    }
    if (NEAREST_NEIGHBOR.getInterpolationType() != 1) {
      throw new AssertionError("Nearest neighbor is not 1");
    }
    if (BILINEAR.getInterpolationType() != 2) {
      throw new AssertionError("Bilinear is not 2");
    }
    if (BICUBIC.getInterpolationType() != 3) {
      throw new AssertionError("Bicubic is not 3");
    }
  }


  private Interpolations() {
    throw new AssertionError();
  }

  public static int indexOf(Interpolation interpolation) {
    return INTERPOLATIONS.indexOf(interpolation);
  }

  public static Interpolation getInterpolation(int type) {
    return INTERPOLATIONS.get(type);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Interpolation> T getInterpolation(int type, Class<T> as) {
    return (T) INTERPOLATIONS.get(type);
  }

  /**
   * @param interpolation
   * @return
   */
  @SuppressWarnings("null")
  private static final int registerTransformer(Interpolation interpolation) {
    if (INTERPOLATIONS.contains(interpolation)) {
      throw new IllegalStateException("Interpolation transformer already registered");
    }
    if (interpolation == null) {
      interpolation.equals(null); // throw a helpful NPE (JPE 358);
    }

    INTERPOLATIONS.add(interpolation);
    return INTERPOLATIONS.indexOf(interpolation);
  }

  private static final void registerDefaultInterpolations() {
    if (INTERPOLATIONS.size() > 0) {
      throw new AssertionError(new IllegalStateException("Default interpolations already registered!"));
    }
  }

}
