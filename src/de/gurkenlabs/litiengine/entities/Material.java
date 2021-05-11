package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomPropertyProvider;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Material extends CustomPropertyProvider {
  private static final Map<String, Material> materials = new ConcurrentHashMap<>();
  public static final Material UNDEFINED = new Material("UNDEFINED");
  public static final Material CERAMIC = new Material("CERAMIC");
  public static final Material FLESH = new Material("FLESH");
  public static final Material FOLIAGE = new Material("FOLIAGE");
  public static final Material PLASTIC = new Material("PLASTIC");
  public static final Material STEEL = new Material("STEEL");
  public static final Material STONE = new Material("STONE");
  public static final Material WOOD = new Material("WOOD");

  private final String name;

  /**
   * Initializes a new instance of the {@code Material} class.
   *
   * @param name The name of the material.
   */
  public Material(String name) {
    this.name = name;
    materials.put(name.toLowerCase(), this);
  }

  public static Material get(String name) {
    if (name == null || name.isEmpty()) {
      return UNDEFINED;
    }

    return materials.getOrDefault(name.toLowerCase(), UNDEFINED);
  }

  public static Collection<Material> getMaterials() {
    return materials.values();
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
