package de.gurkenlabs.litiengine.resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Images extends ResourcesContainer<BufferedImage> {
  private static final Logger log = Logger.getLogger(Images.class.getName());

  Images() {
  }

  /**
   * Loads the image by the specified resourceName. This method supports both,
   * loading images from a folder and loading them from the resources.
   *
   * @param resourceName
   *          The path to the image.
   * 
   * @return the image
   */
  @Override
  protected BufferedImage load(String resourceName) {
    if (resourceName == null || resourceName.isEmpty()) {
      return null;
    }

    if (!ImageFormat.isSupported(resourceName)) {
      log.log(Level.SEVERE, "The image file {0} could not be loaded because the image format is not supported.", new Object[] { resourceName });
      return null;
    }

    // try to get image from resource folder first and as a fallback get it from
    // a normal folder
    BufferedImage img = null;
    final InputStream imageFile = FileUtilities.getGameResource(resourceName);
    if (imageFile != null) {
      try {
        img = ImageIO.read(imageFile);
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        return null;
      }
    }

    if (img == null) {
      return null;
    }

    final BufferedImage compatibleImg = ImageProcessing.getCompatibleImage(img.getWidth(), img.getHeight());
    compatibleImg.createGraphics().drawImage(img, 0, 0, null);

    return compatibleImg;
  }
}
