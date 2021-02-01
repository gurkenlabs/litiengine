package com.litiengine.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import com.litiengine.environment.tilemap.xml.Tileset;
import com.litiengine.environment.tilemap.xml.TmxException;
import com.litiengine.util.io.XmlUtilities;

public class Tilesets extends ResourcesContainer<Tileset> {

  Tilesets() {
  }

  @Override
  protected Tileset load(URL resourceName) throws IOException, URISyntaxException {
    try {
      Tileset tileset = XmlUtilities.read(Tileset.class, resourceName);
      tileset.finish(resourceName);
      return tileset;
    } catch (JAXBException e) {
      throw new TmxException(e);
    }
  }
}
