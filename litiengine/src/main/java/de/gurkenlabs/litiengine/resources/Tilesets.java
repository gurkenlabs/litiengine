package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Manages the loading and storage of Tileset resources.
 */
public class Tilesets extends ResourcesContainer<Tileset> {

  /**
   * Default constructor for the Tilesets class. This constructor is kept for serialization purposes.
   */
  Tilesets() {
  }

  /**
   * Loads a Tileset from the specified URL.
   *
   * @param resourceName The URL of the resource to load.
   * @return The loaded Tileset.
   * @throws IOException        If an I/O error occurs.
   * @throws URISyntaxException If the URL is not formatted correctly.
   */
  @Override
  protected Tileset load(URL resourceName) throws IOException, URISyntaxException {
    try {
      Tileset tileset = XmlUtilities.read(Tileset.class, resourceName);
      if (tileset != null) {
        tileset.finish(resourceName);
      }
      return tileset;
    } catch (JAXBException e) {
      throw new TmxException(e);
    }
  }
}
