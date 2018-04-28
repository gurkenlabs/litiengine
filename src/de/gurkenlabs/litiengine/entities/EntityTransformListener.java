package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

public interface EntityTransformListener extends EventListener {
  public void locationChanged(IEntity entity);

  public void sizeChanged(IEntity entity);
}
