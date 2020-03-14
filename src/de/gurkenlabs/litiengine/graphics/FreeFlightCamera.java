package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.input.Input;

public class FreeFlightCamera extends Camera implements IUpdateable {
  private static final double DEFAULT_SCROLL_PIXELS_PER_SECOND = 400;
  private static final double DEFAULT_SCROLL_PADDING = 20;

  private double velocity;
  private double scrollPadding;

  /**
   * Initializes a new instance of the <code>FreeFlightCamera</code>.
   */
  public FreeFlightCamera() {
    this(0, 0);
  }

  /**
   * Initializes a new instance of the <code>FreeFlightCamera</code> with the specified initial focus.
   *
   * @param x
   *          The x-coordinate of the initial focus of this instance.
   * @param y
   *          The y-coordinate of the initial focus of this instance.
   */
  public FreeFlightCamera(double x, double y) {
    this(new Point2D.Double(x, y));
  }

  /**
   * Initializes a new instance of the <code>FreeFlightCamera</code> with the specified initial focus.
   *
   *
   * @param focus
   *          The initial focus of this instance.
   */
  public FreeFlightCamera(final Point2D focus) {
    this.setFocus(focus);
    this.velocity = DEFAULT_SCROLL_PIXELS_PER_SECOND;
    this.scrollPadding = DEFAULT_SCROLL_PADDING;
    this.setClampToMap(true);

    Game.loop().attach(this);
  }

  public double getVelocity() {
    return this.velocity;
  }

  public double getScrollPadding() {
    return this.scrollPadding;
  }

  public void setVelocity(double velocity) {
    this.velocity = velocity;
  }

  public void setScrollPadding(double scrollPadding) {
    this.scrollPadding = scrollPadding;
  }

  @Override
  public void update() {
    this.handleFreeFlightCamera();
  }

  private void handleFreeFlightCamera() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    final Point2D mouseLocation = Input.mouse().getLocation();

    final double scrollSpeed = this.getVelocity() / (double) Game.loop().getTickRate() * Game.config().input().getMouseSensitivity();

    double x = this.getFocus().getX();
    double y = this.getFocus().getY();

    double deltaX = 0;
    if (mouseLocation.getX() < this.getScrollPadding()) {
      deltaX -= scrollSpeed;
    } else if (Game.window().getResolution().getWidth() - mouseLocation.getX() < this.getScrollPadding()) {
      deltaX += scrollSpeed;
    }

    double deltaY = 0;
    if (mouseLocation.getY() < this.getScrollPadding()) {
      deltaY -= scrollSpeed;
    } else if (Game.window().getResolution().getHeight() - mouseLocation.getY() < this.getScrollPadding()) {
      deltaY += scrollSpeed;
    }

    x += deltaX;
    y += deltaY;

    this.setFocus(new Point2D.Double(x, y));
  }
}