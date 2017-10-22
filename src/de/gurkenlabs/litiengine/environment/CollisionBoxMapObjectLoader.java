package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  public CollisionBoxMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CollisionBoxMapObjectLoader.class);
    }

    final String obstacle = mapObject.getCustomProperty(MapObjectProperty.OBSTACLE);
    boolean isObstacle = true;
    if (obstacle != null && !obstacle.isEmpty()) {
      isObstacle = Boolean.valueOf(obstacle);
    }

    final CollisionBox col = new CollisionBox(isObstacle);
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());
    col.setMapId(mapObject.getId());
    col.setName(mapObject.getName());

    return col;
  }
}
