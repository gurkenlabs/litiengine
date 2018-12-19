package de.gurkenlabs.litiengine.resources;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public final class Maps extends ResourcesContainer<IMap> {

  Maps() {
  }

  public static boolean isSupported(String fileName) {
    String extension = FileUtilities.getExtension(fileName);
    return extension != null && !extension.isEmpty() && extension.equalsIgnoreCase(Map.FILE_EXTENSION);
  }

  @Override
  protected IMap load(String resourceName) throws TmxException {
    if (!isSupported(resourceName)) {
      return null;
    }

    Map map;
    try {
      map = XmlUtilities.readFromFile(Map.class, resourceName);
    } catch (JAXBException e) {
      throw new TmxException("could not parse xml data", e);
    }
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

  @Override
  protected String getAlias(String resourceName, IMap resource) {
    if (resource == null || resource.getName() == null || resource.getName().isEmpty() || resource.getName().equalsIgnoreCase(resourceName)) {
      return null;
    }

    return resource.getName();
  }
}
