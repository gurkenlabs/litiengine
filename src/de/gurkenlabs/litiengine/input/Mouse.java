/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;

/**
 * This implementation provides information about the mouse input in the litiengine.
 */
public class Mouse implements IMouse {

  /** The mouse listeners. */
  private final List<MouseListener> mouseListeners;

  /** The mouse motion listeners. */
  private final List<MouseMotionListener> mouseMotionListeners;

  /** The mouse wheel listeners. */
  private final List<MouseWheelListener> mouseWheelListeners;

  private final float sensitivity;

  private Robot robot;

  /** The position. */
  private Point location;

  /** The pressed. */
  private boolean pressed;
  /** The grab mouse. */
  private boolean grabMouse;

  private boolean isGrabbing;

  /**
   * Instantiates a new mouse.
   */
  public Mouse() {
    this.mouseListeners = new CopyOnWriteArrayList<>();
    this.mouseMotionListeners = new CopyOnWriteArrayList<>();
    this.mouseWheelListeners = new CopyOnWriteArrayList<>();

    try {
      this.robot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }

    this.setGrabMouse(true);
    this.location = new Point((int) Game.getScreenManager().getCamera().getCenterX(), (int) Game.getScreenManager().getCamera().getCenterY());
    this.sensitivity = Game.getConfiguration().INPUT.getMouseSensitivity();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.input.IMouse#getRenderLocation()
   */
  @Override
  public Point getLocation() {
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
    this.setLocation(e.getPoint());
    this.mouseListeners.forEach(listener -> listener.mouseClicked(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.mouseMotionListeners.forEach(listener -> listener.mouseDragged(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.mouseListeners.forEach(listener -> listener.mouseEntered(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.mouseListeners.forEach(listener -> listener.mouseExited(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.mouseMotionListeners.forEach(listener -> listener.mouseMoved(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.setPressed(true);
    this.mouseListeners.forEach(listener -> listener.mousePressed(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent e) {
    this.setLocation(e.getPoint());
    this.setPressed(false);
    this.mouseListeners.forEach(listener -> listener.mouseReleased(this.createEvent(e)));
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

  /**
   * Calculates the location of the ingame mouse by the position diff and locks
   * the original mouse to the center of the screen.
   * 
   * @param mouseLocation
   *          The location of the original mouse.
   */
  private void setLocation(Point mouseLocation) {
    if (this.isGrabbing) {
      return;
    }
    final double screenCenterX = Game.getScreenManager().getResolution().getWidth() / 2;
    final double screenCenterY = Game.getScreenManager().getResolution().getHeight() / 2;

    // calculate diffs and new location for the ingame mouse
    double diffX = mouseLocation.x - screenCenterX;
    double diffY = mouseLocation.y - screenCenterY;
    int newX = (int) (this.getLocation().getX() + diffX * this.sensitivity);
    int newY = (int) (this.getLocation().getY() + diffY * this.sensitivity);

    // ensure that x coordinates are within the screen
    if (newX < 0) {
      newX = 0;
    } else if (newX > Game.getScreenManager().getResolution().getWidth()) {
      newX = (int) Game.getScreenManager().getResolution().getWidth();
    }

    // ensure that y coordinates are within the screen
    if (newY < 0) {
      newY = 0;
    } else if (newY > Game.getScreenManager().getResolution().getHeight()) {
      newY = (int) Game.getScreenManager().getResolution().getHeight();
    }

    this.location = new Point(newX, newY);

    // lock original mouse back to the center of the screen
    this.isGrabbing = true;
    this.robot.mouseMove((int) (Game.getScreenManager().getScreenLocation().getX() + screenCenterX), (int) (Game.getScreenManager().getScreenLocation().getY() + screenCenterY));
    this.isGrabbing = false;
  }

  private MouseEvent createEvent(MouseEvent original) {
    MouseEvent event = new MouseEvent(original.getComponent(), original.getID(), original.getWhen(), original.getModifiers(), this.getLocation().x, this.getLocation().y, original.getXOnScreen(), original.getYOnScreen(), original.getClickCount(), original.isPopupTrigger(), original.getButton());
    return event;
  }
}
