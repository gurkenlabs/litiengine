package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

public interface IGuiComponent extends IRenderable {

  /**
   * Gets the height.
   *
   * @return the height
   */
  public double getHeight();

  /**
   * Gets the width.
   *
   * @return the width
   */
  public double getWidth();

  /**
   * Gets the x.
   *
   * @return the x
   */
  public double getX();

  /**
   * Gets the y.
   *
   * @return the y
   */
  public double getY();

  /**
   * Prepare.
   */
  public void prepare();

  public void setHeight(double height);

  public void setWidth(double width);

  /**
   * Suspend.
   */
  public void suspend();

  void setDimension(double width, double height);

  void setPosition(double x, double y);

  void setPosition(Point2D newPosition);
}
