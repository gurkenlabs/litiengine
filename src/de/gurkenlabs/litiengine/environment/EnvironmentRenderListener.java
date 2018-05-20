package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * This listener provides call backs for different points during rendering an <code>IEnvironment</code>.
 * 
 * @see IEnvironment#render(Graphics2D)
 */
public interface EnvironmentRenderListener extends EventListener {
  /**
   * This method is called after the <code>IEnvironment</code> rendered everything of the specified <code>RenderType</code>.
   */
  public void rendered(Graphics2D g, RenderType type);
}
