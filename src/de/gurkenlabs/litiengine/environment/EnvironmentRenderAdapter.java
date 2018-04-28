package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;

/**
 * An abstract implementation of a <code>EnvironmentRenderListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentRenderListener
 */
public abstract class EnvironmentRenderAdapter implements EnvironmentRenderListener {

  @Override
  public void mapRendered(Graphics2D g) {
  }

  @Override
  public void groundRendered(Graphics2D g) {
  }

  @Override
  public void entitiesRendered(Graphics2D g) {
  }

  @Override
  public void overlayRendered(Graphics2D g) {
  }

  @Override
  public void uiRendered(Graphics2D g) {
  }
}
