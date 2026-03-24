package de.gurkenlabs.litiengine.sound.spi.mp3;

public class WindowFunctions {

  private static final float[] LONG_WINDOW;
  private static final float[] SHORT_WINDOW;
  private static final float[] START_WINDOW;
  private static final float[] END_WINDOW;

  static {
    LONG_WINDOW = createLongWindow();
    SHORT_WINDOW = createShortWindow();
    START_WINDOW = createStartWindow();
    END_WINDOW = createEndWindow();
  }

  private static float[] createLongWindow() {
    float[] window = new float[36];
    for (int i = 0; i < 36; i++) {
      window[i] = (float) Math.sin(Math.PI / 36 * (i + 0.5));
    }
    return window;
  }

  private static float[] createShortWindow() {
    float[] window = new float[12];
    for (int i = 0; i < 12; i++) {
      window[i] = (float) Math.sin(Math.PI / 12 * (i + 0.5));
    }
    return window;
  }

  private static float[] createStartWindow() {
    float[] window = new float[36];
    for (int i = 0; i < 36; i++) {
      if (i < 18) {
        window[i] = (float) Math.sin(Math.PI / 36 * (i + 0.5));
      } else {
        window[i] = 1.0f;
      }
    }
    return window;
  }

  private static float[] createEndWindow() {
    float[] window = new float[36];
    for (int i = 0; i < 36; i++) {
      if (i < 18) {
        window[i] = 1.0f;
      } else {
        window[i] = (float) Math.sin(Math.PI / 36 * (i - 18 + 0.5));
      }
    }
    return window;
  }

  public static float[] getLongWindow() {
    return LONG_WINDOW.clone();
  }

  public static float[] getShortWindow() {
    return SHORT_WINDOW.clone();
  }

  public static float[] getStartWindow() {
    return START_WINDOW.clone();
  }

  public static float[] getEndWindow() {
    return END_WINDOW.clone();
  }

  public static float[] getWindow(int blockType) {
    return switch (blockType) {
      case 0 -> getLongWindow();
      case 1 -> getStartWindow();
      case 2 -> getShortWindow();
      case 3 -> getEndWindow();
      default -> getLongWindow();
    };
  }

  public static float[] applyWindow(float[] data, int blockType) {
    float[] window = getWindow(blockType);
    float[] result = new float[data.length];

    if (blockType == 2) { // SHORT block
      // data has 36 samples (3 windows of 12 samples each)
      // window has 12 samples
      for (int w = 0; w < 3; w++) {
        int offset = w * 12;
        for (int i = 0; i < 12; i++) {
          result[offset + i] = data[offset + i] * window[i];
        }
      }
    } else { // LONG block
      int len = Math.min(data.length, window.length);
      for (int i = 0; i < len; i++) {
        result[i] = data[i] * window[i];
      }
    }

    return result;
  }
}
