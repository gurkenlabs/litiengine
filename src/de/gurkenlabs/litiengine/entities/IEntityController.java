package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IEntityController extends IUpdateable, IEntityProvider{
  public void attach();
  public void detach();
}
