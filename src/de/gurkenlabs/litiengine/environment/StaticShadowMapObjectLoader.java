package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;

public class StaticShadowMapObjectLoader extends MapObjectLoader {

  protected StaticShadowMapObjectLoader() {
    super(MapObjectType.STATICSHADOW);
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.STATICSHADOW) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + MapAreaMapObjectLoader.class);
    }

    Collection<IEntity> entities = new ArrayList<>();
    StaticShadowType type = mapObject.getEnumValue(MapObjectProperty.SHADOW_TYPE, StaticShadowType.class, StaticShadowType.DOWN);
    int offset = mapObject.getIntValue(MapObjectProperty.SHADOW_OFFSET, StaticShadow.DEFAULT_OFFSET);

    StaticShadow shadow = this.createStaticShadow(mapObject, type, offset);
    loadDefaultProperties(shadow, mapObject);

    shadow.setOffset(offset);
    entities.add(shadow);
    return entities;
  }

  protected StaticShadow createStaticShadow(IMapObject mapObject, StaticShadowType type, int offset) {
    return new StaticShadow(type, offset);
  }
}
