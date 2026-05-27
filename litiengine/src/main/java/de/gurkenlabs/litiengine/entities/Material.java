package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomPropertyProvider;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes the physical material of an entity (e.g. {@link #WOOD}, {@link #STONE}). Materials are looked up by name and are typically used by the
 * engine to drive damage and sound interactions. Custom materials can be created by instantiating this class.
 */
public class Material extends CustomPropertyProvider {
  private static final Map<String, Material> materials = new ConcurrentHashMap<>();
  /**
   * Default fall-back material used when no specific material is assigned.
   */
  public static final Material UNDEFINED = new Material("UNDEFINED");
  /**
   * Ceramic material.
   */
  public static final Material CERAMIC = new Material("CERAMIC");
  /** Flesh material. */
  public static final Material FLESH = new Material("FLESH");
  /** Foliage material. */
  public static final Material FOLIAGE = new Material("FOLIAGE");
  /** Plastic material. */
  public static final Material PLASTIC = new Material("PLASTIC");
  /** Steel material. */
  public static final Material STEEL = new Material("STEEL");
  /** Stone material. */
  public static final Material STONE = new Material("STONE");
  /** Wood material. */
  public static final Material WOOD = new Material("WOOD");

  private final String name;

  /**
   * Initializes a new instance of the {@code Material} class.
   *
   * @param name
   *          The name of the material.
   */
  public Material(String name) {
    this.name = name;
    materials.put(name.toLowerCase(), this);
  }

  /**
   * Returns the material registered under the supplied name (case-insensitive), or {@link #UNDEFINED} if no such material exists.
   *
   * @param name the material name
   * @return the matching material, or {@link #UNDEFINED}
   */
  public static Material get(String name) {
    if (name == null || name.isEmpty()) {
      return UNDEFINED;
    }

    return materials.getOrDefault(name.toLowerCase(), UNDEFINED);
  }

  /**
   * Returns all registered materials, including any custom ones created at runtime.
   *
   * @return the registered materials
   */
  public static Collection<Material> getMaterials() {
    return materials.values();
  }

  /**
   * Returns the display name of this material.
   *
   * @return the material name
   */
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
