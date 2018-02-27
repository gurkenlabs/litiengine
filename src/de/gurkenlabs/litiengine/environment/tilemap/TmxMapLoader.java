package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/**
 * This class allows to deserialzie a tmx file into an IMap instance.
 */
public final class TmxMapLoader implements IMapLoader {

  @Override
  public IMap loadMap(final String path) {
    final Map map = XmlUtilities.readFromFile(Map.class, path);
    if (map == null) {
      return null;
    }

    String basePath = FileUtilities.getParentDirPath(path);
    for (Tileset tilesets : map.getRawTileSets()) {
      tilesets.loadFromSource(basePath);
    }

    // by default the map is named by the source file
    String name = path;
    final int pos = name.lastIndexOf('.');
    if (pos > 0) {
      name = name.substring(0, pos);
    }

    int lastBackslash = name.lastIndexOf('/');
    if (lastBackslash != -1) {
      name = name.substring(lastBackslash + 1, name.length());
    } else {
      int lastForwardSlash = name.lastIndexOf('\\');
      if (lastForwardSlash != -1) {
        name = name.substring(lastForwardSlash + 1, name.length());
      }
    }

    map.setFileName(name);
    map.setPath(path);
    map.updateTileTerrain();

    return map;
  }
}