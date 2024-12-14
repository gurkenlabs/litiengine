package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import jakarta.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/**
 * A class that manages the loading and handling of Blueprint resources.
 */
public class Blueprints extends ResourcesContainer<Blueprint> {

  /**
   * Constructs a new Blueprints container.
   */
  Blueprints() {}

  /**
   * Checks if the given file name is supported by this container.
   *
   * @param fileName The name of the file to check.
   * @return true if the file is supported, false otherwise.
   */
  public static boolean isSupported(String fileName) {
    String extension = FileUtilities.getExtension(fileName);
    return !extension.isEmpty() && (extension.equalsIgnoreCase(Blueprint.BLUEPRINT_FILE_EXTENSION) || extension.equalsIgnoreCase(
      Blueprint.TEMPLATE_FILE_EXTENSION));
  }

  /**
   * Loads a Blueprint resource from the specified URL.
   *
   * @param resourceName The URL of the resource to load.
   * @return The loaded Blueprint resource.
   * @throws Exception if an error occurs while loading the resource.
   */
  @Override
  protected Blueprint load(URL resourceName) throws Exception {
    Blueprint blueprint;
    try {
      blueprint = XmlUtilities.read(Blueprint.class, resourceName);
    } catch (JAXBException e) {
      throw new TmxException("could not parse xml data", e);
    }

    return blueprint;
  }

  /**
   * Gets the alias for the specified resource.
   *
   * @param resourceName The name of the resource.
   * @param resource The resource to get the alias for.
   * @return The alias of the resource, or null if no alias is available.
   */
  @Override
  protected String getAlias(String resourceName, Blueprint resource) {
    if (resource == null || resource.getName() == null || resource.getName().isEmpty() || resource.getName().equalsIgnoreCase(resourceName)) {
      return null;
    }

    return resource.getName();
  }
}
