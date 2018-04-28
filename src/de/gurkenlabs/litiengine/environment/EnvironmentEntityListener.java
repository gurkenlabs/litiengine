package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EnvironmentEntityListener extends EventListener {
  public void entityAdded(IEntity entity);

  public void entityRemoved(IEntity entity);
}
