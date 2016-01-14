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
}
