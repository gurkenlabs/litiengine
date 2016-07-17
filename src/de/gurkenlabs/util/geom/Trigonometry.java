package de.gurkenlabs.util.geom;

public class Trigonometry {
  private static final int ATAN2_BITS = 7;

  private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
  private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
  private static final int ATAN2_COUNT = ATAN2_MASK + 1;
  private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

  private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
  private static final float DEG = 180.0f / (float) Math.PI;

  private static final float[] atan2 = new float[ATAN2_COUNT];

  private static final float RAD = (float) Math.PI / 180.0F;

  private static final int SIN_BITS = 12;
  private static final int SIN_MASK = ~(-1 << SIN_BITS);
  private static final int SIN_COUNT = SIN_MASK + 1;

  private static final float radFull = (float) (Math.PI * 2.0);
  private static final float degFull = (float) 360.0;
  private static final float radToIndex = SIN_COUNT / radFull;
  private static final float degToIndex = SIN_COUNT / degFull;

  private static final float[] sin = new float[SIN_COUNT];
  private static final float[] cos = new float[SIN_COUNT];

  static {
    for (int i = 0; i < ATAN2_DIM; i++) {
      for (int j = 0; j < ATAN2_DIM; j++) {
        final float x0 = (float) i / ATAN2_DIM;
        final float y0 = (float) j / ATAN2_DIM;

        atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
      }
    }

    for (int i = 0; i < SIN_COUNT; i++) {
      sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
      cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
    }
  }

  /**
   * ATAN2
   */
  public static final float atan2Deg(final float y, final float x) {
    return atan2(y, x) * DEG;
  }

  public static final float atan2DegStrict(final float y, final float x) {
    return (float) Math.atan2(y, x) * DEG;
  }

  public static final float atan2(float y, float x) {
    float add, mul;

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

    final float invDiv = 1.0f / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);

    final int xi = (int) (x * invDiv);
    final int yi = (int) (y * invDiv);
    
    return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
  }

  /**
   * SIN / COS (RAD)
   */
  public static final float sin(final float rad) {
    return sin[(int) (rad * radToIndex) & SIN_MASK];
  }

  public static final float cos(final float rad) {
    return cos[(int) (rad * radToIndex) & SIN_MASK];
  }

  /**
   * SIN / COS (DEG)
   */
  public static final float sinDeg(final float deg) {
    return sin[(int) (deg * degToIndex) & SIN_MASK];
  }

  public static final float cosDeg(final float deg) {
    return cos[(int) (deg * degToIndex) & SIN_MASK];
  }
}
