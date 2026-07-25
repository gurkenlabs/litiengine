package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import java.util.List;

public interface PropertyInspector extends Controller {
  void bind(IMapObject target);

  default void bindAll(List<IMapObject> targets) {
    bind(targets == null || targets.isEmpty() ? null : targets.get(0));
  }

  MapObjectType getObjectType();

  void setMapObjectType(MapObjectType type);
}
