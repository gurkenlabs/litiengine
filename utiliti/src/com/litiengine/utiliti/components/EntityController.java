package com.litiengine.utiliti.components;

import com.litiengine.environment.tilemap.IMapObject;

public interface EntityController extends Controller {
  public void select(IMapObject mapObject);
}
