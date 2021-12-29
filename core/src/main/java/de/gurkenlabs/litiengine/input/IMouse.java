package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameWindow;
import de.gurkenlabs.litiengine.graphics.MouseCursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.EventListener;

/** The {@code IMouse} interface is the engine's API for receiving mouse input events. */
public interface IMouse {
  /**
   * Adds the specified mouse clicked listener to receive events when the mouse has been clicked.
   *
   * @param listener
   *          The listener to add.
   * @see MouseListener#mouseClicked(MouseEvent)
   * @see MouseEvent#MOUSE_CLICKED
   */
  void onClicked(MouseClickedListener listener);

  /**
   * Unregisters the specified mouse clicked listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMouseClickedListener(MouseClickedListener listener);

  /**
   * Adds the specified mouse dragged listener to receive events when the mouse has been dragged.
   *
   * @param listener
   *          The listener to add.
   * @see MouseDraggedListener#mouseDragged(MouseEvent)
   * @see MouseMotionListener#mouseDragged(MouseEvent)
   * @see MouseEvent#MOUSE_DRAGGED
   */
  void onDragged(MouseDraggedListener listener);

  /**
   * Unregisters the specified mouse dragged listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMouseDraggedListener(MouseDraggedListener listener);

  /**
   * Adds the specified mouse moved listener to receive events when the mouse has been moved.
   *
   * @param listener
   *          The listener to add.
   * @see MouseMotionListener#mouseMoved(MouseEvent)
   * @see MouseEvent#MOUSE_MOVED
   */
  void onMoved(MouseMovedListener listener);

  /**
   * Unregisters the specified mouse moved listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMouseMovedListener(MouseMovedListener listener);

  /**
   * Adds the specified mouse pressed listener to receive events when the mouse has been pressed.
   *
   * @param listener
   *          The listener to add.
   * @see MouseListener#mousePressed(MouseEvent)
   * @see MouseEvent#MOUSE_PRESSED
   */
  void onPressed(MousePressedListener listener);

  /**
   * Unregisters the specified mouse pressed listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMousePressedListener(MousePressedListener listener);

  /**
   * Adds the specified mouse pressing listener to receive continuous events while the mouse is being pressed.
   *
   * @param listener
   *          The listener to add.
   */
  void onPressing(MousePressingListener listener);

  /**
   * Unregisters the specified mouse pressing listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMousePressingListener(MousePressingListener listener);

  /**
   * Adds the specified mouse released listener to receive events when the mouse has been released.
   *
   * @param listener
   *          The listener to add.
   * @see MouseListener#mouseReleased(MouseEvent)
   * @see MouseEvent#MOUSE_RELEASED
   */
  void onReleased(MouseReleasedListener listener);

  /**
   * Unregisters the specified mouse released listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMouseReleasedListener(MouseReleasedListener listener);

  /**
   * Adds the specified mouse wheel listener to receive events when the mouse wheel has been moved.
   *
   * @param listener
   *          The listener to add.
   * @see MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   * @see MouseWheelEvent
   */
  void onWheelMoved(MouseWheelListener listener);

  /**
   * Unregisters the specified mouse wheel listener.
   *
   * @param listener
   *          The listener to remove.
   */
  void removeMouseWheelListener(MouseWheelListener listener);

  /**
   * Register mouse listener.
   *
   * @param listener
   *          the listener
   */
  void addMouseListener(MouseListener listener);

  /**
   * Unregister mouse listener.
   *
   * @param listener
   *          the listener
   */
  void removeMouseListener(MouseListener listener);

  /**
   * Register mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  void addMouseMotionListener(MouseMotionListener listener);

  /**
   * Unregister mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  void removeMouseMotionListener(MouseMotionListener listener);

  /**
   * Removes all registered event listeners from the Mouse instance. This <b>does not affect</b> registered
   * {@code MouseListener}, {@code MouseMotionListener} or {@code MouseWheelListener} instances.
   *
   * @see #onClicked(MouseClickedListener)
   * @see #onDragged(MouseDraggedListener)
   * @see #onMoved(MouseMovedListener)
   * @see #onPressed(MousePressedListener)
   * @see #onPressing(MousePressingListener)
   * @see #onReleased(MouseReleasedListener)
   */
  void clearExplicitListeners();

  /**
   * Gets the current location of the mouse within the game window.
   *
   * <p>
   * The coordinates are relative to the game window and don't reflect coordinates on the game world. <br>
   * Use {@link #getMapLocation()} to get a translated position for the current environment.
   *
   * @return The current location of the mouse within the game window.
   * @see #getMapLocation()
   */
  Point2D getLocation();

  /**
   * Gets the location of the mouse on the current map.
   *
   * <p>
   * This translates the current mouse locations to the location on the map by using the current camera. <br>
   * Use {@link #getLocation()} to get the location within the game window.
   *
   * @return The location of the mouse on the current map.
   * @see IMouse#getLocation()
   */
  Point2D getMapLocation();

