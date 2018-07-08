package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class MapAreaMapObjectLoader extends MapObjectLoader {

  protected MapAreaMapObjectLoader() {
    super(MapObjectType.AREA);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.AREA) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + MapAreaMapObjectLoader.class);
    }

    Collection<IEntity> entities = new ArrayList<>();
    MapArea mapArea = this.createMapArea(mapObject);
    loadDefaultProperties(mapArea, mapObject);

    entities.add(mapArea);
    return entities;
  }

  protected MapArea createMapArea(IMapObject mapObject) {
    return new MapArea();
  }
}
