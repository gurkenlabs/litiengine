package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class ColliderMapObjectLoader extends MapObjectLoader {

  public ColliderMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject + " with a loader of the type " + ColliderMapObjectLoader.class);
    }

    final String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    boolean isObstacle = true;
    if (obstacle != null && !obstacle.isEmpty()) {
      isObstacle = Boolean.valueOf(obstacle);
    }

    final Collider col = new Collider(isObstacle);
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());
    col.setMapId(mapObject.getId());
    col.setName(mapObject.getName());

    return col;
  }
}
