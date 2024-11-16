package de.gurkenlabs.litiengine.gui;

/**
 * Enum representing different modes for scaling images.
 */
public enum ImageScaleMode {
  /**
   * No scaling is applied to the image.
   */
  NORMAL,

  /**
   * The image is stretched to fill the available space.
   */
  STRETCH,

  /**
   * The image is scaled to fit within the available space while maintaining its aspect ratio.
   */
  FIT,

  /**
   * The image is scaled and cropped to fill the available space while maintaining its aspect ratio.
   */
  SLICE
}
