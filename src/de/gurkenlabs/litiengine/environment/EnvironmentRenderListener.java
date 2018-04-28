package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * This listener provides callbacks for different points during rendering an <code>IEnvironment</code>.
 * 
 * @see IEnvironment#render(Graphics2D)
 */
public interface EnvironmentRenderListener extends EventListener {
  /**
   * This method is called after <code>IMap</code> has been rendered.
   * 
   * @param g
   *          The graphics object of the rendering process.
   * 
   * @see IEnvironment#getMap()
   */
  public void mapRendered(Graphics2D g);

  /**
   * This method is called after everything with the {@link RenderType#GROUND} has been rendered.
   * 
   * @param g
   *          The graphics object of the rendering process.
   */
  public void groundRendered(Graphics2D g);

  /**
   * This method is called after all entities and everything with the {@link RenderType#NORMAL} has been rendered.
   * 
   * @param g
   *          The graphics object of the rendering process.
   */
  public void entitiesRendered(Graphics2D g);

  /**
   * This method is called after everything with the {@link RenderType#OVERLAY} has been rendered.
   * 
   * @param g
   *          The graphics object of the rendering process.
   */
  public void overlayRendered(Graphics2D g);

  /**
   * This method is called after the UI and everything with the {@link RenderType#UI} has been rendered.
   * 
   * @param g
   *          The graphics object of the rendering process.
   */
  public void uiRendered(Graphics2D g);
}
