package de.gurkenlabs.litiengine.resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public final class Images extends ResourcesContainer<BufferedImage> {
  Images() {
  }

  /**
   * Loads all images from the specified texture atlas.
   * 
   * @param textureAtlas
   *          The texture atlas that contains all the images.
   */
  public void load(TextureAtlas textureAtlas) {
    BufferedImage atlasImage = Resources.images().get(textureAtlas.getAbsolutImagePath());
    if (atlasImage == null || atlasImage.getWidth() == 0 || atlasImage.getHeight() == 0) {
      return;
    }

    for (TextureAtlas.Sprite sprite : textureAtlas.getSprites()) {
      BufferedImage image = atlasImage.getSubimage(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
      if (sprite.isRotated()) {
        image = ImageProcessing.rotate(image, Rotation.ROTATE_270);
      }

      Resources.images().add(sprite.getName(), image);
    }
  }

  /**
   * Loads the image by the specified resourceName. This method supports both
   * loading images from a folder and loading them from the resources.
   *
   * @param resourceName
   *          The path to the image.
   * 
   * @return the image
   */
  @Override
  protected BufferedImage load(String resourceName) throws IOException {
    if (resourceName == null || resourceName.isEmpty()) {
      return null;
    }

    // try to get image from resource folder first and as a fallback get it from
    // a normal folder
    BufferedImage img = null;
    final InputStream imageFile = Resources.get(resourceName);
    if (imageFile != null) {
      img = ImageIO.read(imageFile);
    }

    if (img == null) {
      return null;
    }

    final BufferedImage compatibleImg = ImageProcessing.getCompatibleImage(img.getWidth(), img.getHeight());
    compatibleImg.createGraphics().drawImage(img, 0, 0, null);

    return compatibleImg;
  }
}
