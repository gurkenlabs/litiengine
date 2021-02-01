package com.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.entities.CollisionBox;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.StaticShadow;

public class CollisionBoxMapObjectLoader extends MapObjectLoader {

  protected CollisionBoxMapObjectLoader() {
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
