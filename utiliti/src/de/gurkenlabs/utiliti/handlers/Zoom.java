package de.gurkenlabs.utiliti.handlers;

import java.util.Arrays;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.components.Editor;

public final class Zoom {
  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private static final int DEFAULT_ZOOM_INDEX = 3;
  private static int currentZoomIndex = DEFAULT_ZOOM_INDEX;

  private Zoom() {
  }
  
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
   * @param preference
   *          The preferred zoom.
   * @return The index of the matched zoom provided by this class.
   */
  public static int match(float preference) {
    int index = Arrays.binarySearch(zooms, preference);
    if (index >= 0) {
      return index;
    }
    index = -(index + 1);
    if (index == zooms.length || index > 0 && zooms[index] + zooms[index - 1] > 2 * preference) {
      index--;
    }
    return index;
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
    return get(currentZoomIndex);
  }

  public static float get(int zoomIndex) {
    return zooms[zoomIndex];
  }

  public static void set(float zoom) {
    currentZoomIndex = match(zoom);
  }

  public static float getMax() {
    return get(zooms.length - 1);
  }

  public static float getMin() {
    return get(0);
  }

  public static float getDefault() {
    return get(DEFAULT_ZOOM_INDEX);
  }
}
