/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

/**
 * The Interface IMouse provides methods to mange mouse input in the litiengine.
 */
public interface IMouse extends MouseListener, MouseMotionListener, MouseWheelListener {

  /**
   * Gets the render location.
   *
   * @return the render location
   */
  public Point getLocation();

  public Point2D getMapLocation();
  
  public void setLocation(Point adjustMouse);

  public boolean isLeftMouseButtonDown();

  /**
   * Checks if is pressed.
   *
   * @return true, if is pressed
   */
  public boolean isPressed();

  public boolean isRightMouseButtonDown();

  public void setGrabMouse(boolean grab);
  
  public boolean isGrabMouse();
  public void onWheelMoved(Consumer<MouseWheelEvent> consumer);

  public void onClicked(Consumer<MouseEvent> consumer);

  public void onPressed(Consumer<MouseEvent> consumer);

  public void onDragged(Consumer<MouseEvent> consumer);

  public void onReleased(Consumer<MouseEvent> consumer);

  public void onMoved(Consumer<MouseEvent> consumer);

  /**
   * Register mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void registerMouseListener(MouseListener listener);

  /**
   * Register mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void registerMouseMotionListener(MouseMotionListener listener);

  /**
   * Register mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void registerMouseWheelListener(MouseWheelListener listener);

  /**
   * Unregister mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void unregisterMouseListener(MouseListener listener);

  /**
   * Unregister mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void unregisterMouseMotionListener(MouseMotionListener listener);

  /**
   * Unregister mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void unregisterMouseWheelListener(MouseWheelListener listener);
}
