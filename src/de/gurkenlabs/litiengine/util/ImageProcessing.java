package de.gurkenlabs.litiengine.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public class ImageProcessing {
  private static final Logger log = Logger.getLogger(ImageProcessing.class.getName());
  public static final int CROP_ALIGN_CENTER = 0;
  public static final int CROP_ALIGN_LEFT = 1;
  public static final int CROP_ALIGN_RIGHT = 2;

  public static final int CROP_VALIGN_BOTTOM = 3;
  public static final int CROP_VALIGN_CENTER = 0;
  public static final int CROP_VALIGN_TOP = 1;
  public static final int CROP_VALIGN_TOPCENTER = 2;

  private static GraphicsConfiguration graphicsConfig;

  private ImageProcessing() {
  }

  /**
   * Adds a shadow effect by executing the following steps: 1. Transform visible
   * pixels to a semi-transparent black 2. Flip the image vertically 3. Scale it
   * down 4. Render original image and shadow on a buffered image
   *
   * @param image
   *          the image
   * @param xOffset
   *          the x offset
   * @param yOffset
   *          the y offset
   * @return the buffered image
   */
  public static BufferedImage addShadow(final BufferedImage image, final int xOffset, final int yOffset) {
    if (image == null) {
      return image;
    }

    final int width = image.getWidth();
    final int height = image.getHeight();
    if (width == 0 || height == 0) {
      return image;
    }

    // Transform visible pixels to a semi-transparent black
    final BufferedImage shadowImage = flashVisiblePixels(image, new Color(0, 0, 0, 30));
    if (shadowImage == null) {
      return image;
    }

    final AffineTransform tx = new AffineTransform();

    // Flip the image vertically
    tx.concatenate(AffineTransform.getScaleInstance(1, -0.15));
    tx.concatenate(AffineTransform.getTranslateInstance(0, -shadowImage.getHeight()));
    final AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    final BufferedImage rotatedImage = op.filter(shadowImage, null);

    final BufferedImage shadow = getCompatibleImage(width, height + rotatedImage.getHeight() * 2);
    final Graphics2D g2D = shadow.createGraphics();
    g2D.drawImage(rotatedImage, xOffset, yOffset + rotatedImage.getHeight(), null);
    g2D.drawImage(image, 0, rotatedImage.getHeight(), null);

    g2D.dispose();

    return shadow;
  }

  /**
   * All pixels that have the specified color are rendered transparent.
   *
   * @param img
   *          the img
   * @param color
   *          the color
   * @return the image
   */
  public static Image applyAlphaChannel(final Image img, final Color color) {
    if (color == null || img == null) {
      return img;
    }

    final ImageFilter filter = new RGBImageFilter() {

      // the color we are looking for... Alpha bits are set to opaque
      public final int markerRGB = color.getRGB() | 0xFF000000;

      @Override
      public final int filterRGB(final int x, final int y, final int rgb) {
        if ((rgb | 0xFF000000) == this.markerRGB) {
          // Mark the alpha bits as zero - transparent
          return 0x00FFFFFF & rgb;
        } else {
          // nothing to do
          return rgb;
        }
      }
    };

    final ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(ip);
  }

  public static BufferedImage borderAlpha(final BufferedImage image, final Color strokeColor, boolean borderOnly) {
    final BufferedImage bimage = getCompatibleImage(image.getWidth(null) + 2, image.getHeight(null) + 2);
    if (bimage == null) {
      return image;
    }

    final BufferedImage strokeImg = flashVisiblePixels(image, strokeColor);
    // Draw the image on to the buffered image
    final Graphics2D graphics = bimage.createGraphics();
    graphics.drawImage(strokeImg, 0, 1, null);
    graphics.drawImage(strokeImg, 2, 1, null);
    graphics.drawImage(strokeImg, 1, 0, null);
    graphics.drawImage(strokeImg, 1, 2, null);

    Composite old = graphics.getComposite();
    graphics.setComposite(AlphaComposite.Clear);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        final int pixel = image.getRGB(x, y);
        if (pixel >> 24 != 0x00) {
          graphics.fillRect(x + 1, y + 1, 1, 1);
        }
      }
    }

    if (!borderOnly) {
      graphics.setComposite(old);
      graphics.drawImage(image, 1, 1, null);
    }

    graphics.dispose();

    return bimage;
  }

  public static boolean isEmpty(final BufferedImage image) {
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        final int pixel = image.getRGB(x, y);
        if (pixel >> 24 != 0x00) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Crops a sub image from the specified image.
   *
   * @param image
   *          The image to crop the sub-image from.
   * @param cropAlignment
   *          use the following consts: <br>
   *          <ul>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_ALIGN_CENTER
   *          CROP_ALIGN_CENTER}</li>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_ALIGN_LEFT
   *          CROP_ALIGN_LEFT}</li>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_ALIGN_RIGHT
   *          CROP_ALIGN_RIGHT}</li>
   *          </ul>
   * @param cropVerticlaAlignment
   *          use the following consts: <br>
   *          <ul>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_VALIGN_CENTER
   *          CROP_VALIGN_CENTER}</li>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_VALIGN_TOP
   *          CROP_VALIGN_TOP}</li>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_VALIGN_TOPCENTER
   *          CROP_VALIGN_TOPCENTER}</li>
   *          <li>{@link de.gurkenlabs.litiengine.util.ImageProcessing#CROP_VALIGN_BOTTOM
   *          CROP_VALIGN_BOTTOM}</li>
   *          </ul>
   * @param width
   *          The width to crop.
   * @param height
   *          The height to crop.
   * @return The cropped image or the original image if it is smaller than the
   *         specified dimensions.
   */
  public static BufferedImage crop(final BufferedImage image, final int cropAlignment, final int cropVerticlaAlignment, final int width, final int height) {
    if (width > image.getWidth() || height > image.getHeight()) {
      return image;
    }

    int x;
    switch (cropAlignment) {
    case CROP_ALIGN_CENTER:
      x = image.getWidth() / 2 - width / 2;
      break;
    case CROP_ALIGN_RIGHT:
      x = image.getWidth() - width;
      break;
    case CROP_ALIGN_LEFT:
    default:
      x = 0;
      break;
    }

    int y;
    switch (cropVerticlaAlignment) {
    case CROP_VALIGN_CENTER:
      y = image.getHeight() / 2 - height / 2;
      break;
    case CROP_VALIGN_BOTTOM:
      y = image.getHeight() - height;
      break;
    case CROP_VALIGN_TOPCENTER:
      y = image.getHeight() / 2 - height;
      break;
    case CROP_VALIGN_TOP:
    default:
      y = 0;
      break;
    }

    return image.getSubimage(x, y, width, height);
  }

  public static BufferedImage decodeToImage(final String imageString) {
    if (imageString == null) {
      return null;
    }

    BufferedImage image = null;
    byte[] imageByte;
    try {
      imageByte = Base64.getDecoder().decode(imageString);
      final ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
      image = ImageIO.read(bis);
      bis.close();
    } catch (final Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return image;
  }

  public static String encodeToString(final BufferedImage image) {
    return encodeToString(image, ImageFormat.PNG);
  }

  public static String encodeToString(final BufferedImage image, ImageFormat imageFormat) {
    if (image == null) {
      return null;
    }

    String imageString = null;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      ImageIO.write(image, imageFormat != ImageFormat.UNDEFINED ? imageFormat.toString() : ImageFormat.PNG.toString(), bos);
      final byte[] imageBytes = bos.toByteArray();

      imageString = Base64.getEncoder().encodeToString(imageBytes);

      bos.close();
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return imageString;
  }

  /**
   * All pixels that are not transparent are replaced by a pixel of the
   * specified flashColor.
   *
   * @param image
   *          the image
   * @param flashColor
   *          the flash color
   * @return the buffered image
   */
  public static BufferedImage flashVisiblePixels(final Image image, final Color flashColor) {
    final BufferedImage bimage = getCompatibleImage(image.getWidth(null), image.getHeight(null));
    if (bimage == null) {
      return null;
    }

    // Draw the image on to the buffered image
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(image, 0, 0, null);
    bGr.dispose();

    for (int y = 0; y < bimage.getHeight(); y++) {
      for (int x = 0; x < bimage.getWidth(); x++) {
        final int pixel = bimage.getRGB(x, y);
        if (pixel >> 24 != 0x00) {
          bimage.setRGB(x, y, flashColor.getRGB());
        }
      }
    }

    return bimage;
  }

  public static BufferedImage flipSpritesHorizontally(final Spritesheet sprite) {
    final BufferedImage flippedSprite = ImageProcessing.getCompatibleImage(sprite.getSpriteWidth() * sprite.getTotalNumberOfSprites(), sprite.getSpriteHeight());
    if (flippedSprite == null) {
      return null;
    }

    final Graphics2D g = (Graphics2D) flippedSprite.getGraphics();
    for (int i = 0; i < sprite.getTotalNumberOfSprites(); i++) {
      g.drawImage(ImageProcessing.horizontalFlip(sprite.getSprite(i)), i * sprite.getSpriteWidth(), 0, null);
    }
    g.dispose();

    return flippedSprite;
  }

  public static BufferedImage flipSpritesVertically(final Spritesheet sprite) {
    final BufferedImage flippedSprite = ImageProcessing.getCompatibleImage(sprite.getSpriteWidth() * sprite.getTotalNumberOfSprites(), sprite.getSpriteHeight());
    if (flippedSprite == null) {
      return null;
    }

    final Graphics2D g = (Graphics2D) flippedSprite.getGraphics();
    for (int i = 0; i < sprite.getTotalNumberOfSprites(); i++) {
      g.drawImage(ImageProcessing.verticalFlip(sprite.getSprite(i)), i * sprite.getSpriteWidth(), 0, null);
    }
    g.dispose();

    return flippedSprite;
  }
  
  public static BufferedImage getCompatibleImage(final int width, final int height) {
    if (width == 0 && height == 0) {
      return null;
    }

    if (graphicsConfig == null) {
      final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice device = env.getDefaultScreenDevice();
      graphicsConfig = device.getDefaultConfiguration();
    }

    return graphicsConfig.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
  }

  public static BufferedImage[][] getSubImages(final BufferedImage image, final int rows, final int columns) {
    final BufferedImage[][] smallImages = new BufferedImage[rows][columns];
    final int smallWidth = image.getWidth() / columns;
    final int smallHeight = image.getHeight() / rows;

    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < columns; x++) {
        final int cellX = x * smallWidth;
        final int cellY = y * smallHeight;
        smallImages[y][x] = image.getSubimage(cellX, cellY, smallWidth, smallHeight);
      }
    }

    return smallImages;
  }

  /**
   * Horizontalflip.
   *
   * @param img
   *          the img
   * @return the buffered image
   */
  public static BufferedImage horizontalFlip(final BufferedImage img) {
    final int w = img.getWidth();
    final int h = img.getHeight();
    if (w == 0 || h == 0) {
      return img;
    }

    final BufferedImage dimg = getCompatibleImage(w, h);
    final Graphics2D g = dimg.createGraphics();
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
    g.dispose();
    return dimg;
  }

  /**
   * Needs border.
   *
   * @param image
   *          the image
   * @param x
   *          the x
   * @param y
   *          the y
   * @return true, if successful
   */
  public static boolean needsBorder(final BufferedImage image, final int x, final int y) {
    if (y < 0 || y >= image.getHeight()) {
      return false;
    }

    if (x < 0 || x >= image.getWidth()) {
      return false;
    }

    // if the current pixel is not transparent, we cannot stroke it
    if (image.getRGB(x, y) >> 24 != 0x00) {
      return false;
    }

    // check pixel above the current one
    if (y > 0 && image.getRGB(x, y - 1) >> 24 != 0x00) {
      return true;
    }

    // check below pixel
    if (y < image.getHeight() - 1 && image.getRGB(x, y + 1) >> 24 != 0x00) {
      return true;
    }

    // check left pixel
    if (x > 0 && image.getRGB(x - 1, y) >> 24 != 0x00) {
      return true;
    }

    // check right pixel
    return x < image.getWidth() - 1 && image.getRGB(x + 1, y) >> 24 != 0x00;
  }

  public static BufferedImage rotate(final BufferedImage bufferedImage, final Rotation rotation) {
    return rotate(bufferedImage, rotation.getRadians());
  }

  public static BufferedImage rotate(final BufferedImage bufferedImage, final double radians) {

    final AffineTransform tx = new AffineTransform();
    tx.rotate(radians, bufferedImage.getWidth() / 2.0, bufferedImage.getHeight() / 2.0);

    final AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(bufferedImage, null);
  }

  public static BufferedImage scaleImage(final BufferedImage image, final int max) {
    Dimension2D newDimension = GeometricUtilities.scaleWithRatio(image.getWidth(), image.getHeight(), max);
    return scaleImage(image, (int) newDimension.getWidth(), (int) newDimension.getHeight());
  }

  public static BufferedImage scaleImage(final BufferedImage image, final float factor) {
    final double width = image.getWidth();
    final double height = image.getHeight();

    return scaleImage(image, (int) (width * factor), (int) (height * factor));
  }

  /**
   * The specified image is scaled to a new dimension with the specified width
   * and height. This method doesn't use anti aliasing for this process to keep
   * the indy look.
   *
   * @param image
   *          the image
   * @param width
   *          the width
   * @param height
   *          the height
   * @return the buffered image
   */
  public static BufferedImage scaleImage(final BufferedImage image, final int width, final int height) {
    return scaleImage(image, width, height, false);
  }

  public static BufferedImage scaleImage(final BufferedImage image, final int width, final int height, final boolean keepRatio) {
    return scaleImage(image, width, height, keepRatio, true);
  }

  public static BufferedImage scaleImage(final BufferedImage image, final int width, final int height, final boolean keepRatio, final boolean fill) {
    if (width == 0 || height == 0 || image == null) {
      return null;
    }

    final int imageWidth = image.getWidth();
    final int imageHeight = image.getHeight();
    double newWidth = width;
    double newHeight = height;
    if (keepRatio) {
      final double ratioWidth = image.getWidth() / (double) image.getHeight();
      final double ratioHeight = image.getHeight() / (double) image.getWidth();

      newHeight = newWidth * ratioHeight;
      if (newHeight > height) {
        newHeight = height;
        newWidth = newHeight * ratioWidth;
      }
    }

    final double scaleX = newWidth / imageWidth;
    final double scaleY = newHeight / imageHeight;
    final AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
    final AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    final BufferedImage scaled = bilinearScaleOp.filter(image, getCompatibleImage((int) newWidth, (int) newHeight));
    final BufferedImage newImg = getCompatibleImage((int) newWidth, (int) newHeight);
    if (newImg == null) {
      return image;
    }

    final Graphics2D g = (Graphics2D) newImg.getGraphics();
    g.drawImage(scaled, 0, 0, null);
    g.dispose();

    if (fill && (newWidth != width || newHeight != height)) {
      final BufferedImage wrapperImage = getCompatibleImage(width, height);
      final Graphics2D g2 = (Graphics2D) wrapperImage.getGraphics();
      g2.drawImage(newImg, (int) ((width - newWidth) / 2.0), (int) ((height - newHeight) / 2.0), null);
      g2.dispose();
      return wrapperImage;
    }

    return newImg;
  }

  public static BufferedImage scaleImageWidth(final BufferedImage image, final int newWidth) {
    final double width = image.getWidth();
    final double height = image.getHeight();
    if (width == 0 || height == 0) {
      return null;
    }

    final double ratio = newWidth / width;
    final double newHeight = height * ratio;

    return scaleImage(image, newWidth, (int) newHeight);
  }

  public static BufferedImage setOpacity(final Image img, final float opacity) {
    final BufferedImage bimage = getCompatibleImage(img.getWidth(null), img.getHeight(null));
    if (bimage == null) {
      return null;
    }

    // Draw the image on to the buffered image
    final Graphics2D g2d = bimage.createGraphics();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();

    return bimage;
  }

  public static BufferedImage toBufferedImage(final Image img) {
    if (img == null) {
      return null;
    }

    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }

    final BufferedImage bimage = getCompatibleImage(img.getWidth(null), img.getHeight(null));
    if (bimage == null) {
      return null;
    }

    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    return bimage;
  }

  public static BufferedImage verticalFlip(final BufferedImage img) {
    final int w = img.getWidth();
    final int h = img.getHeight();
    if (w == 0 || h == 0) {
      return img;
    }

    final BufferedImage dimg = getCompatibleImage(w, h);
    final Graphics2D g = dimg.createGraphics();
    g.drawImage(img, 0, 0 + h, w, -h, null);
    g.dispose();
    return dimg;
  }

  public static BufferedImage zoom(final BufferedImage image, final float zoomLevel) {
    final int newImageWidth = (int) (image.getWidth() * zoomLevel);
    final int newImageHeight = (int) (image.getHeight() * zoomLevel);
    final BufferedImage resizedImage = getCompatibleImage(newImageWidth, newImageHeight);
    if (resizedImage == null) {
      return image;
    }

    final Graphics2D g = resizedImage.createGraphics();
    g.drawImage(image, 0, 0, newImageWidth, newImageHeight, null);
    g.dispose();

    return resizedImage;
  }

  public static BufferedImage convertToGrayScale(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
    Graphics2D g = result.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return result;
  }
}