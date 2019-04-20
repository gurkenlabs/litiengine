package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.physics.Collision;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  protected CollisionBoxMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CollisionBoxMapObjectLoader.class);
    }

    final CollisionBox col = this.createCollisionBox(mapObject);
    loadDefaultProperties(col, mapObject);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());
    
    Collection<IEntity> entities = new ArrayList<>();
    entities.add(col);

    if (col.isObstructingLight()) {
      entities.add(new StaticShadow(col));
    }

    return entities;
  }

  protected CollisionBox createCollisionBox(IMapObject mapObject) {
    return new CollisionBox();
  }
}
