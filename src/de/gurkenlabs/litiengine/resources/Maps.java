package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public final class Maps extends ResourcesContainer<IMap> {

  Maps() {
  }

  @Override
  protected IMap load(String resourceName) {
    final Map map = XmlUtilities.readFromFile(Map.class, resourceName);
    if (map == null) {
      return null;
    }

    String basePath = FileUtilities.getParentDirPath(resourceName);
    for (Tileset tilesets : map.getRawTilesets()) {
      tilesets.loadFromSource(basePath);
    }

    for (TileLayer layer : map.getRawTileLayers()) {
      layer.setTilesetEntries(map);
    }

    // by default the map is named by the source file
    String name = FileUtilities.getFileName(resourceName);

    map.setName(name);
    map.setPath(resourceName);
    map.updateTileTerrain();

    return map;
  }
}
