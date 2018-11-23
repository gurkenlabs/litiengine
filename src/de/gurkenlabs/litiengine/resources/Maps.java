package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapLoader;

public final class Maps extends ResourcesContainer<IMap> {

  protected Maps() {
  }

  @Override
  protected IMap load(String resourceName) {
    return MapLoader.load(resourceName);
  }
}
