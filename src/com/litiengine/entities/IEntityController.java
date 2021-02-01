package com.litiengine.entities;

import com.litiengine.IUpdateable;

public interface IEntityController extends IUpdateable {
  void attach();

  void detach();

  IEntity getEntity();
}
