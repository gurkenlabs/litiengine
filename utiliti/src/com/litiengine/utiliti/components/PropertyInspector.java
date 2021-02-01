package com.litiengine.utiliti.components;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectType;

public interface PropertyInspector extends Controller {
  void bind(IMapObject target);
  MapObjectType getObjectType();
  void setMapObjectType(MapObjectType type);
}
