package de.gurkenlabs.litiengine.environment;

import java.awt.Point;

import de.gurkenlabs.litiengine.entities.Direction;
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
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.SPAWNPOINT) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + SpawnpointMapObjectLoader.class);
    }

    final Direction direction = mapObject.getCustomProperty(MapObjectProperty.SPAWN_DIRECTION) != null ? Direction.valueOf(mapObject.getCustomProperty(MapObjectProperty.SPAWN_DIRECTION)) : Direction.DOWN;
    final Spawnpoint spawn = new Spawnpoint(mapObject.getId(), new Point(mapObject.getLocation()), direction);
    spawn.setName(mapObject.getName());
    spawn.setSize(mapObject.getWidth(), mapObject.getHeight());

    final String spawnType = mapObject.getCustomProperty(MapObjectProperty.SPAWN_TYPE);
    spawn.setSpawnType(spawnType);

    return spawn;
  }
}
