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
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.util.MathUtilities;

/**
 * This implementation provides information about the mouse input in the
 * litiengine.
 */
public class Mouse implements IMouse {

  /** The mouse listeners. */
  private final List<MouseListener> mouseListeners;

  /** The mouse motion listeners. */
  private final List<MouseMotionListener> mouseMotionListeners;

  /** The mouse wheel listeners. */
  private final List<MouseWheelListener> mouseWheelListeners;

  private final List<Consumer<MouseWheelEvent>> wheelMovedConsumer;

  private final List<Consumer<MouseEvent>> mouseClickedConsumer;
  private final List<Consumer<MouseEvent>> mouseMovedConsumer;
  private final List<Consumer<MouseEvent>> mousePressedConsumer;
  private final List<Consumer<MouseEvent>> mouseDraggedConsumer;
  private final List<Consumer<MouseEvent>> mouseReleasedConsumer;

  private final float sensitivity;

  private Robot robot;

  /** The position. */
  private Point location;

  /** The pressed. */
  private boolean pressed;

  private boolean isLeftMouseButtonDown;
  private boolean isRightMouseButtonDown;

  private boolean grabMouse;

  private Point lastLocation;

  /**
   * Instantiates a new mouse.
   */
  public Mouse() {
    this.mouseListeners = new CopyOnWriteArrayList<>();
    this.mouseMotionListeners = new CopyOnWriteArrayList<>();
    this.mouseWheelListeners = new CopyOnWriteArrayList<>();
    this.wheelMovedConsumer = new CopyOnWriteArrayList<>();
    this.mouseClickedConsumer = new CopyOnWriteArrayList<>();
    this.mousePressedConsumer = new CopyOnWriteArrayList<>();
    this.mouseMovedConsumer = new CopyOnWriteArrayList<>();
    this.mouseDraggedConsumer = new CopyOnWriteArrayList<>();
    this.mouseReleasedConsumer = new CopyOnWriteArrayList<>();

    try {
      this.robot = new Robot();
      this.robot.setAutoDelay(0);
    } catch (final AWTException e) {
      e.printStackTrace();
    }

    this.location = new Point((int) Game.getScreenManager().getCamera().getViewPort().getCenterX(), (int) Game.getScreenManager().getCamera().getViewPort().getCenterY());
    this.lastLocation = this.location;
    this.sensitivity = Game.getConfiguration().INPUT.getMouseSensitivity();
    this.grabMouse = true;
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
    return Game.getScreenManager().getCamera().getMapLocation(new Point2D.Double(this.getLocation().getX() / Game.getInfo().getRenderScale(), this.getLocation().getY() / Game.getInfo().getRenderScale()));
  }

