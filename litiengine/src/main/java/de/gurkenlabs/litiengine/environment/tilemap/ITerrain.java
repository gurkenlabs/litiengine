package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.*;

/**
 * Interface representing a terrain with specific characteristics. Implementations of this interface define methods
 * to retrieve the name, color, and probability associated with the terrain, which may be used
 * for automatic mapping purposes.
 */
public interface ITerrain {

  /**
   * Gets the name of the terrain.
   *
   * @return A {@code String} representing the name of the terrain.
   */
  String getName();

  /**
   * Gets the color associated with the terrain.
   *
   * @return A {@link Color} object representing the color of the terrain.
   */
  Color getColor();

  /**
   * Gets the probability value associated with the terrain.
   * <p>
   * The probability value is used in automatic mapping processes to indicate the likelihood or preference of using
   * this terrain in specific contexts. Higher probability values may suggest a higher preference for automatic mapping.
   *
   * @return A {@code double} representing the probability value of the terrain.
   */
  double getProbability();
}
