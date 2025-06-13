package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import java.util.Arrays;

public record Zoom(float value) implements Comparable<Zoom> {
  private static final Zoom[] zooms =
      new Zoom[] {
          new Zoom(0.1f),
          new Zoom(0.25f),
          new Zoom(0.5f),
          new Zoom(1),
          new Zoom(1.5f),
          new Zoom(2f),
          new Zoom(3f),
          new Zoom(4f),
          new Zoom(5f),
          new Zoom(6f),
          new Zoom(7f),
          new Zoom(8f),
          new Zoom(9f),
          new Zoom(10f),
          new Zoom(16f),
          new Zoom(32f),
          new Zoom(50f),
          new Zoom(80f),
          new Zoom(100f)
      };
  private static final int DEFAULT_ZOOM_INDEX = 3;
  private static int currentZoomIndex = DEFAULT_ZOOM_INDEX;

  public float getValue() {
    return value;
  }

  @Override
  public int compareTo(Zoom o) {
    return Float.compare(this.getValue(), o.getValue());
  }

  @Override
  public String toString() {
    return String.format("%6d%%", (int) (this.getValue() * 100));
  }

  public static void apply() {
    if (Game.world() == null || Game.world().camera() == null) {
      return;
    }

    Game.world().camera().setZoom(get(), 0);
  }

  public static void applyPreference() {
    set(Editor.preferences().getZoom());
  }

  public static Zoom[] getAll() {
    return zooms;
  }

  /**
   * Matches the specified zoom with the closest zoom level that is provided by this class.
   *
   * <p>
   * For example: 1.111f would be converted to the preset 1.0f zoom level.
   *
   * @param preference
   *          The preferred zoom.
   * @return The index of the matched zoom provided by this class.
   */
  public static int match(float preference) {
    int index = Arrays.binarySearch(zooms, new Zoom(preference));
    if (index >= 0) {
      return index;
    }
    index = -(index + 1);
    if (index == zooms.length
        || index > 0 && zooms[index].getValue() + zooms[index - 1].getValue() > 2 * preference) {
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

  public static Zoom getZoom() {
    return zooms[currentZoomIndex];
  }

  public static float get(int zoomIndex) {
    return zooms[zoomIndex].getValue();
  }

  public static void set(float zoom) {
    currentZoomIndex = match(zoom);
    apply();
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