  @Override
  public boolean isLeftMouseButtonDown() {
    return this.isLeftMouseButtonDown;
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

  @Override
  public boolean isRightMouseButtonDown() {
    return this.isRightMouseButtonDown;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(final MouseEvent e) {
    this.setLocation(e);
    this.mouseListeners.forEach(listener -> listener.mouseClicked(this.createEvent(e)));

    for (Consumer<MouseEvent> cons : this.mouseClickedConsumer) {
      cons.accept(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(final MouseEvent e) {
    this.setLocation(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseDragged(this.createEvent(e)));

    for (Consumer<MouseEvent> cons : this.mouseDraggedConsumer) {
      cons.accept(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(final MouseEvent e) {
    if (!this.grabMouse) {
      this.lastLocation = e.getPoint();
      this.location = e.getPoint();
    } else {
      this.setLocation(e);
    }

    this.mouseListeners.forEach(listener -> listener.mouseEntered(this.createEvent(e)));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(final MouseEvent e) {
    this.setLocation(e);
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
    this.setLocation(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseMoved(this.createEvent(e)));

    for (Consumer<MouseEvent> cons : this.mouseMovedConsumer) {
      cons.accept(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent e) {
    this.setLocation(e);
    this.setPressed(true);
    this.mouseListeners.forEach(listener -> listener.mousePressed(this.createEvent(e)));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = true;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = true;
    }

    for (Consumer<MouseEvent> cons : this.mousePressedConsumer) {
      cons.accept(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent e) {
    this.setLocation(e);
    this.setPressed(false);
    this.mouseListeners.forEach(listener -> listener.mouseReleased(this.createEvent(e)));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = false;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = false;
    }

    for (Consumer<MouseEvent> cons : this.mouseReleasedConsumer) {
      cons.accept(e);
    }
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
    this.wheelMovedConsumer.forEach(cons -> cons.accept(e));
  }

  @Override
  public void onWheelMoved(final Consumer<MouseWheelEvent> consumer) {
    this.wheelMovedConsumer.add(consumer);
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

  @Override
  public void setGrabMouse(boolean grab) {
    this.grabMouse = grab;
  }

  @Override
  public boolean isGrabMouse() {
    return this.grabMouse;
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

  @Override
  public void onClicked(Consumer<MouseEvent> consumer) {
    this.mouseClickedConsumer.add(consumer);
  }

  @Override
  public void onPressed(Consumer<MouseEvent> consumer) {
    this.mousePressedConsumer.add(consumer);
  }

  @Override
  public void onDragged(Consumer<MouseEvent> consumer) {
    this.mouseDraggedConsumer.add(consumer);
  }

  @Override
  public void onReleased(Consumer<MouseEvent> consumer) {
    this.mouseReleasedConsumer.add(consumer);
  }

  @Override
  public void onMoved(Consumer<MouseEvent> consumer) {
    this.mouseMovedConsumer.add(consumer);
  }

  /**
   * Calculates the location of the ingame mouse by the position diff and locks
   * the original mouse to the center of the screen.
   *
   * @param mouseLocation
   *          The location of the original mouse.
   */
  private void setLocation(MouseEvent e) {
    if (this.grabMouse && !Game.getScreenManager().isFocusOwner()) {
      return;
    }

    double diffX, diffY;
    if (!this.grabMouse) {
      // get diff relative from last mouse location
      diffX = e.getX() - this.lastLocation.x;
      diffY = e.getY() - this.lastLocation.y;
      this.lastLocation = new Point(e.getPoint().x + Game.getScreenManager().getRenderComponent().getCursorOffsetX(), e.getPoint().y + Game.getScreenManager().getRenderComponent().getCursorOffsetY());
    } else {
      // get diff relative from grabbed position
      final double screenCenterX = Game.getScreenManager().getResolution().getWidth() * 0.5;
      final double screenCenterY = Game.getScreenManager().getResolution().getHeight() * 0.5;
      final Point screenLocation = Game.getScreenManager().getScreenLocation();
      final int grabX = (int) (screenLocation.x + screenCenterX);
      final int grabY = (int) (screenLocation.y + screenCenterY);

      // lock original mouse back to the center of the screen
      this.robot.mouseMove(grabX - Game.getScreenManager().getRenderComponent().getCursorOffsetX(), grabY - Game.getScreenManager().getRenderComponent().getCursorOffsetY());

      // calculate diffs and new location for the ingame mouse
      diffX = e.getXOnScreen() - grabX;
      diffY = e.getYOnScreen() - grabY;
    }

    // set new mouse location
    int newX = (int) (this.getLocation().getX() + diffX * this.sensitivity);
    int newY = (int) (this.getLocation().getY() + diffY * this.sensitivity);
    newX = MathUtilities.clamp(newX, 0, (int) Game.getScreenManager().getResolution().getWidth());
    newY = MathUtilities.clamp(newY, 0, (int) Game.getScreenManager().getResolution().getHeight());

    this.location = new Point(newX + Game.getScreenManager().getRenderComponent().getCursorOffsetX(), newY + Game.getScreenManager().getRenderComponent().getCursorOffsetY());
  }

  private MouseEvent createEvent(final MouseEvent original) {
    final MouseEvent event = new MouseEvent(original.getComponent(), original.getID(), original.getWhen(), original.getModifiers(), this.getLocation().x, this.getLocation().y, original.getXOnScreen(), original.getYOnScreen(), original.getClickCount(), original.isPopupTrigger(),
        original.getButton());
    return event;
  }

  /**
   * Sets the pressed.
   *
   * @param pressed
   *          the new pressed
   */
  private void setPressed(final boolean pressed) {
    this.pressed = pressed;
  }
}
