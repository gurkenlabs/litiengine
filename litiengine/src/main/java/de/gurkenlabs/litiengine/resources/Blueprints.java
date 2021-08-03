package de.gurkenlabs.litiengine.resources;

import java.io.IOException;
import java.net.URL;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public class Blueprints extends ResourcesContainer<Blueprint> {

  Blueprints() {
  }

  public static boolean isSupported(String fileName) {
    String extension = FileUtilities.getExtension(fileName);
    return extension != null && !extension.isEmpty() && (extension.equalsIgnoreCase(Blueprint.BLUEPRINT_FILE_EXTENSION) || extension.equalsIgnoreCase(Blueprint.TEMPLATE_FILE_EXTENSION));
  }

  @Override
  protected Blueprint load(URL resourceName) throws Exception {
    Blueprint blueprint;
    try {
      blueprint = XmlUtilities.read(Blueprint.class, resourceName);
    } catch (IOException e) {
      throw new TmxException("could not parse xml data", e);
    }

    return blueprint;
  }

  @Override
  protected String getAlias(String resourceName, Blueprint resource) {
    if (resource == null || resource.getName() == null || resource.getName().isEmpty() || resource.getName().equalsIgnoreCase(resourceName)) {
      return null;
    }

    return resource.getName();
  }
}
