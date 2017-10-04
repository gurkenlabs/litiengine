package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public abstract class MapObjectLoader implements IMapObjectLoader {
  private final String mapObjectType;

  protected MapObjectLoader(String mapObjectType) {
    this.mapObjectType = mapObjectType;
  }

  protected MapObjectLoader(MapObjectType mapObjectType) {
    this.mapObjectType = mapObjectType.name();
  }

  @Override
  public String getMapObjectTypeQ() {
    return this.mapObjectType;
  }
}
