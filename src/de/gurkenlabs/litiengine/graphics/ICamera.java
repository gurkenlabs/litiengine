package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Interface ICamera defines methods that allow to determine where entities
 * or tiles are rendered on the current screen.
 * 
 * Camera control is based on a Focus system. Generally, the camera will always
 * try to keep the focus point in the center of the viewport.
 * 
 * There are two coordinate systems referenced in ICamera methods: map coordinates,
 * and screen coordinates. The camera is responsible for converting between the
 * two coordinate systems.
 */
public interface ICamera extends IUpdateable {
  /**
   * Adds the specified zoom changed listener to receive events when the zoom of this camera changed.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onZoom(ZoomChangedListener listener);

  /**
   * Removes the specified zoom changed listener.
   * 
   * @param listener
   *          The listener to add.
   */
  public void removeZoomListener(ZoomChangedListener listener);

  /**
   * Adds the specified focus changed listener to receive events when the focus of this camera changed.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onFocus(FocusChangedListener listener);

  /**
   * Removes the specified focus changed listener.
   * 
   * @param listener
   *          The listener to add.
   */
  public void removeFocusListener(FocusChangedListener listener);

  /**
   * Gets the map location that is focused by this camera.
   *
   * @return the focus's location in map coordinates
   */
  public Point2D getFocus();

  /**
   * Converts a point in screen coordinates into a map location.
   *
   * @param point
   *          the point in screen coordinates
   * @return the map location
   */
  public Point2D getMapLocation(Point2D point);

  /**
   * Gets the x coordinate of the viewport's origin.
   *
   * @return the offset, in screen coordinates
   */
  public double getPixelOffsetX();

  /**
   * Gets the y coordinate of the viewport's origin.
   *
   * @return the offset, in screen coordinates
   */
  public double getPixelOffsetY();

  /**
   * Gets the camera's viewport region, in screen coordinates.
   *
   * @return the viewport region, in screen coordinates
   */
  public Rectangle2D getViewport();

  /**
   * Gets the center of the entity, in screen coordinates.
   * 
   * @param entity
   *          The entity to retrieve the dimension center for.
   * @return the center, in screen coordinates
   */
  public Point2D getViewportDimensionCenter(IEntity entity);

  /**
   * Converts a location in map coordinates into screen coordinates.
   *
   * @param x
   *          The x-coordinate of the viewport location.
   * @param y
   *          The y-coordinate of the viewport location.
   * @return the screen location
   */
  public Point2D getViewportLocation(double x, double y);

  /**
   * Converts the entity's location into screen coordinates.
   *
   * @param entity
   *          the entity
   * @return the screen location
   */
  public default Point2D getViewportLocation(IEntity entity) {
    Point2D entityLocation = entity.getLocation();
    return getViewportLocation(entityLocation.getX(), entityLocation.getY());
  }

  /**
   * Converts a location in map coordinates into screen coordinates.
   *
   * @param point
   *          the point
   * @return the screen location
   */
  public default Point2D getViewportLocation(Point2D point) {
    return getViewportLocation(point.getX(), point.getY());
  }

  /**
   * Combines this camera's zoom with the game's render scale.
   * 
   * @see RenderEngine#setBaseRenderScale(float)
   * @return the scale factor
   */
  public default float getRenderScale() {
    return Game.graphics().getBaseRenderScale() * Game.window().getResolutionScale() * this.getZoom();
  }

  /**
   * The zoom factor of this camera.
   * 
   * @return the scale factor
   */
  public float getZoom();

  /**
   * Focuses the camera on a given point.
   * 
   * @param focus
   *          the point, in map coordinates
   */
  public default void setFocus(Point2D focus) {
    setFocus(focus.getX(), focus.getY());
  }

  /**
   * Focuses the camera on a given point.
   * 
   * @param x
   *          the x coordinate of the point, in map coordinates
   * @param y
   *          the y coordinate of the point, in map coordinates
   */
  public void setFocus(double x, double y);

  /**
   * Pans the camera over the specified duration (in frames) to the target
   * location, after accounting for modifications such as clamping to the
   * map. Event listeners attached to this camera are notified when the pan
   * completes.
   * 
   * @param focus
   *          the new focus for the camera once the panning is complete
   * @param duration
   *          the number of frames between this call and when the pan
   *          completes
   */
  public void pan(Point2D focus, int duration);

  /**
   * Pans the camera over the specified duration (in frames) to the target
   * location, after accounting for modifications such as clamping to the
   * map. Event listeners attached to this camera are notified when the pan
   * completes.
   * 
   * @param x
   *          the new X position for the camera once the panning is complete
   * @param y
   *          the new Y position for the camera once the panning is complete
   * @param duration
   *          the number of frames between this call and when the pan
   *          completes
   */
  public void pan(double x, double y, int duration);

  /**
   * Changes the camera's zoom over the specified duration (in frames) to the
   * target zoom.
   * 
   * @param zoom
   *          the new zoom scale
   * @param duration
   *          the number of frames between this call and when the zoom
   *          completes
   */
  public void setZoom(float zoom, int duration);

  /**
   * Returns whether this camera will clamp the viewport to the bounds of the
   * map.
   * 
   * @return True if the camera viewport is currently clamped to the map boundaries; otherwise false.
   */
  public boolean isClampToMap();

  /**
   * Set the camera to clamp the viewport to the bounds of the map.
   * 
   * @param clampToMap
   *          A flag indicating whether the camera viewport should be clamped to the map boundaries.
   */
  public void setClampToMap(final boolean clampToMap);

  public void setClampAlign(Align align, Valign valign);

  public Align getClampAlign();

  public Valign getClampValign();

  /**
   * Shake the camera for the specified duration (in frames). The way the camera
   * shakes is implementation defined.
   * 
   * @param intensity
   *          The intensity of the screen shake effect.
   * @param delay
   *          The delay before the effect starts.
   * @param duration
   *          The duration of the effect.
   */
  public void shake(double intensity, final int delay, int duration);

  /**
   * Currently an update function for the shake effect.
   */
  public void updateFocus();

  /**
   * This listener interface receives zoom events for a camera.
   * 
   * @see ICamera#onZoom(ZoomChangedListener)
   */
  @FunctionalInterface
  public interface ZoomChangedListener extends EventListener {
    /**
     * Invoked when the zoom of a camera changed.
     * 
     * @param event
     *          The zoom changed event.
     */
    void zoomChanged(ZoomChangedEvent event);
  }

  /**
   * This listener interface receives focus events for a camera.
   * 
   * @see ICamera#onFocus(FocusChangedListener)
   */
  @FunctionalInterface
  public interface FocusChangedListener extends EventListener {
    /**
     * Invoked when the focus of a camera changed.
     * 
     * @param event
     *          The focus changed event.
     */
    void focusChanged(FocusChangedEvent event);
  }
}
