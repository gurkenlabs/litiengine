package de.gurkenlabs.utiliti.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public interface PropertyInspector extends Controller {
  void bind(IMapObject target);

  MapObjectType getObjectType();

  void setMapObjectType(MapObjectType type);
}
