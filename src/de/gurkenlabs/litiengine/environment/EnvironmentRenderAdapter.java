package de.gurkenlabs.litiengine.environment;

import java.awt.Graphics2D;

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
