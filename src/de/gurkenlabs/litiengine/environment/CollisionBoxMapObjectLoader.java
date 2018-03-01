package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.StaticShadow;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  protected CollisionBoxMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CollisionBoxMapObjectLoader.class);
    }

    boolean isObstacle = mapObject.getCustomPropertyBool(MapObjectProperty.PROP_OBSTACLE, true);
    boolean isObstructingLight = mapObject.getCustomPropertyBool(MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS);

    final CollisionBox col = new CollisionBox(isObstacle, isObstructingLight);
    loadDefaultProperties(col, mapObject);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());

    Collection<IEntity> entities = super.load(environment, mapObject);
    entities.add(col);

    if (isObstructingLight) {
      entities.add(new StaticShadow(col));
    }

    return entities;
  }
}
