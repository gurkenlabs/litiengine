package de.gurkenlabs.utiliti.handlers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.components.Editor;

public class Zoom {
  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private static final int DEFAULT_ZOOM_INDEX = 3;
  private static int currentZoomIndex = DEFAULT_ZOOM_INDEX;

  public static void apply() {
    if (Game.world() == null || Game.world().camera() == null) {
      return;
    }

    Game.world().camera().setZoom(get(), 0);
  }

  public static void applyPreference() {
    set(Editor.preferences().getZoom());
    apply();
  }

  /**
   * Matches the specified zoom with the closest zoom level that is provided by
   * this class.
   * <p>
   * For example: 1.111f would be converted to the preset 1.0f zoom level.
   * </p>
   * 
   * @param preferece
   *          The preferred zoom.
   * @return The index of the matched zoom provided by this class.
   */
  public static int match(float preference) {
    int match = Integer.MAX_VALUE;
    double diff = 0;
    for (int i = 0; i < zooms.length; i++) {
      double newDiff = Math.abs(preference - zooms[i]);
      if ((diff == 0 && match == Integer.MAX_VALUE) || newDiff < diff) {
        if (newDiff == 0) {
          return i;
        }

        diff = newDiff;
        match = i;
      }
    }

    return match;
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

  public static void set(float zoom) {
    currentZoomIndex = match(zoom);
  }

  public static float getMax() {
    return zooms[zooms.length - 1];
  }

  public static float getMin() {
    return zooms[0];
  }

  public static float getDefault() {
    return zooms[DEFAULT_ZOOM_INDEX];
  }
}
