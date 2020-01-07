package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Interface ICamera defines methods that allow to determine where entities
 * or tiles are rendered on the current screen.
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
   * @return the focus map location
   */
  public Point2D getFocus();

  /**
   * Gets the map location.
   *
   * @param point
   *          the point
   * @return the map location
   */
  public Point2D getMapLocation(Point2D point);

  /**
   * Gets the pixel offset x.
   *
   * @return the pixel offset x
   */
  public double getPixelOffsetX();

  /**
   * Gets the pixel offset y.
   *
   * @return the pixel offset y
   */
  public double getPixelOffsetY();

  /**
   * Gets the camera region.
   *
   * @return the camera region
   */
  public Rectangle2D getViewport();

  public Point2D getViewportDimensionCenter(IEntity entity);

  /**
   * Gets the render location.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @return the render location
   */
  public Point2D getViewportLocation(double x, double y);

  /**
   * This method calculates to location for the specified entity in relation to
   * the focus map location of the camera.
   *
   * @param entity
   *          the entity
   * @return the render location
   */
  public Point2D getViewportLocation(IEntity entity);

  /**
   * This method calculates to location for the specified point in relation to
   * the focus map location of the camera.
   *
   * @param point
   *          the point
   * @return the render location
   */
  public Point2D getViewportLocation(Point2D point);

  public float getRenderScale();

  public float getZoom();

  public void setFocus(Point2D focus);

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

  public void setZoom(float zoom, int delay);

  public boolean isClampToMap();

  public void setClampToMap(final boolean clampToMap);

  public void setClampAlign(Align align, Valign valign);

  public Align getClampAlign();

  public Valign getClampValign();

  public void shake(double intensity, final int delay, int duration);

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
