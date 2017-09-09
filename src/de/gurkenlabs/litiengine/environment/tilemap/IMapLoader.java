package de.gurkenlabs.litiengine.environment.tilemap;

/**
 * The Interface IMapLoader.
 */
public interface IMapLoader {

  /**
   * Load map.
   *
   * @param path
   *          the path
   * @return the i map
   */
  IMap LoadMap(String path);
}
