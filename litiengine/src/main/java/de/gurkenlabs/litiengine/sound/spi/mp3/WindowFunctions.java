package de.gurkenlabs.litiengine.sound.spi.mp3;

final class WindowFunctions {
  private static final float[][] WINDOWS = createWindows();

  private WindowFunctions() {}

  private static float[][] createWindows() {
    float[][] windows = new float[4][36];
    for (int i = 0; i < 36; i++) windows[0][i] = (float) Math.sin(Math.PI / 36 * (i + 0.5));

    System.arraycopy(windows[0], 0, windows[1], 0, 18);
    for (int i = 18; i < 24; i++) windows[1][i] = 1;
    for (int i = 24; i < 30; i++) windows[1][i] = (float) Math.sin(Math.PI / 12 * (i - 18 + 0.5));

    for (int i = 6; i < 12; i++) windows[3][i] = (float) Math.sin(Math.PI / 12 * (i - 6 + 0.5));
    for (int i = 12; i < 18; i++) windows[3][i] = 1;
    System.arraycopy(windows[0], 18, windows[3], 18, 18);
    return windows;
  }

  static float[] getWindow(int blockType) {
    if (blockType == 2) return getShortWindow();
    if (blockType < 0 || blockType >= WINDOWS.length) return getLongWindow();
    return WINDOWS[blockType].clone();
  }

  static float[] getLongWindow() { return WINDOWS[0].clone(); }

  static float[] getStartWindow() { return WINDOWS[1].clone(); }

  static float[] getEndWindow() { return WINDOWS[3].clone(); }

  static float[] getShortWindow() {
    float[] window = new float[12];
    for (int i = 0; i < window.length; i++) window[i] = (float) Math.sin(Math.PI / 12 * (i + 0.5));
    return window;
  }

  static float[] applyWindow(float[] data, int blockType) {
    float[] result = data.clone();
    float[] window = getWindow(blockType);
    for (int i = 0; i < Math.min(result.length, window.length); i++) result[i] *= window[i];
    return result;
  }
}
