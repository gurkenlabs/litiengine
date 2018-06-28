package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;

public class SpawnpointMapObjectLoader extends MapObjectLoader {

  protected SpawnpointMapObjectLoader() {
    super(MapObjectType.SPAWNPOINT);
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.SPAWNPOINT) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + SpawnpointMapObjectLoader.class);
    }

    final Direction direction = mapObject.getCustomProperty(MapObjectProperty.SPAWN_DIRECTION) != null ? Direction.valueOf(mapObject.getCustomProperty(MapObjectProperty.SPAWN_DIRECTION)) : Direction.DOWN;
    final String spawnType = mapObject.getCustomProperty(MapObjectProperty.SPAWN_TYPE);

    final Spawnpoint spawn = this.createSpawnpoint(mapObject, direction, spawnType);
    loadDefaultProperties(spawn, mapObject);

    Collection<IEntity> entities = new ArrayList<>();
    entities.add(spawn);
    return entities;
  }

  protected Spawnpoint createSpawnpoint(IMapObject mapObject, Direction direction, String spawnType) {
    return new Spawnpoint(direction, spawnType);
  }
}
