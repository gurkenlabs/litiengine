package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public interface IMapObjectLoader {

  String getMapObjectType();

  IEntity load(IMapObject mapObject);
}
