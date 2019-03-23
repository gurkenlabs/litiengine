package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.image.BufferedImage;

public interface ITilesetEntry extends ICustomPropertyProvider {

  public int getId();

  public ITerrain[] getTerrain();

  public ITileAnimation getAnimation();

  /**
   * Gets the current image for this tileset entry.
   * @return The current image for this tileset entry, accounting for animation.
   */
  public BufferedImage getImage();

  /**
   * Gets the "standard" image for this tileset entry, without applying any animations.
   * @return The standard image for this tileset entry
   */
  public BufferedImage getBasicImage();

  /**
   * Gets the tileset that this entry belongs to.
   * @return The tileset for this entry
   */
  public ITileset getTileset();

  public String getType();
}
