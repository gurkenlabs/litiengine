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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.util.MathUtilities;

/**
 * This implementation provides information about the mouse input in the
 * litiengine.
 */
public class Mouse implements IMouse {
  private static final Logger log = Logger.getLogger(Mouse.class.getName());
  private boolean grabMouse;

  private boolean isLeftMouseButtonDown;

  private boolean isRightMouseButtonDown;

  private Point2D lastLocation;

  /** The position. */
  private Point2D location;
  private final List<Consumer<MouseEvent>> mouseClickedConsumer;
  private final List<Consumer<MouseEvent>> mouseDraggedConsumer;
  /** The mouse listeners. */
  private final List<MouseListener> mouseListeners;
  /** The mouse motion listeners. */
  private final List<MouseMotionListener> mouseMotionListeners;

  private final List<Consumer<MouseEvent>> mouseMovedConsumer;

  private final List<Consumer<MouseEvent>> mousePressedConsumer;

  private final List<Consumer<MouseEvent>> mouseReleasedConsumer;

  /** The mouse wheel listeners. */
  private final List<MouseWheelListener> mouseWheelListeners;

  /** The pressed. */
  private boolean pressed;
  private Robot robot;

  private final float sensitivity;

  private final List<Consumer<MouseWheelEvent>> wheelMovedConsumer;

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
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    this.location = new Point2D.Double(Game.getCamera().getViewPort().getCenterX(), Game.getCamera().getViewPort().getCenterY());
    this.lastLocation = this.location;
    this.sensitivity = Game.getConfiguration().input().getMouseSensitivity();
    this.grabMouse = true;
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
    return Game.getCamera().getMapLocation(new Point2D.Double(this.getLocation().getX() / Game.getCamera().getRenderScale(), this.getLocation().getY() / Game.getCamera().getRenderScale()));
  }

  @Override
  public boolean isGrabMouse() {
    return this.grabMouse;
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

    for (final Consumer<MouseEvent> cons : this.mouseClickedConsumer) {
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

    for (final Consumer<MouseEvent> cons : this.mouseDraggedConsumer) {
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

    for (final Consumer<MouseEvent> cons : this.mouseMovedConsumer) {
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

    for (final Consumer<MouseEvent> cons : this.mousePressedConsumer) {
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

    for (final Consumer<MouseEvent> cons : this.mouseReleasedConsumer) {
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
  public void onClicked(final Consumer<MouseEvent> consumer) {
    this.mouseClickedConsumer.add(consumer);
  }

  @Override
  public void onDragged(final Consumer<MouseEvent> consumer) {
    this.mouseDraggedConsumer.add(consumer);
  }

  @Override
  public void onMoved(final Consumer<MouseEvent> consumer) {
    this.mouseMovedConsumer.add(consumer);
  }

  @Override
  public void onPressed(final Consumer<MouseEvent> consumer) {
    this.mousePressedConsumer.add(consumer);
  }

  @Override
  public void onReleased(final Consumer<MouseEvent> consumer) {
    this.mouseReleasedConsumer.add(consumer);
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
  public void setGrabMouse(final boolean grab) {
    this.grabMouse = grab;
  }

  @Override
  public void setLocation(final Point2D adjustMouse) {
    if (adjustMouse == null) {
      return;
    }

    this.location = adjustMouse;
    this.lastLocation = adjustMouse;

    final MouseEvent mouseEvent = new MouseEvent(Game.getScreenManager().getRenderComponent(), MouseEvent.MOUSE_MOVED, 0, 0, (int) this.getLocation().getX(), (int) this.getLocation().getY(), 0, false, MouseEvent.NOBUTTON);
    for (final Consumer<MouseEvent> cons : this.mouseMovedConsumer) {
      cons.accept(this.createEvent(mouseEvent));
    }
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

  private MouseEvent createEvent(final MouseEvent original) {
    return new MouseEvent(original.getComponent(), original.getID(), original.getWhen(), original.getModifiers(), (int) this.getLocation().getX(), (int) this.getLocation().getY(), original.getXOnScreen(), original.getYOnScreen(), original.getClickCount(), original.isPopupTrigger(),
        original.getButton());
  }

  /**
   * Calculates the location of the ingame mouse by the position diff and locks
   * the original mouse to the center of the screen.
   *
   * @param mouseLocation
   *          The location of the original mouse.
   */
  private void setLocation(final MouseEvent e) {
    if (this.grabMouse && !Game.getScreenManager().isFocusOwner()) {
      return;
    }

    double diffX;
    double diffY;
    if (!this.grabMouse) {
      // get diff relative from last mouse location
      diffX = e.getX() - this.lastLocation.getX();
      diffY = e.getY() - this.lastLocation.getY();
      this.lastLocation = new Point(e.getPoint().x - Game.getScreenManager().getRenderComponent().getCursorOffsetX(), e.getPoint().y - Game.getScreenManager().getRenderComponent().getCursorOffsetY());
    } else {
      // get diff relative from grabbed position
      final double screenCenterX = Game.getScreenManager().getResolution().getWidth() * 0.5;
      final double screenCenterY = Game.getScreenManager().getResolution().getHeight() * 0.5;
      final Point screenLocation = Game.getScreenManager().getScreenLocation();
      final int grabX = (int) (screenLocation.x + screenCenterX);
      final int grabY = (int) (screenLocation.y + screenCenterY);

      // lock original mouse back to the center of the screen
      this.robot.mouseMove(grabX, grabY);

      // calculate diffs and new location for the ingame mouse
      diffX = e.getXOnScreen() - (double) grabX;
      diffY = e.getYOnScreen() - (double) grabY;
    }

    // set new mouse location
    double newX = this.getLocation().getX() + diffX * this.sensitivity;
    double newY = this.getLocation().getY() + diffY * this.sensitivity;
    newX = MathUtilities.clamp(newX, 0, Game.getScreenManager().getResolution().getWidth());
    newY = MathUtilities.clamp(newY, 0, Game.getScreenManager().getResolution().getHeight());

    this.location = new Point2D.Double(newX, newY);
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
