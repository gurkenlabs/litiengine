package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  public CollisionBoxMapObjectLoader() {
    super(MapObjectType.COLLISIONBOX);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final CollisionBox col = this.createCollisionBox(mapObject);
    loadDefaultProperties(col, mapObject);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());

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
