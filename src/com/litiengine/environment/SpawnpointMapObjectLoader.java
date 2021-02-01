package com.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.Direction;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.Spawnpoint;

public class SpawnpointMapObjectLoader extends MapObjectLoader {

  protected SpawnpointMapObjectLoader() {
    super(MapObjectType.SPAWNPOINT);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final Direction direction = mapObject.getStringValue(MapObjectProperty.SPAWN_DIRECTION) != null ? Direction.valueOf(mapObject.getStringValue(MapObjectProperty.SPAWN_DIRECTION)) : Direction.DOWN;
    final String spawnType = mapObject.getStringValue(MapObjectProperty.SPAWN_INFO);

    final Spawnpoint spawn = this.createSpawnpoint(mapObject, direction, spawnType);
    loadDefaultProperties(spawn, mapObject);

    entities.add(spawn);
    return entities;
  }

  protected Spawnpoint createSpawnpoint(IMapObject mapObject, Direction direction, String spawnType) {
    return new Spawnpoint(direction, spawnType);
  }
}
