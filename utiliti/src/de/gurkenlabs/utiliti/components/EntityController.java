package de.gurkenlabs.utiliti.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public interface EntityController extends Controller {
  public void select(IMapObject mapObject);
}
