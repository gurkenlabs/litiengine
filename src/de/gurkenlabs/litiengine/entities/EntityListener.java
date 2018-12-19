package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

import de.gurkenlabs.litiengine.environment.IEnvironment;

public interface EntityListener extends EventListener {

  public void loaded(IEntity entity, IEnvironment environment);

  public void removed(IEntity entity, IEnvironment environment);
}
