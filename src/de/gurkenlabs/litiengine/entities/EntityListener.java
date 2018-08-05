package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

public interface EntityListener extends EventListener {

  public void loaded(IEntity entity);

  public void removed(IEntity entity);
}
