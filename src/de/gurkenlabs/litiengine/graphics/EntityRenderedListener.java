package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.util.EventListener;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EntityRenderedListener extends EventListener {
  public void rendered(Graphics2D g, IEntity entity);
}
