package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

public interface IGuiComponent extends IRenderable {

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight();

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth();

  /**
   * Gets the x.
   *
   * @return the x
   */
  public int getX();

  /**
   * Gets the y.
   *
   * @return the y
   */
  public int getY();

  /**
   * Prepare.
   */
  public void prepare();

  public void setHeight(int height);

  public void setWidth(int width);

  /**
   * Suspend.
   */
  public void suspend();

  void setPosition(int x, int y);
  void setPosition(Point2D newPosition);

  void setDimension(int width, int height);
}
