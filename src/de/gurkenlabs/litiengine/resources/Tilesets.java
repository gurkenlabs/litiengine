package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public class Tilesets extends ResourcesContainer<Tileset> {

  Tilesets() {
  }

  @Override
  protected Tileset load(URL resourceName) throws TmxException {
    try {
      Tileset tileset = XmlUtilities.readFromFile(Tileset.class, resourceName);
      tileset.finish(resourceName);
      return tileset;
    } catch (JAXBException e) {
      throw new TmxException(e);
    }
  }
}
