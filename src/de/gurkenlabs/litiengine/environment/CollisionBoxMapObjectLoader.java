package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  protected CollisionBoxMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CollisionBoxMapObjectLoader.class);
    }

    boolean isObstacle = mapObject.getCustomPropertyBool(MapObjectProperty.OBSTACLE, true);
    boolean isObstructingLight = mapObject.getCustomPropertyBool(MapObjectProperty.OBSTRUCTINGLIGHTS);

    final CollisionBox col = new CollisionBox(isObstacle, isObstructingLight);
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getWidth(), mapObject.getHeight());
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());
    col.setMapId(mapObject.getId());
    col.setName(mapObject.getName());

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(col);

    if (isObstructingLight) {
      entities.add(new StaticShadow(col));
    }

    return entities;
  }
}
