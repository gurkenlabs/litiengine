/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

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

  /**
   * Checks if is pressed.
   *
   * @return true, if is pressed
   */
  public boolean isPressed();

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
