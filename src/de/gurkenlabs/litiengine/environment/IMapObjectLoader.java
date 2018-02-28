package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public interface IMapObjectLoader {

  String getMapObjectType();

  Collection<IEntity> load(IEnvironment environment, IMapObject mapObject);
}
