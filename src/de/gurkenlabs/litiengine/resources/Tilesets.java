package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public class Tilesets extends ResourcesContainer<Tileset> {

  Tilesets() {
  }

  @Override
  protected Tileset load(String resourceName) {
    return XmlUtilities.readFromFile(Tileset.class, resourceName);
  }
}
