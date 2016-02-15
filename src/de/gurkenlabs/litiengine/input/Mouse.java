/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;

// TODO: Auto-generated Javadoc
/**
 * The Class Mouse.
 */
public class Mouse implements IMouse {

  /** The mouse listeners. */
  private final CopyOnWriteArrayList<MouseListener> mouseListeners;

  /** The mouse motion listeners. */
  private final CopyOnWriteArrayList<MouseMotionListener> mouseMotionListeners;

  /** The mouse wheel listeners. */
  private final CopyOnWriteArrayList<MouseWheelListener> mouseWheelListeners;

  /** The position. */
  private Point2D location;

  /** The pressed. */
  private boolean pressed;
  /** The grab mouse. */
  private boolean grabMouse;

  /**
   * Instantiates a new mouse.
   */
  public Mouse() {
    this.mouseListeners = new CopyOnWriteArrayList<>();
    this.mouseMotionListeners = new CopyOnWriteArrayList<>();
    this.mouseWheelListeners = new CopyOnWriteArrayList<>();

    this.setGrabMouse(true);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.input.IMouse#getRenderLocation()
   */
  @Override
  public Point2D getLocation() {
    return this.location;
  }

  @Override
  public Point2D getMapLocation() {
    return Game.getScreenManager().getCamera().getMapLocation(new Point2D.Double(this.getLocation().getX() / Game.getInfo().renderScale(), this.getLocation().getY() / Game.getInfo().renderScale()));
  }

  /**
   * Checks if is grab mouse.
   *
   * @return true, if is grab mouse
   */
  public boolean isGrabMouse() {
    return this.grabMouse;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.input.IMouse#isPressed()
   */
  @Override
  public boolean isPressed() {
    return this.pressed;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(final MouseEvent e) {
    this.location = e.getPoint();
    this.mouseListeners.forEach(listener -> listener.mouseClicked(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(final MouseEvent e) {
    this.location = e.getPoint();
    this.mouseMotionListeners.forEach(listener -> listener.mouseDragged(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(final MouseEvent e) {
    this.location = e.getPoint();
    this.mouseListeners.forEach(listener -> listener.mouseEntered(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(final MouseEvent e) {
    this.location = e.getPoint();
    this.mouseListeners.forEach(listener -> listener.mouseExited(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(final MouseEvent e) {
    this.location = e.getPoint();

    this.mouseMotionListeners.forEach(listener -> listener.mouseMoved(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent e) {
    this.location = e.getPoint();
    this.setPressed(true);
    this.mouseListeners.forEach(listener -> listener.mousePressed(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent e) {
    this.setPressed(false);
    this.mouseListeners.forEach(listener -> listener.mouseReleased(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
   * MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    this.mouseWheelListeners.forEach(listener -> listener.mouseWheelMoved(e));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.input.IMouse#registerMouseListener(java.awt.event.
   * MouseListener)
   */
  @Override
  public void registerMouseListener(final MouseListener listener) {
    if (this.mouseListeners.contains(listener)) {
      return;
    }

    this.mouseListeners.add(listener);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IMouse#registerMouseMotionListener(java.awt.event.
   * MouseMotionListener)
   */
  @Override
  public void registerMouseMotionListener(final MouseMotionListener listener) {
    if (this.mouseMotionListeners.contains(listener)) {
      return;
    }

    this.mouseMotionListeners.add(listener);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IMouse#registerMouseWheelListener(java.awt.event.
   * MouseWheelListener)
   */
  @Override
  public void registerMouseWheelListener(final MouseWheelListener listener) {
    if (this.mouseWheelListeners.contains(listener)) {
      return;
    }

    this.mouseWheelListeners.add(listener);
  }

  /**
   * Sets the grab mouse.
   *
   * @param grabMouse
   *          the new grab mouse
   */
  public void setGrabMouse(final boolean grabMouse) {
    this.grabMouse = grabMouse;
  }

  /**
   * Sets the pressed.
   *
   * @param pressed
   *          the new pressed
   */
  public void setPressed(final boolean pressed) {
    this.pressed = pressed;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IMouse#unregisterMouseListener(java.awt.event.
   * MouseListener)
   */
  @Override
  public void unregisterMouseListener(final MouseListener listener) {
    if (!this.mouseListeners.contains(listener)) {
      return;
    }

    this.mouseListeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IMouse#unregisterMouseMotionListener(java.awt.
   * event.MouseMotionListener)
   */
  @Override
  public void unregisterMouseMotionListener(final MouseMotionListener listener) {
    if (!this.mouseMotionListeners.contains(listener)) {
      return;
    }

    this.mouseMotionListeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IMouse#unregisterMouseWheelListener(java.awt.event
   * .MouseWheelListener)
   */
  @Override
  public void unregisterMouseWheelListener(final MouseWheelListener listener) {
    if (!this.mouseWheelListeners.contains(listener)) {
      return;
    }

    this.mouseWheelListeners.remove(listener);
  }

}
