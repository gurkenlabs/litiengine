package de.gurkenlabs.utiliti.handlers;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.components.EditorScreen;

public class Transform {

  public enum ResizeAnchor {
    UP, DOWN, LEFT, RIGHT, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT
  }

  public enum TransformType {
    NONE, RESIZE, MOVE
  }

  private static final Map<ResizeAnchor, Rectangle2D> resizeAnchors = new ConcurrentHashMap<>();
  private static final int TRANSFORM_RECT_SIZE = 6;

  private static double transformRectSize = TRANSFORM_RECT_SIZE;

  private static TransformType type;
  private static ResizeAnchor anchor;

  public static TransformType type() {
    return type;
  }

  public static ResizeAnchor anchor() {
    return anchor;
  }

  public static Collection<Rectangle2D> getAnchors() {
    return resizeAnchors.values();
  }

  public static void updateTransform() {
    anchor = null;
    boolean anchorHovered = false;
    for (Entry<ResizeAnchor, Rectangle2D> entry : resizeAnchors.entrySet()) {
      Rectangle2D hoverrect = GeometricUtilities.extrude(entry.getValue(), 2.5);
      if (hoverrect.contains(Input.mouse().getMapLocation())) {
        anchorHovered = true;
        if (entry.getKey() == ResizeAnchor.DOWN || entry.getKey() == ResizeAnchor.UP) {
          Game.window().getRenderComponent().setCursor(Cursors.TRANS_VERTICAL, 0, 0);
        } else if (entry.getKey() == ResizeAnchor.UPLEFT || entry.getKey() == ResizeAnchor.DOWNRIGHT) {
          Game.window().getRenderComponent().setCursor(Cursors.TRANS_DIAGONAL_LEFT, 0, 0);
        } else if (entry.getKey() == ResizeAnchor.UPRIGHT || entry.getKey() == ResizeAnchor.DOWNLEFT) {
          Game.window().getRenderComponent().setCursor(Cursors.TRANS_DIAGONAL_RIGHT, 0, 0);
        } else {
          Game.window().getRenderComponent().setCursor(Cursors.TRANS_HORIZONTAL, 0, 0);
        }

        anchor = entry.getKey();
        type = TransformType.RESIZE;
        break;
      }
    }

    if (!anchorHovered) {
      for (IMapObject selected : EditorScreen.instance().getMapComponent().getSelectedMapObjects()) {
        if (selected.getBoundingBox().contains(Input.mouse().getMapLocation())) {
          Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
          type = TransformType.MOVE;
          return;
        }
      }

      Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
      type = TransformType.NONE;
    }
  }

  public static void updateAnchors() {
    transformRectSize = Zoom.get() < Zoom.getDefault() ? TRANSFORM_RECT_SIZE : TRANSFORM_RECT_SIZE / (Math.sqrt(Zoom.get() * 1.25));
    final Rectangle2D focus = EditorScreen.instance().getMapComponent().getFocusBounds();
    if (focus == null) {
      resizeAnchors.clear();
      return;
    }

    for (ResizeAnchor trans : ResizeAnchor.values()) {
      Rectangle2D transRect = new Rectangle2D.Double(getTransX(trans, focus), getTransY(trans, focus), transformRectSize, transformRectSize);
      resizeAnchors.put(trans, transRect);
    }
  }

  private static double getTransX(ResizeAnchor type, Rectangle2D focus) {
    switch (type) {
    case DOWN:
    case UP:
      return focus.getCenterX() - transformRectSize / 2;
    case LEFT:
    case DOWNLEFT:
    case UPLEFT:
      return focus.getX() - transformRectSize;
    case RIGHT:
    case DOWNRIGHT:
    case UPRIGHT:
      return focus.getMaxX();
    default:
      return 0;
    }
  }

  private static double getTransY(ResizeAnchor type, Rectangle2D focus) {
    switch (type) {
    case DOWN:
    case DOWNLEFT:
    case DOWNRIGHT:
      return focus.getMaxY();
    case UP:
    case UPLEFT:
    case UPRIGHT:
      return focus.getY() - transformRectSize;
    case LEFT:
    case RIGHT:
      return focus.getCenterY() - transformRectSize / 2;
    default:
      return 0;
    }
  }
}
