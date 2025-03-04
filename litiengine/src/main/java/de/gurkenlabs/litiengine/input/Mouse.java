package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/** This implementation provides information about the mouse input in the LITIENGINE. */
public final class Mouse
  implements MouseListener, MouseMotionListener, MouseWheelListener, IMouse, IUpdateable {
  private static final Logger log = Logger.getLogger(Mouse.class.getName());

  private final Collection<MouseClickedListener> mouseClickedListeners =
    ConcurrentHashMap.newKeySet();
  private final Collection<MouseDraggedListener> mouseDraggedListeners =
    ConcurrentHashMap.newKeySet();
  private final Collection<MouseMovedListener> mouseMovedListeners = ConcurrentHashMap.newKeySet();
  private final Collection<MousePressedListener> mousePressedListeners =
    ConcurrentHashMap.newKeySet();
  private final Collection<MousePressingListener> mousePressingListeners =
    ConcurrentHashMap.newKeySet();
  private final Collection<MouseReleasedListener> mouseReleasedListeners =
    ConcurrentHashMap.newKeySet();

  private final Collection<MouseListener> mouseListeners = ConcurrentHashMap.newKeySet();
  private final Collection<MouseMotionListener> mouseMotionListeners =
    ConcurrentHashMap.newKeySet();
  private final Collection<MouseWheelListener> mouseWheelListeners = ConcurrentHashMap.newKeySet();

  private final Robot robot;

  private final float sensitivity;
  private boolean grabMouse;

  private boolean pressed;
  private boolean isLeftMouseButtonDown;
  private boolean isRightMouseButtonDown;

  private Point2D lastLocation;
  private Point2D location;

  private MouseEvent updateLocation;
  private boolean updatingLocation;

  /**
   * Instantiates a new mouse.
   *
   * @throws AWTException
   *   In case the {@link Robot} class could not be initialized.
   */
  Mouse() throws AWTException {
    try {
      this.robot = new Robot();
      this.robot.setAutoDelay(0);
    } catch (final AWTException e) {
      log.log(Level.SEVERE, "The mouse input could not be initialized.");
      throw e;
    }

    this.location =
      new Point2D.Double(
        Game.world().camera().getViewport().getCenterX(),
        Game.world().camera().getViewport().getCenterY());
    this.lastLocation = this.location;
    this.sensitivity = Game.config().input().getMouseSensitivity();
    this.grabMouse = false;
  }

  @Override
  public void update() {
    if (this.isPressed()) {
      for (final MousePressingListener listener : this.mousePressingListeners) {
        listener.mousePressing();
      }
    }
    if (this.updateLocation != null && !this.updatingLocation) {
      this.updatingLocation = true;
      try {
        this.setLocation(this.updateLocation);
        this.updateLocation = null;
      } finally {
        this.updatingLocation = false;
      }
    }
  }

  @Override
  public Point2D getLocation() {
    return this.location;
  }

  @Override
  public Point2D getMapLocation() {
    if (Game.world().camera() == null) {
      return null;
    }
    return Game.world()
      .camera()
      .getMapLocation(
        new Point2D.Double(
          this.getLocation().getX() / Game.world().camera().getRenderScale(),
          this.getLocation().getY() / Game.world().camera().getRenderScale()));
  }

  @Override
  public Point getTile() {
    return MapUtilities.getTile(this.getMapLocation());
  }

  @Override
  public boolean isGrabMouse() {
    return this.grabMouse;
  }

  @Override
  public boolean isLeftButtonPressed() {
    return this.isLeftMouseButtonDown;
  }

  @Override
  public boolean isPressed() {
    return this.pressed;
  }

  @Override
  public boolean isRightButtonPressed() {
    return this.isRightMouseButtonDown;
  }

  @Override
  public boolean isLeftButton(MouseEvent event) {
    return SwingUtilities.isLeftMouseButton(event);
  }

  @Override
  public boolean isRightButton(MouseEvent event) {
    return SwingUtilities.isRightMouseButton(event);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    this.updateLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseClicked(wrappedEvent));

    for (final MouseClickedListener listener : this.mouseClickedListeners) {
      listener.mouseClicked(wrappedEvent);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    this.updateLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseDragged(wrappedEvent));

    for (final MouseDraggedListener listener : this.mouseDraggedListeners) {
      listener.mouseDragged(wrappedEvent);
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    if (!this.grabMouse) {
      this.lastLocation = e.getPoint();
      this.location = e.getPoint();
    } else {
      this.updateLocation(e);
    }

    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseEntered(wrappedEvent));
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    this.updateLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseExited(wrappedEvent));
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    this.updateLocation(e);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseMotionListeners.forEach(listener -> listener.mouseMoved(wrappedEvent));

    for (final MouseMovedListener listener : this.mouseMovedListeners) {
      listener.mouseMoved(wrappedEvent);
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    this.updateLocation(e);
    this.setPressed(true);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mousePressed(wrappedEvent));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = true;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = true;
    }

    for (final MousePressedListener listener : this.mousePressedListeners) {
      listener.mousePressed(wrappedEvent);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    this.updateLocation(e);
    this.setPressed(false);
    final MouseEvent wrappedEvent = this.createEvent(e);
    this.mouseListeners.forEach(listener -> listener.mouseReleased(wrappedEvent));

    if (SwingUtilities.isLeftMouseButton(e)) {
      this.isLeftMouseButtonDown = false;
    }

    if (SwingUtilities.isRightMouseButton(e)) {
      this.isRightMouseButtonDown = false;
    }

    for (final MouseReleasedListener listener : this.mouseReleasedListeners) {
      listener.mouseReleased(wrappedEvent);
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    this.mouseWheelListeners.forEach(listener -> listener.mouseWheelMoved(e));
  }

  @Override
  public void onClicked(final MouseClickedListener listener) {
    this.mouseClickedListeners.add(listener);
  }

  @Override
  public void removeMouseClickedListener(MouseClickedListener listener) {
    this.mouseClickedListeners.remove(listener);
  }

  @Override
  public void onDragged(final MouseDraggedListener listener) {
    this.mouseDraggedListeners.add(listener);
  }

  @Override
  public void removeMouseDraggedListener(MouseDraggedListener listener) {
    this.mouseDraggedListeners.remove(listener);
  }

  @Override
  public void onMoved(final MouseMovedListener listener) {
    this.mouseMovedListeners.add(listener);
  }

  @Override
  public void removeMouseMovedListener(MouseMovedListener listener) {
    this.mouseMovedListeners.remove(listener);
  }

  @Override
  public void onPressed(final MousePressedListener listener) {
    this.mousePressedListeners.add(listener);
  }

  @Override
  public void removeMousePressedListener(MousePressedListener listener) {
    this.mousePressedListeners.remove(listener);
  }

  @Override
  public void onPressing(MousePressingListener listener) {
    this.mousePressingListeners.add(listener);
  }

  @Override
  public void removeMousePressingListener(MousePressingListener listener) {
    this.mousePressingListeners.remove(listener);
  }

  @Override
  public void onReleased(final MouseReleasedListener listener) {
    this.mouseReleasedListeners.add(listener);
  }

  @Override
  public void removeMouseReleasedListener(MouseReleasedListener listener) {
    this.mouseReleasedListeners.remove(listener);
  }

  @Override
  public void onWheelMoved(final MouseWheelListener listener) {
    this.mouseWheelListeners.add(listener);
  }

  @Override
  public void removeMouseWheelListener(final MouseWheelListener listener) {
    this.mouseWheelListeners.remove(listener);
  }

  @Override
  public void addMouseListener(final MouseListener listener) {
    if (this.mouseListeners.contains(listener)) {
      return;
    }

    this.mouseListeners.add(listener);
  }

  @Override
  public void removeMouseListener(final MouseListener listener) {
    this.mouseListeners.remove(listener);
  }

  @Override
  public void addMouseMotionListener(final MouseMotionListener listener) {
    if (this.mouseMotionListeners.contains(listener)) {
      return;
    }

    this.mouseMotionListeners.add(listener);
  }

  @Override
  public void removeMouseMotionListener(final MouseMotionListener listener) {
    this.mouseMotionListeners.remove(listener);
  }

  @Override
  public void clearExplicitListeners() {
    this.mouseClickedListeners.clear();
    this.mouseDraggedListeners.clear();
    this.mouseMovedListeners.clear();
    this.mousePressedListeners.clear();
    this.mousePressingListeners.clear();
    this.mouseReleasedListeners.clear();
  }

  @Override
  public void setGrabMouse(final boolean grab) {
    this.grabMouse = grab;

    if (this.isGrabMouse()) {
      Game.window().cursor().hideDefaultCursor();
    } else if (!Game.window().cursor().isVisible()) {
      Game.window().cursor().showDefaultCursor();
    }
  }

  @Override
  public void setLocation(final Point2D adjustMouse) {
    if (adjustMouse == null) {
      return;
    }

    this.location = adjustMouse;
    this.lastLocation = adjustMouse;

    final MouseEvent mouseEvent =
      new MouseEvent(
        Game.window().getRenderComponent(),
        MouseEvent.MOUSE_MOVED,
        0,
        0,
        (int) this.getLocation().getX(),
        (int) this.getLocation().getY(),
        0,
        false,
        MouseEvent.NOBUTTON);
    final MouseEvent wrappedEvent = this.createEvent(mouseEvent);
    for (final MouseMovedListener listener : this.mouseMovedListeners) {
      listener.mouseMoved(wrappedEvent);
    }
  }

  @Override
  public void setLocation(double x, double y) {
    this.setLocation(new Point2D.Double(x, y));
  }

  private MouseEvent createEvent(final MouseEvent original) {
    return new MouseEvent(
      original.getComponent(),
      original.getID(),
      original.getWhen(),
      original.getModifiersEx(),
      (int) this.getLocation().getX(),
      (int) this.getLocation().getY(),
      original.getXOnScreen(),
      original.getYOnScreen(),
      original.getClickCount(),
      original.isPopupTrigger(),
      original.getButton());
  }

  /**
   * Calculates the location of the ingame mouse by the position diff and locks the original mouse to the center of the screen.
   *
   * @param e
   *   The event containing information about the original mouse.
   */
  private void setLocation(final MouseEvent e) {
    if (this.grabMouse && !Game.window().isFocusOwner()) {
      return;
    }

    double diffX;
    double diffY;
    if (!this.grabMouse) {
      // get diff relative from last mouse location
      diffX = e.getX() - this.lastLocation.getX();
      diffY = e.getY() - this.lastLocation.getY();
      this.lastLocation = new Point(e.getX(), e.getY());
    } else {
      // get diff relative from grabbed position
      final double screenCenterX = Game.window().getResolution().getWidth() * 0.5;
      final double screenCenterY = Game.window().getResolution().getHeight() * 0.5;
      final Point screenLocation = Game.window().getLocationOnScreen();
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
    newX = Math.clamp(newX, 0, Game.window().getResolution().getWidth());
    newY = Math.clamp(newY, 0, Game.window().getResolution().getHeight());

    this.location = new Point2D.Double(newX, newY);
  }

  /**
   * Sets the pressed.
   *
   * @param pressed
   *   the new pressed
   */
  private void setPressed(final boolean pressed) {
    this.pressed = pressed;
  }

  private void updateLocation(MouseEvent mouseEvent) {
    if (this.updatingLocation) {
      return;
    }

    this.updateLocation = mouseEvent;
  }
}
