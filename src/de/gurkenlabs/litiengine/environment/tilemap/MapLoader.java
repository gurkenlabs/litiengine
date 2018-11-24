package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/**
 * This class allows to deserialize a tmx file into an IMap instance.
 */
public final class MapLoader {
  private MapLoader() {
    throw new UnsupportedOperationException();
  }

  public static IMap load(final String path) {
    final Map map = XmlUtilities.readFromFile(Map.class, path);
    if (map == null) {
      return null;
    }

    String basePath = FileUtilities.getParentDirPath(path);
    for (Tileset tilesets : map.getRawTilesets()) {
      tilesets.loadFromSource(basePath);
    }
    
    for (TileLayer layer : map.getRawTileLayers()) {
      layer.setCustomPropertySources(map);
    }

    // by default the map is named by the source file
    String name = FileUtilities.getFileName(path);

    map.setName(name);
    map.setPath(path);
    map.updateTileTerrain();

    return map;
  }
}
