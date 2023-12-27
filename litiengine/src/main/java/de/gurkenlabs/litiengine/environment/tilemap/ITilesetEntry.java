package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.image.BufferedImage;

public interface ITilesetEntry extends ICustomPropertyProvider {

  int getId();

  ITileAnimation getAnimation();

  /**
   * Gets the current image for this tileset entry.
   *
   * @return The current image for this tileset entry, accounting for animation.
   */
  BufferedImage getImage();

  /**
   * Gets the "standard" image for this tileset entry, without applying any animations.
   *
   * @return The standard image for this tileset entry
   */
  BufferedImage getBasicImage();

  /**
   * Gets the tileset that this entry belongs to.
   *
   * @return The tileset for this entry
   */
  ITileset getTileset();

  String getType();

  IMapObjectLayer getCollisionInfo();
}
