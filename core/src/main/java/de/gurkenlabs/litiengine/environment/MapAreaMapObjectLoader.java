package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

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

    MapArea mapArea = new MapArea();
    loadDefaultProperties(mapArea, mapObject);
    initShape(mapArea, mapObject);

    entities.add(mapArea);
    return entities;
  }

  private static void initShape(MapArea area, IMapObject mapObject) {
    if (mapObject.isPolygon() || mapObject.isPolyline()) {
      area.setShape(MapUtilities.convertPolyshapeToPath(mapObject));
    } else if (mapObject.isEllipse()) {
      area.setShape(mapObject.getEllipse());
    }
  }
}
