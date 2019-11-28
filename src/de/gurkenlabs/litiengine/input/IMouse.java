package de.gurkenlabs.litiengine.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The Interface IMouse provides methods to manage mouse input in the LITIengine.
 */
public interface IMouse extends MouseListener, MouseMotionListener, MouseWheelListener {

  /**
   * Gets the render location.
   *
   * @return the render location
   */
  public Point2D getLocation();

  public Point2D getMapLocation();

  public Point getTile();

  public boolean isGrabMouse();

  public boolean isLeftMouseButtonDown();

  public boolean isPressed();

  public boolean isRightMouseButtonDown();

  public void onClicked(Consumer<MouseEvent> consumer);

  public void onDragged(Consumer<MouseEvent> consumer);

  public void onMoved(Consumer<MouseEvent> consumer);

  public void onPressed(Consumer<MouseEvent> consumer);
  
  public void onPressing(Runnable consumer);

  public void onReleased(Consumer<MouseEvent> consumer);

  public void onWheelMoved(Consumer<MouseWheelEvent> consumer);
  
  /**
   * Removes all registered event consumers from the Mouse instance. This <b>does not affect</b> registered <code>MouseListener</code>, <code>MouseMotionListener</code> or <code>MouseWheelListener</code> instances.
   * 
   * @see #onClicked(Consumer)
   * @see #onDragged(Consumer)
   * @see #onMoved(Consumer)
   * @see #onPressed(Consumer)
   * @see #onReleased(Consumer)
   * @see #onWheelMoved(Consumer)
   */
  public void clearEventConsumers();

  /**
   * Register mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseListener(MouseListener listener);

  /**
   * Register mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseMotionListener(MouseMotionListener listener);

  /**
   * Register mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseWheelListener(MouseWheelListener listener);

  /**
   * If set to true, the mouse will be locked to the render component of the game.
   * 
   * @param grab True if the mouse should be grabbed to the {@link RenderComponent}, otherwise false.
   */
  public void setGrabMouse(boolean grab);

  public void setLocation(Point2D adjustMouse);
  
  public void setLocation(double x, double y);

  /**
   * Unregister mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseListener(MouseListener listener);

  /**
   * Unregister mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseMotionListener(MouseMotionListener listener);

  /**
   * Unregister mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseWheelListener(MouseWheelListener listener);
}
