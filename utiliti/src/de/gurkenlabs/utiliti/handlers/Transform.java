package de.gurkenlabs.utiliti.handlers;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.UI;

public class Transform {

  public enum ResizeAnchor {
    UP, DOWN, LEFT, RIGHT, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT
  }

  public enum TransformType {
    NONE, RESIZE, MOVE
  }

  private static final Map<ResizeAnchor, Rectangle2D> resizeAnchors = new ConcurrentHashMap<>();
  private static final Map<IMapObject, Point2D> dragLocationMapObjects = new ConcurrentHashMap<>();

  private static final int TRANSFORM_RECT_SIZE = 6;

  private static double transformRectSize = TRANSFORM_RECT_SIZE;

  private static TransformType type;
  private static ResizeAnchor anchor;

  private static float dragSizeHeight;
  private static float dragSizeWidth;
  private static Point2D dragPoint;

  public static TransformType type() {
    return type;
  }

  public static ResizeAnchor anchor() {
    return anchor;
  }

  public static Collection<Rectangle2D> getAnchors() {
    return resizeAnchors.values();
  }

  public static void resize() {
    final IMapObject transformObject = Editor.instance().getMapComponent().getFocusedMapObject();
    if (transformObject == null || Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_EDIT || type() != TransformType.RESIZE || anchor() == null) {
      return;
    }

    if (dragPoint == null) {
      dragPoint = Input.mouse().getMapLocation();
      dragLocationMapObjects.put(transformObject, new Point2D.Double(transformObject.getX(), transformObject.getY()));
      dragSizeHeight = transformObject.getHeight();
      dragSizeWidth = transformObject.getWidth();
      return;
    }

    Point2D dragLocationMapObject = dragLocationMapObjects.get(transformObject);
    double deltaX = Input.mouse().getMapLocation().getX() - dragPoint.getX();
    double deltaY = Input.mouse().getMapLocation().getY() - dragPoint.getY();
    double newWidth = dragSizeWidth;
    double newHeight = dragSizeHeight;
    double newX = dragLocationMapObject.getX();
    double newY = dragLocationMapObject.getY();

    switch (Transform.anchor()) {
    case DOWN:
      newHeight += deltaY;
      break;
    case DOWNRIGHT:
      newHeight += deltaY;
      newWidth += deltaX;
      break;
    case DOWNLEFT:
      newHeight += deltaY;
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + dragSizeWidth);
      break;
    case LEFT:
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + dragSizeWidth);
      break;
    case RIGHT:
      newWidth += deltaX;
      break;
    case UP:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + dragSizeHeight);
      break;
    case UPLEFT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + dragSizeHeight);
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + dragSizeWidth);
      break;
    case UPRIGHT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + dragSizeHeight);
      newWidth += deltaX;
      break;
    default:
      return;
    }

    newX = Snap.x(newX);
    newY = Snap.y(newY);
    newWidth = Snap.x(newWidth);
    newHeight = Snap.y(newHeight);

    final IMap map = Game.world().environment().getMap();
    if (map != null && Editor.preferences().clampToMap()) {
      newX = MathUtilities.clamp(newX, 0, map.getSizeInPixels().width);
      newY = MathUtilities.clamp(newX, 0, map.getSizeInPixels().height);

      newWidth = MathUtilities.clamp(newWidth, 0, map.getSizeInPixels().width - newX);
      newHeight = MathUtilities.clamp(newHeight, 0, map.getSizeInPixels().height - newY);
    }

    transformObject.setWidth((float) newWidth);
    transformObject.setHeight((float) newHeight);

    transformObject.setX((float) newX);
    transformObject.setY((float) newY);

    Game.world().environment().reloadFromMap(transformObject.getId());
    MapObjectType mapObjectType = MapObjectType.get(transformObject.getType());
    if (mapObjectType == MapObjectType.LIGHTSOURCE) {
      Game.world().environment().updateLighting(transformObject.getBoundingBox());
    }

    if (mapObjectType == MapObjectType.STATICSHADOW) {
      Game.world().environment().updateLighting();
    }

    UI.getInspector().bind(transformObject);
    updateAnchors();
  }

  public static void move() {
    final List<IMapObject> selectedMapObjects = Editor.instance().getMapComponent().getSelectedMapObjects();
    IMapObject minX = null;
    IMapObject minY = null;
    for (IMapObject selected : selectedMapObjects) {
      if (minX == null || selected.getX() < minX.getX()) {
        minX = selected;
      }

      if (minY == null || selected.getY() < minY.getY()) {
        minY = selected;
      }
    }

    if (minX == null || minY == null || (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_MOVE)) {
      return;
    }

    if (dragPoint == null) {
      dragPoint = Input.mouse().getMapLocation();
      return;
    }

    if (!dragLocationMapObjects.containsKey(minX)) {
      dragLocationMapObjects.put(minX, new Point2D.Double(minX.getX(), minX.getY()));
    }

    if (!dragLocationMapObjects.containsKey(minY)) {
      dragLocationMapObjects.put(minY, new Point2D.Double(minY.getX(), minY.getY()));
    }

    Point2D dragLocationMapObjectMinX = dragLocationMapObjects.get(minX);
    Point2D dragLocationMapObjectMinY = dragLocationMapObjects.get(minY);

    double deltaX = Input.mouse().getMapLocation().getX() - dragPoint.getX();
    float newX = Snap.x(dragLocationMapObjectMinX.getX() + deltaX);
    float snappedDeltaX = newX - minX.getX();

    double deltaY = Input.mouse().getMapLocation().getY() - dragPoint.getY();
    float newY = Snap.y(dragLocationMapObjectMinY.getY() + deltaY);
    float snappedDeltaY = newY - minY.getY();

    if (snappedDeltaX == 0 && snappedDeltaY == 0) {
      return;
    }

    final Rectangle2D beforeBounds = MapObject.getBounds2D(selectedMapObjects);
    moveEntities(selectedMapObjects, snappedDeltaX, snappedDeltaY);

    if (selectedMapObjects.stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW)) {
      Game.world().environment().updateLighting();
    } else if (selectedMapObjects.stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.LIGHTSOURCE)) {
      final Rectangle2D afterBounds = MapObject.getBounds2D(selectedMapObjects);
      double x = Math.min(beforeBounds.getX(), afterBounds.getX());
      double y = Math.min(beforeBounds.getY(), afterBounds.getY());
      double width = Math.max(beforeBounds.getMaxX(), afterBounds.getMaxX()) - x;
      double height = Math.max(beforeBounds.getMaxY(), afterBounds.getMaxY()) - y;
      Game.world().environment().updateLighting(new Rectangle2D.Double(x, y, width, height));
    }
  }

  public static void moveEntities(List<IMapObject> selectedMapObjects, float deltaX, float deltaY) {
    final IMap map = Game.world().environment().getMap();

    for (IMapObject selected : selectedMapObjects) {
      float newX = selected.getX() + deltaX;
      float newY = selected.getY() + deltaY;
      if (Editor.preferences().clampToMap()) {
        newX = MathUtilities.clamp(newX, 0, map.getSizeInPixels().width - selected.getWidth());
        newY = MathUtilities.clamp(newY, 0, map.getSizeInPixels().height - selected.getHeight());
      }

      selected.setX(newX);
      selected.setY(newY);

      IEntity entity = Game.world().environment().get(selected.getId());
      if (entity != null) {
        entity.setX(selected.getLocation().getX());
        entity.setY(selected.getLocation().getY());
      } else {
        Game.world().environment().reloadFromMap(selected.getId());
      }

      if (selected.equals(Editor.instance().getMapComponent().getFocusedMapObject())) {
        UI.getInspector().bind(selected);
      }
    }

    Transform.updateAnchors();
  }

  /***
   * Updates the currently applicable transform by evaluating the focused resize
   * anchor from the current mouse location or whether the mouse is currently
   * hovered over any selected map object to allow a move transformation.
   * <p>
   * This method also ensures that an adequate mouse cursor is set.
   * </p>
   */
  public static void updateTransform() {
    anchor = null;
    for (Entry<ResizeAnchor, Rectangle2D> entry : resizeAnchors.entrySet()) {
      Rectangle2D hoverrect = GeometricUtilities.extrude(entry.getValue(), 2.5);
      if (hoverrect.contains(Input.mouse().getMapLocation())) {
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

    // if no anchor is hovered, check if we can apply a move transform
    if (anchor == null) {
      for (IMapObject selected : Editor.instance().getMapComponent().getSelectedMapObjects()) {
        if (selected.getBoundingBox().contains(Input.mouse().getMapLocation())) {
          Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
          type = TransformType.MOVE;
          return;
        }
      }

      // if no transform can be applied, reset the transform type
      Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
      type = TransformType.NONE;
    }
  }

  public static void updateAnchors() {
    transformRectSize = Zoom.get() < Zoom.getDefault() ? TRANSFORM_RECT_SIZE : TRANSFORM_RECT_SIZE / (Math.sqrt(Zoom.get() * 1.25));
    final Rectangle2D focus = Editor.instance().getMapComponent().getFocusBounds();
    if (focus == null) {
      resizeAnchors.clear();
      return;
    }

    for (ResizeAnchor trans : ResizeAnchor.values()) {
      Rectangle2D transRect = new Rectangle2D.Double(getTransX(trans, focus), getTransY(trans, focus), transformRectSize, transformRectSize);
      resizeAnchors.put(trans, transRect);
    }
  }

  public static void resetDragging() {
    dragSizeHeight = 0;
    dragSizeWidth = 0;
    dragPoint = null;
    dragLocationMapObjects.clear();
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
