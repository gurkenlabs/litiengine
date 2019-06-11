package de.gurkenlabs.utiliti.handlers;

import de.gurkenlabs.litiengine.Game;

public class Zoom {
  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private static final int DEFAULT_ZOOM_INDEX = 3;
  private static int currentZoomIndex = DEFAULT_ZOOM_INDEX;

  public static void apply() {
    Game.world().camera().setZoom(get(), 0);
  }

  public static void in() {
    if (currentZoomIndex < zooms.length - 1) {
      currentZoomIndex++;
    }

    apply();
  }

  public static void out() {
    if (currentZoomIndex > 0) {
      currentZoomIndex--;
    }

    apply();
  }

  public static float get() {
    return zooms[currentZoomIndex];
  }
}
