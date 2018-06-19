package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/**
 * This class allows to deserialize a tmx file into an IMap instance.
 */
public final class MapLoader {
  private MapLoader() {
  }

  public static IMap load(final String path) {
    final Map map = XmlUtilities.readFromFile(Map.class, path);
    if (map == null) {
      return null;
    }

    String basePath = FileUtilities.getParentDirPath(path);
    for (Tileset tilesets : map.getRawTileSets()) {
      tilesets.loadFromSource(basePath);
    }

    // by default the map is named by the source file
    String name = FileUtilities.getFileName(path);

    map.setFileName(name);
    map.setPath(path);
    map.updateTileTerrain();

    return map;
  }
}
