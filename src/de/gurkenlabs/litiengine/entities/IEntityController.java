package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IEntityController extends IUpdateable {
  public void attach();

  public void detach();

  public IEntity getEntity();
}
