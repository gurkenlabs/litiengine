package de.gurkenlabs.litiengine.util.geom;

/**
 * Provides fast, approximate trigonometric functions backed by precomputed look-up tables. Useful when many calls are made per frame and the slight
 * approximation error introduced by table look-ups is acceptable.
 */
public final class Trigonometry {
  private static final int ATAN2_BITS = 7;
  private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
  private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
  private static final int ATAN2_COUNT = ATAN2_MASK + 1;
  private static final float[] atan2 = new float[ATAN2_COUNT];
  private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

  private static final int SIN_BITS = 12;
  private static final int SIN_MASK = ~(-1 << SIN_BITS);
  private static final int SIN_COUNT = SIN_MASK + 1;

  private static final float[] cos = new float[SIN_COUNT];

  private static final float DEG = 180.0f / (float) Math.PI;

  private static final float DEG_FULL = (float) 360.0;
  private static final float DEG_TO_INDEX = SIN_COUNT / DEG_FULL;
  private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

  private static final float RAD_FULL = (float) (Math.PI * 2.0);
  private static final float RAD_TO_INDEX = SIN_COUNT / RAD_FULL;
  private static final float[] sin = new float[SIN_COUNT];

  static {
    for (int i = 0; i < ATAN2_DIM; i++) {
      for (int j = 0; j < ATAN2_DIM; j++) {
        final float x0 = (float) i / ATAN2_DIM;
        final float y0 = (float) j / ATAN2_DIM;

        atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
      }
    }

    for (int i = 0; i < SIN_COUNT; i++) {
      sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * RAD_FULL);
      cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * RAD_FULL);
    }
  }

  private Trigonometry() {
    throw new UnsupportedOperationException();
  }

  /**
   * Fast approximation of {@link Math#atan2(double, double)} in radians.
   *
   * @param y the y component
   * @param x the x component
   * @return the angle, in radians
   */
  public static float atan2(float y, float x) {
    float add;
    float mul;

    if (x < 0.0f) {
      if (y < 0.0f) {
        x = -x;
        y = -y;

        mul = 1.0f;
      } else {
        x = -x;
        mul = -1.0f;
      }

      add = -3.141592653f;
    } else {
      if (y < 0.0f) {
        y = -y;
        mul = -1.0f;
      } else {
        mul = 1.0f;
      }

      add = 0.0f;
    }

    final float invDiv = 1.0f / (Math.max(x, y) * INV_ATAN2_DIM_MINUS_1);

    final int xi = (int) (x * invDiv);
    final int yi = (int) (y * invDiv);

    return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
  }

  /**
   * Fast approximation of {@link Math#atan2(double, double)} in degrees.
   *
   * @param y the y component
   * @param x the x component
   * @return the angle, in degrees
   */
  public static float atan2Deg(final float y, final float x) {
    return atan2(y, x) * DEG;
  }

  /**
   * Strict (non-approximated) {@link Math#atan2(double, double)} in degrees.
   *
   * @param y the y component
   * @param x the x component
   * @return the angle, in degrees
   */
  public static float atan2DegStrict(final float y, final float x) {
    return (float) Math.atan2(y, x) * DEG;
  }

  /**
   * Fast approximation of {@link Math#cos(double)}.
   *
   * @param rad the angle, in radians
   * @return the cosine
   */
  public static final float cos(final float rad) {
    return cos[(int) (rad * RAD_TO_INDEX) & SIN_MASK];
  }

  /**
   * Fast approximation of {@link Math#cos(double)} accepting degrees.
   *
   * @param deg the angle, in degrees
   * @return the cosine
   */
  public static final float cosDeg(final float deg) {
    return cos[(int) (deg * DEG_TO_INDEX) & SIN_MASK];
  }

  /**
   * Fast approximation of {@link Math#sin(double)}.
   *
   * @param rad the angle, in radians
   * @return the sine
   */
  public static final float sin(final float rad) {
    return sin[(int) (rad * RAD_TO_INDEX) & SIN_MASK];
  }

  /**
   * Fast approximation of {@link Math#sin(double)} accepting degrees.
   *
   * @param deg the angle, in degrees
   * @return the sine
   */
  public static final float sinDeg(final float deg) {
    return sin[(int) (deg * DEG_TO_INDEX) & SIN_MASK];
  }
}
