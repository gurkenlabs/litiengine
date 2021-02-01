package com.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.MapArea;

public class MapAreaMapObjectLoader extends MapObjectLoader {

  protected MapAreaMapObjectLoader() {
    super(MapObjectType.AREA);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    MapArea mapArea = this.createMapArea(mapObject);
    loadDefaultProperties(mapArea, mapObject);

    entities.add(mapArea);
    return entities;
  }

  protected MapArea createMapArea(IMapObject mapObject) {
    return new MapArea();
  }
}
