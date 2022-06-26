package de.gurkenlabs.utiliti.handlers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.utiliti.components.Editor;

public final class Snap {

  private static final int DEFAULT_PRECISION = 2;

  private Snap() {}

  public static float x(double x) {
    final IMap map =
        Game.world() != null && Game.world().environment() != null
            ? Game.world().environment().getMap()
            : null;
    int gridSize = map != null ? map.getTileSize().width : 1;

    return x(x, gridSize, Editor.preferences().snapToGrid(), Editor.preferences().snapToPixels());
  }

  public static float x(double x, int gridSize, boolean snapToGrid, boolean snapToPixel) {
    return snap(x, gridSize, snapToGrid, snapToPixel);
  }

  public static float y(double y) {
    final IMap map =
        Game.world() != null && Game.world().environment() != null
            ? Game.world().environment().getMap()
            : null;
    int gridSize = map != null ? map.getTileSize().height : 1;

    return y(y, gridSize, Editor.preferences().snapToGrid(), Editor.preferences().snapToPixels());
  }

  public static float y(double y, int gridSize, boolean snapToGrid, boolean snapToPixel) {
    return snap(y, gridSize, snapToGrid, snapToPixel);
  }

  private static float snap(double value, int gridSize, boolean snapToGrid, boolean snapToPixel) {
    double snapped = value;

    if (gridSize > 1 && snapToGrid) {
      snapped = snapToGrid(value, gridSize);
    } else if (snapToPixel) {
      snapped = snapToPixels(value);
    }

    // apply default precision
    return MathUtilities.round((float) snapped, DEFAULT_PRECISION);
  }

  private static int snapToPixels(double value) {
    return (int) Math.round(value);
  }

  private static double snapToGrid(double value, int gridSize) {
    double gridSizeDivided = gridSize / (double) Editor.preferences().getSnapDivision();
    return Math.round(value / gridSizeDivided) * gridSizeDivided;
  }
}
