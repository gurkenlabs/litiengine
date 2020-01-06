package de.gurkenlabs.litiengine.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The <code>IMouse</code> interface is the engine's API for receiving mouse input events.
 */
public interface IMouse extends MouseListener, MouseMotionListener, MouseWheelListener {
  public void onClicked(MouseClickedListener listener);

  public void removeMouseClickedListener(MouseClickedListener listener);

  public void onDragged(MouseDraggedListener listener);

  public void removeMouseDraggedListener(MouseDraggedListener listener);

  public void onMoved(MouseMovedListener listener);

  public void removeMouseMovedListener(MouseMovedListener listener);

  public void onPressed(MousePressedListener listener);

  public void removeMousePressedListener(MousePressedListener listener);

  public void onPressing(MousePressingListener listener);

  public void removeMousePressingListener(MousePressingListener listener);

  public void onReleased(MouseReleasedListener listener);

  public void removeMouseReleasedListener(MouseReleasedListener listener);

  public void onWheelMoved(MouseWheelListener listener);

  /**
   * Unregister mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseWheelListener(MouseWheelListener listener);

  /**
   * Register mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseListener(MouseListener listener);

  /**
   * Unregister mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseListener(MouseListener listener);

  /**
   * Register mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseMotionListener(MouseMotionListener listener);

  /**
   * Unregister mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseMotionListener(MouseMotionListener listener);

  /**
   * Removes all registered event listeners from the Mouse instance. This <b>does not affect</b> registered <code>MouseListener</code>,
   * <code>MouseMotionListener</code> or <code>MouseWheelListener</code> instances.
   * 
   * @see #onClicked(MouseClickedListener)
   * @see #onDragged(MouseDraggedListener)
   * @see #onMoved(MouseMovedListener)
   * @see #onPressed(MousePressedListener)
   * @see #onPressing(MousePressingListener)
   * @see #onReleased(MouseReleasedListener)
   */
  public void clearExplicitListeners();

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

  /**
   * If set to true, the mouse will be locked to the render component of the game.
   * 
   * @param grab
   *          True if the mouse should be grabbed to the {@link RenderComponent}, otherwise false.
   */
  public void setGrabMouse(boolean grab);

  public void setLocation(Point2D adjustMouse);

  public void setLocation(double x, double y);

  @FunctionalInterface
  public interface MouseClickedListener extends EventListener {
    void mouseClicked(MouseEvent event);
  }

  @FunctionalInterface
  public interface MouseDraggedListener extends EventListener {
    void mouseDragged(MouseEvent event);
  }

  @FunctionalInterface
  public interface MouseMovedListener extends EventListener {
    void mouseMoved(MouseEvent event);
  }

  @FunctionalInterface
  public interface MousePressedListener extends EventListener {
    void mousePressed(MouseEvent event);
  }

  @FunctionalInterface
  public interface MousePressingListener extends EventListener {
    void mousePressing();
  }

  @FunctionalInterface
  public interface MouseReleasedListener extends EventListener {
    void mouseReleased(MouseEvent event);
  }
}
