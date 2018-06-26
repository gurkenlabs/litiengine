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
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.util.MathUtilities;

/**
 * This class provides information about the mouse input in the
 * litiengine.
 */
public class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener {
  private static final Logger log = Logger.getLogger(Mouse.class.getName());

  private final List<Consumer<MouseEvent>> mouseClickedConsumer;
  private final List<Consumer<MouseEvent>> mouseDraggedConsumer;
  private final List<MouseListener> mouseListeners;
  private final List<MouseMotionListener> mouseMotionListeners;
  private final List<Consumer<MouseEvent>> mouseMovedConsumer;
  private final List<Consumer<MouseEvent>> mousePressedConsumer;
  private final List<Consumer<MouseEvent>> mouseReleasedConsumer;
  private final List<MouseWheelListener> mouseWheelListeners;
  private final List<Consumer<MouseWheelEvent>> wheelMovedConsumer;

  private final Robot robot;

  private final float sensitivity;
  private boolean grabMouse;

  private boolean pressed;
  private boolean isLeftMouseButtonDown;
  private boolean isRightMouseButtonDown;

  private Point2D lastLocation;
  private Point2D location;

  /**
   * Instantiates a new mouse.
   * 
   * @throws AWTException
   *           In case the {@link Robot} class could not be initialized.
   */
  public Mouse() throws AWTException {
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
      log.log(Level.SEVERE, "The mouse input could not be initialized.");
      throw e;
    }

    this.location = new Point2D.Double(Game.getCamera().getViewPort().getCenterX(), Game.getCamera().getViewPort().getCenterY());
    this.lastLocation = this.location;
    this.sensitivity = Game.getConfiguration().input().getMouseSensitivity();
    this.grabMouse = true;
  }
  
  /**
   * Gets the render location.
   *
   * @return the render location
   */
  public Point2D getLocation() {
    return this.location;
  }

  public Point2D getMapLocation() {
    return Game.getCamera().getMapLocation(new Point2D.Double(this.getLocation().getX() / Game.getCamera().getRenderScale(), this.getLocation().getY() / Game.getCamera().getRenderScale()));
  }

  public Point getTile() {
    return MapUtilities.getTile(this.getMapLocation());
  }

  public boolean isGrabMouse() {
    return this.grabMouse;
  }

  public boolean isLeftMouseButtonDown() {
    return this.isLeftMouseButtonDown;
  }

  public boolean isPressed() {
    return this.pressed;
  }

  public boolean isRightMouseButtonDown() {
    return this.isRightMouseButtonDown;
  }

  public void mouseClicked(final MouseEvent e) {
    this.setLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseClicked(wrappedEvent));

    for (final Consumer<MouseEvent> cons : this.mouseClickedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void mouseDragged(final MouseEvent e) {
    this.setLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseDragged(wrappedEvent));

    for (final Consumer<MouseEvent> cons : this.mouseDraggedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void mouseEntered(final MouseEvent e) {
    if (!this.grabMouse) {
      this.lastLocation = e.getPoint();
      this.location = e.getPoint();
    } else {
      this.setLocation(e);
    }

    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseEntered(wrappedEvent));
  }

  public void mouseExited(final MouseEvent e) {
    this.setLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseExited(wrappedEvent));
  }

  public void mouseMoved(final MouseEvent e) {
    this.setLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseMoved(wrappedEvent));

    for (final Consumer<MouseEvent> cons : this.mouseMovedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void mousePressed(final MouseEvent e) {
    this.setLocation(e);
    this.setPressed(true);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mousePressed(wrappedEvent));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = true;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = true;
    }

    for (final Consumer<MouseEvent> cons : this.mousePressedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void mouseReleased(final MouseEvent e) {
    this.setLocation(e);
    this.setPressed(false);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseReleased(wrappedEvent));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = false;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = false;
    }

    for (final Consumer<MouseEvent> cons : this.mouseReleasedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void mouseWheelMoved(final MouseWheelEvent e) {
    this.mouseWheelListeners.forEach(listener -> listener.mouseWheelMoved(e));
    this.wheelMovedConsumer.forEach(cons -> cons.accept(e));
  }

  public void onClicked(final Consumer<MouseEvent> consumer) {
    this.mouseClickedConsumer.add(consumer);
  }

  public void onDragged(final Consumer<MouseEvent> consumer) {
    this.mouseDraggedConsumer.add(consumer);
  }

  public void onMoved(final Consumer<MouseEvent> consumer) {
    this.mouseMovedConsumer.add(consumer);
  }

  public void onPressed(final Consumer<MouseEvent> consumer) {
    this.mousePressedConsumer.add(consumer);
  }

  public void onReleased(final Consumer<MouseEvent> consumer) {
    this.mouseReleasedConsumer.add(consumer);
  }

  public void onWheelMoved(final Consumer<MouseWheelEvent> consumer) {
    this.wheelMovedConsumer.add(consumer);
  }
  
  /**
   * Register mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseListener(final MouseListener listener) {
    if (this.mouseListeners.contains(listener)) {
      return;
    }

    this.mouseListeners.add(listener);
  }

  /**
   * Register mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseMotionListener(final MouseMotionListener listener) {
    if (this.mouseMotionListeners.contains(listener)) {
      return;
    }

    this.mouseMotionListeners.add(listener);
  }

  /**
   * Register mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void addMouseWheelListener(final MouseWheelListener listener) {
    if (this.mouseWheelListeners.contains(listener)) {
      return;
    }

    this.mouseWheelListeners.add(listener);
  }

  /**
   * If set to true, the mouse will be locked to the render component of the game.
   * 
   * @param grab True if the mouse should be grabbed to the {@link RenderComponent}, otherwise false.
   */
  public void setGrabMouse(final boolean grab) {
    this.grabMouse = grab;
  }

  public void setLocation(final Point2D adjustMouse) {
    if (adjustMouse == null) {
      return;
    }

    this.location = adjustMouse;
    this.lastLocation = adjustMouse;

    final MouseEvent mouseEvent = new MouseEvent(Game.getScreenManager().getRenderComponent(), MouseEvent.MOUSE_MOVED, 0, 0, (int) this.getLocation().getX(), (int) this.getLocation().getY(), 0, false, MouseEvent.NOBUTTON);
    final MouseEvent wrappedEvent = this.createEvent(mouseEvent);
    for (final Consumer<MouseEvent> cons : this.mouseMovedConsumer) {
      cons.accept(wrappedEvent);
    }
  }

  public void setLocation(double x, double y) {
    this.setLocation(new Point2D.Double(x, y));
  }

  /**
   * Unregister mouse listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseListener(final MouseListener listener) {
    this.mouseListeners.remove(listener);
  }

  /**
   * Unregister mouse motion listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseMotionListener(final MouseMotionListener listener) {
    this.mouseMotionListeners.remove(listener);
  }

  /**
   * Unregister mouse wheel listener.
   *
   * @param listener
   *          the listener
   */
  public void removeMouseWheelListener(final MouseWheelListener listener) {
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
