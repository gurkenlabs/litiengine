package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;

record InspectorNavigationTarget(IMap map, Type type, Object value) {
  enum Type {
    MAP,
    OBJECT,
    LAYER,
    SPRITE
  }

  static InspectorNavigationTarget map(IMap map) {
    return new InspectorNavigationTarget(map, Type.MAP, map);
  }

  static InspectorNavigationTarget object(IMap map, IMapObject mapObject) {
    return new InspectorNavigationTarget(map, Type.OBJECT, mapObject);
  }

  static InspectorNavigationTarget layer(IMap map, ILayer layer) {
    return new InspectorNavigationTarget(map, Type.LAYER, layer);
  }

  static InspectorNavigationTarget sprite(IMap map, SpritesheetResource sprite) {
    return new InspectorNavigationTarget(map, Type.SPRITE, sprite);
  }
}
