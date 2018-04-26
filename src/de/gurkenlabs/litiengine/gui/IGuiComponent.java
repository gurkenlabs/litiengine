package de.gurkenlabs.litiengine.gui;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.IRenderable;

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

  void setLocation(double x, double y);

  void setLocation(Point2D newPosition);
}
