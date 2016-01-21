package de.gurkenlabs.litiengine.graphics;

public interface IGuiComponent extends IRenderable {

  /**
   * Prepare.
   */
  public void prepare();

  /**
   * Suspend.
   */
  public void suspend();

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth();

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight();

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
  
  public void setWidth(int width);
  public void setHeight(int height);
}
