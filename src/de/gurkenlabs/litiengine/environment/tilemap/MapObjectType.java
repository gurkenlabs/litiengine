package de.gurkenlabs.litiengine.environment.tilemap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum MapObjectType {
  PROP, EMITTER, SPAWNPOINT, SPAWNPOINT_CREEP, LANE, MOB, DECORMOB, COLLISIONBOX, LIGHTSOURCE, TRIGGER, SPAWNPOINT_PICKUP, UNKNOWN;

  public static MapObjectType get(String mapObjectType) {
    if (mapObjectType == null || mapObjectType.isEmpty()) {
      return MapObjectType.UNKNOWN;
    }

    try {
      return MapObjectType.valueOf(mapObjectType);
    } catch (IllegalArgumentException iae) {
      return MapObjectType.UNKNOWN;
    }
  }
  
  public static MapObjectType fromOrdinal(int n) {return values()[n];}
}