  /**
   * Gets the coordinates of the tile on the map on which the mouse is currently located at.
   *
   * @return The tile on which the mouse is currently located at.
   */
  Point getTile();

  /**
   * A flag indicating whether the mouse should be grabbed by the game's window.
   *
   * @return True if the mouse is locked to the game window; otherwise false.
   */
  boolean isGrabMouse();

  /**
   * A flag indicating whether any mouse button is currently pressed.
   *
   * @return True if any mouse button is currently pressed; otherwise false.
   */
  boolean isPressed();

  /**
   * A flag indicating whether the left mouse button is currently pressed.
   *
   * @return True if the left mouse button is currently pressed; otherwise false.
   */
  boolean isLeftButtonPressed();

  /**
   * A flag indicating whether the right mouse button is currently pressed.
   *
   * @return True if the right mouse button is currently pressed; otherwise false.
   */
  boolean isRightButtonPressed();

  /**
   * Returns true if the mouse event specifies the left mouse button.
   *
   * @param event
   *          The MouseEvent object
   * @return true if the left mouse button was active.
   */
  boolean isLeftButton(MouseEvent event);

  /**
   * Returns true if the mouse event specifies the right mouse button.
   *
   * @param event
   *          The MouseEvent object
   * @return true if the right mouse button was active.
   */
  boolean isRightButton(MouseEvent event);

  /**
   * If set to true, the mouse will be locked to the render component of the game.
   *
   * <p>
   * If this is set to true, the default cursor cannot be used anymore and instead a virtual cursor should be set.
   *
   * @param grab
   *          True if the mouse should be grabbed to the game's window, otherwise false.
   * @see MouseCursor#set(java.awt.Image)
   * @see GameWindow#cursor()
   * @see Game#window()
   */
  void setGrabMouse(boolean grab);

  /**
   * Sets the current mouse location to the specified location in the game window.
   *
   * <p>
   * <b>The location is not a location on the map but a location relative to the game window.</b>
   *
   * @param newLocation
   *          The location to which the mouse will be moved.
   * @see #getLocation()
   */
  void setLocation(Point2D newLocation);

  /**
   * Sets the current mouse location to the specified location in the game window.
   *
   * <p>
   * <b>The location is not a location on the map but a location relative to the game window.</b>
   *
   * @param x
   *          The x-coordinate to which the mouse will be moved.
   * @param y
   *          The y-coordinate to which the mouse will be moved.
   * @see #getLocation()
   */
  void setLocation(double x, double y);

  /**
   * This listener interface receives clicked events for the mouse.
   *
   * @see IMouse#onClicked(MouseClickedListener)
   */
  @FunctionalInterface
  interface MouseClickedListener extends EventListener {
    /**
     * Invoked when the mouse button has been clicked (pressed and released) on the game window.
     *
     * @param event
     *          The mouse event.
     */
    void mouseClicked(MouseEvent event);
  }

  /**
   * This listener interface receives dragged events for the mouse.
   *
   * @see IMouse#onDragged(MouseDraggedListener)
   */
  @FunctionalInterface
  interface MouseDraggedListener extends EventListener {
    /**
     * Invoked when a mouse button is pressed on the game window and then dragged. {@code
     * MOUSE_DRAGGED} events will continue to be delivered to the component where the drag originated until the mouse button
     * is released (regardless of whether the mouse position is within the bounds of the component).
     *
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations, {@code MOUSE_DRAGGED} events may not be delivered during a
     * native Drag&amp;Drop operation.
     *
     * @param event
     *          The mouse event.
     */
    void mouseDragged(MouseEvent event);
  }

  /**
   * This listener interface receives moved events for the mouse.
   *
   * @see IMouse#onMoved(MouseMovedListener)
   */
  @FunctionalInterface
  interface MouseMovedListener extends EventListener {
    /**
     * Invoked when the mouse cursor has been moved on the game window but no buttons have been pushed.
     *
     * @param event
     *          The mouse event.
     */
    void mouseMoved(MouseEvent event);
  }

  /**
   * This listener interface receives pressed events for the mouse.
   *
   * @see IMouse#onPressed(MousePressedListener)
   */
  @FunctionalInterface
  interface MousePressedListener extends EventListener {
    /**
     * Invoked when a mouse button has been pressed on the game window.
     *
     * @param event
     *          The mouse event.
     */
    void mousePressed(MouseEvent event);
  }

  /**
   * This listener interface receives pressing events for the mouse.
   *
   * @see IMouse#onPressing(MousePressingListener)
   */
  @FunctionalInterface
  interface MousePressingListener extends EventListener {
    /** Invoked continuously while a mouse button is being pressed on the game window. */
    void mousePressing();
  }

  /**
   * This listener interface receives released events for the mouse.
   *
   * @see IMouse#onReleased(MouseReleasedListener)
   */
  @FunctionalInterface
  interface MouseReleasedListener extends EventListener {
    /**
     * Invoked when a mouse button has been released on the game window.
     *
     * @param event
     *          The mouse event.
     */
    void mouseReleased(MouseEvent event);
  }
}
