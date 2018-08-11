package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EntityDebugRenderedListener {
  public void entityRendered(Graphics2D g, IEntity entity);
}
