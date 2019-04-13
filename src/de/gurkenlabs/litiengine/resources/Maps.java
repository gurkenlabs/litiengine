package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public final class Maps extends ResourcesContainer<IMap> {

  Maps() {
  }

  public static boolean isSupported(String fileName) {
    String extension = FileUtilities.getExtension(fileName);
    return extension != null && !extension.isEmpty() && extension.equalsIgnoreCase(TmxMap.FILE_EXTENSION);
  }

  @Override
  protected IMap load(URL resourceName) throws TmxException {
    TmxMap map;
    try {
      map = XmlUtilities.readFromFile(TmxMap.class, resourceName);
    } catch (JAXBException e) {
      throw new TmxException("could not parse xml data", e);
    }
    if (map == null) {
      return null;
    }
    map.finish(resourceName);
    return map;
  }

  @Override
  protected String getAlias(URL resourceName, IMap resource) {
    if (resource == null || resource.getName() == null || resource.getName().isEmpty() || resource.getName().equalsIgnoreCase(resourceName.getFile())) {
      return null;
    }

    return resource.getName();
  }
}
