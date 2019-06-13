package de.gurkenlabs.utiliti.handlers;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
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

  private static final int TRANSFORM_RECT_SIZE = 6;

  private static double transformRectSize = TRANSFORM_RECT_SIZE;

  private static TransformType type;
  private static ResizeAnchor anchor;

  private static DragData drag;

  private static float dragSizeHeight;
  private static float dragSizeWidth;

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

    if (drag == null) {
      drag = new DragData(transformObject);
      dragSizeHeight = transformObject.getHeight();
      dragSizeWidth = transformObject.getWidth();
      return;
    }

    // get the distance that the mouse moved since the start of the dragging
    // operation
    final Point2D newMouseLocation = Input.mouse().getMapLocation();
    final double mouseDeltaX = newMouseLocation.getX() - drag.mouseLocation.getX();
    final double mouseDeltaY = newMouseLocation.getY() - drag.mouseLocation.getY();

    double newWidth = dragSizeWidth;
    double newHeight = dragSizeHeight;
    double newX = drag.minX;
    double newY = drag.minY;

    switch (Transform.anchor()) {
    case DOWN:
      newHeight += mouseDeltaY;
      break;
    case DOWNRIGHT:
      newHeight += mouseDeltaY;
      newWidth += mouseDeltaX;
      break;
    case DOWNLEFT:
      newHeight += mouseDeltaY;
      newWidth -= mouseDeltaX;
      newX += mouseDeltaX;
      // newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() +
      // dragSizeWidth);
      break;
    case LEFT:
      newWidth -= mouseDeltaX;
      newX += mouseDeltaX;
      // newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() +
      // dragSizeWidth);
      break;
    case RIGHT:
      newWidth += mouseDeltaX;
      break;
    case UP:
      newHeight -= mouseDeltaY;
      newY += mouseDeltaY;
      // newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() +
      // dragSizeHeight);
      break;
    case UPLEFT:
      newHeight -= mouseDeltaY;
      newY += mouseDeltaY;
      // newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() +
      // dragSizeHeight);
      newWidth -= mouseDeltaX;
      newX += mouseDeltaX;
      // newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() +
      // dragSizeWidth);
      break;
    case UPRIGHT:
      newHeight -= mouseDeltaY;
      newY += mouseDeltaY;
      // newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() +
      // dragSizeHeight);
      newWidth += mouseDeltaX;
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

  /**
   * Moves the currently selected map objects by the distance the mouse passed
   * since starting to drag.
   * 
   * <p>
   * Map objects are moved relative to the corners of the most left and most top
   * objects, i.e. the snapping will only be applied to the objects on the
   * border and all the other objects keep their relative distances. <br>
   * In a way this means that we're not moving the objects individually but
   * instead move them as a group that doesn't change its internal positioning.
   * </p>
   */
  public static void move() {
    final List<IMapObject> selectedMapObjects = Editor.instance().getMapComponent().getSelectedMapObjects();
    if (selectedMapObjects.isEmpty() || (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && Editor.instance().getMapComponent().getEditMode() != MapComponent.EDITMODE_MOVE)) {
      return;
    }

    // track the initial mouse location to use it for calculating the delta
    // values
    if (drag == null) {
      drag = new DragData(selectedMapObjects);
      return;
    }

    // get the distance that the mouse moved since the start of the dragging
    // operation
    // note that the always needs to be relative to the original positions
    // because otherwise, too small mouse movements will be
    // ignored due to snapping (particularly with grid snapping)
    final Point2D newMouseLocation = Input.mouse().getMapLocation();
    final double mouseDeltaX = newMouseLocation.getX() - drag.mouseLocation.getX();
    final double mouseDeltaY = newMouseLocation.getY() - drag.mouseLocation.getY();

    // snap the new location to the grid or the pixels, depending on the user
    // preference
    float newX = Snap.x(drag.minX + mouseDeltaX);
    float newY = Snap.y(drag.minY + mouseDeltaY);

    // clamp the new location to the map
    if (Editor.preferences().clampToMap()) {
      final IMap map = Game.world().environment().getMap();
      newX = MathUtilities.clamp(newX, 0, map.getSizeInPixels().width - drag.width);
      newY = MathUtilities.clamp(newY, 0, map.getSizeInPixels().height - drag.height);
    }

    // calculate the actual delta that all the map objects need to be moved by
    float deltaX = newX - drag.minX;
    float deltaY = newY - drag.minY;
    if (deltaX == 0 && deltaY == 0) {
      return;
    }

    final Rectangle2D beforeBounds = MapObject.getBounds(selectedMapObjects);
    moveEntities(selectedMapObjects, deltaX, deltaY);

    if (selectedMapObjects.stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW)) {
      Game.world().environment().updateLighting();
    } else if (selectedMapObjects.stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.LIGHTSOURCE)) {
      final Rectangle2D afterBounds = MapObject.getBounds(selectedMapObjects);
      double x = Math.min(beforeBounds.getX(), afterBounds.getX());
      double y = Math.min(beforeBounds.getY(), afterBounds.getY());
      double width = Math.max(beforeBounds.getMaxX(), afterBounds.getMaxX()) - x;
      double height = Math.max(beforeBounds.getMaxY(), afterBounds.getMaxY()) - y;
      Game.world().environment().updateLighting(new Rectangle2D.Double(x, y, width, height));
    }
  }

  public static void moveEntities(List<IMapObject> selectedMapObjects, float deltaX, float deltaY) {

    for (IMapObject selected : selectedMapObjects) {
      double newX = drag.originalLocations.get(selected).getX() + deltaX;
      double newY = drag.originalLocations.get(selected).getY() + deltaY;

      selected.setX((float) newX);
      selected.setY((float) newY);

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
      Rectangle2D transRect = new Rectangle2D.Double(getAnchorX(trans, focus), getAnchorY(trans, focus), transformRectSize, transformRectSize);
      resizeAnchors.put(trans, transRect);
    }
  }

  public static void resetDragging() {
    dragSizeHeight = 0;
    dragSizeWidth = 0;
    drag = null;
  }

  private static double getAnchorX(ResizeAnchor type, Rectangle2D focus) {
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

  private static double getAnchorY(ResizeAnchor type, Rectangle2D focus) {
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

  private static class DragData {
    private final Map<IMapObject, Point2D> originalLocations;
    private final Point2D mouseLocation;
    private final float minX;
    private final float minY;
    private final float width;
    private final float height;

    public DragData(IMapObject... selectedMapObjects) {
      this(Arrays.asList(selectedMapObjects));
    }

    public DragData(List<IMapObject> selectedMapObjects) {
      this.originalLocations = new ConcurrentHashMap<>();

      this.mouseLocation = Input.mouse().getMapLocation();
      Rectangle2D bounds = MapObject.getBounds(selectedMapObjects);
      this.minX = (float) bounds.getX();
      this.minY = (float) bounds.getY();
      this.width = (float) bounds.getWidth();
      this.height = (float) bounds.getHeight();

      for (IMapObject obj : selectedMapObjects) {
        this.originalLocations.put(obj, obj.getLocation());
      }
    }
  }
}
