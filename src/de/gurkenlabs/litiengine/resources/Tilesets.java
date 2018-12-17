package de.gurkenlabs.litiengine.resources;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public class Tilesets extends ResourcesContainer<Tileset> {

  Tilesets() {
  }

  @Override
  protected Tileset load(String resourceName) throws TmxException {
    try {
      return XmlUtilities.readFromFile(Tileset.class, resourceName);
    } catch (JAXBException e) {
      throw new TmxException(e);
    }
  }
}
