package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IEntityController extends IUpdateable {
  void attach();

  void detach();

  IEntity getEntity();
}
