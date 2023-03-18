package de.gurkenlabs.litiengine.util;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
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
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

/**
 * The type Imaging.
 */
public final class Imaging {
  public static final int CROP_ALIGN_CENTER = 0;
  public static final int CROP_ALIGN_LEFT = 1;
  public static final int CROP_ALIGN_RIGHT = 2;

  public static final int CROP_VALIGN_BOTTOM = 3;
  public static final int CROP_VALIGN_CENTER = 0;
  public static final int CROP_VALIGN_TOP = 1;
  public static final int CROP_VALIGN_TOPCENTER = 2;

  private static GraphicsConfiguration graphicsConfig;

  private Imaging() {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds a shadow effect by executing the following steps: 1. Transform visible pixels to a semi-transparent black 2.
   * Flip the image vertically 3. Scale it down 4. Render original image and shadow on a buffered image
   *
   * @param image
   *          the image
   * @param xOffset
   *          the x offset
   * @param yOffset
   *          the y offset
   * @return the buffered image
   */
  public static BufferedImage addShadow(
      final BufferedImage image, final int xOffset, final int yOffset) {
    if (image == null) {
      return null;
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
    if (shadow == null) {
      return image;
    }
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
  public static BufferedImage applyAlphaChannel(final BufferedImage img, final Color color) {
    if (color == null || img == null) {
      return img;
    }

    final ImageFilter filter =
        new RGBImageFilter() {

          // the color we are looking for... Alpha bits are set to opaque
          public final int markerRGB = color.getRGB() | 0xFF000000;

          @Override
          public int filterRGB(final int x, final int y, final int rgb) {
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
    return toBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
  }

  /**
   * Draw a stroke around an image in the given color.
   *
   * @param image
   *          the input image
   * @param strokeColor
   *          the stroke color
   * @param borderOnly
   *          decide whether only the border of the given image will be drawn
   * @return the buffered image with a border drawn around it
   */
  public static BufferedImage borderAlpha(
      final BufferedImage image, final Color strokeColor, boolean borderOnly) {
    final BufferedImage bimage =
        getCompatibleImage(image.getWidth(null) + 2, image.getHeight(null) + 2);
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

  /**
   * Checks whether a given BufferedImage only has transparent pixels.
   *
   * @param image
   *          the image
   * @return true if there are no coloured pixels in the image.
   */
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
   * Checks whether two BufferedImages are equal.
   *
   * @param image1
   *          the image 1
   * @param image2
   *          the image 2
   * @return true if the images are equal
   */
  public static boolean areEqual(final BufferedImage image1, final BufferedImage image2) {
    if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
      return false;
    }

    for (int x = 1; x < image2.getWidth(); x++) {
      for (int y = 1; y < image2.getHeight(); y++) {
        if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
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
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_ALIGN_CENTER CROP_ALIGN_CENTER}
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_ALIGN_LEFT CROP_ALIGN_LEFT}
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_ALIGN_RIGHT CROP_ALIGN_RIGHT}
   *          </ul>
   * @param cropVerticlaAlignment
   *          use the following consts: <br>
   *          <ul>
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_VALIGN_CENTER CROP_VALIGN_CENTER}
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_VALIGN_TOP CROP_VALIGN_TOP}
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_VALIGN_TOPCENTER CROP_VALIGN_TOPCENTER}
   *          <li>{@link de.gurkenlabs.litiengine.util.Imaging#CROP_VALIGN_BOTTOM CROP_VALIGN_BOTTOM}
   *          </ul>
   * @param width
   *          The width to crop.
   * @param height
   *          The height to crop.
   * @return The cropped image or the original image if it is smaller than the specified dimensions.
   */
  public static BufferedImage crop(
      final BufferedImage image,
      final int cropAlignment,
      final int cropVerticlaAlignment,
      final int width,
      final int height) {
    if (width > image.getWidth() || height > image.getHeight()) {
      return image;
    }
    int x;
    switch (cropAlignment) {
      case CROP_ALIGN_CENTER -> x = image.getWidth() / 2 - width / 2;
      case CROP_ALIGN_RIGHT -> x = image.getWidth() - width;
      default -> x = 0;
    }
    int y;
    switch (cropVerticlaAlignment) {
      case CROP_VALIGN_CENTER -> y = image.getHeight() / 2 - height / 2;
      case CROP_VALIGN_BOTTOM -> y = image.getHeight() - height;
      case CROP_VALIGN_TOPCENTER -> y = image.getHeight() / 2 - height;
      default -> y = 0;
    }

    return image.getSubimage(x, y, width, height);
  }

  /**
   * All pixels that are not transparent are replaced by a pixel of the specified flashColor.
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

  /**
   * Flip the individual sprites in a {@link Spritesheet} horizontally and return an image from which a new
   * {@link Spritesheet} can be created.
   *
   * @param sprite
   *          the spritesheet
   * @return a {@link BufferedImage} containing the horizontally flipped sprites
   */
  public static BufferedImage flipSpritesHorizontally(final Spritesheet sprite) {
    return flipSprites(sprite, Imaging::horizontalFlip);
  }

  /**
   * Flip the individual sprites in a {@link Spritesheet} vertically and return an image from which a new
   * {@link Spritesheet} can be created.
   *
   * @param sprite
   *          the spritesheet
   * @return a {@link BufferedImage} containing the vertically flipped sprites
   */
  public static BufferedImage flipSpritesVertically(final Spritesheet sprite) {
    return flipSprites(sprite, Imaging::verticalFlip);
  }

  /**
   * Creates a new {@code BufferedImage} instance from the specified image.
   *
   * @param image
   *          The image to be copied.
   * @return A {@link BufferedImage} that is a copy of the input image.
   */
  public static BufferedImage copy(BufferedImage image) {
    ColorModel cm = image.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
  }

  /**
   * Gets an empty {@link BufferedImage} with the given size.
   *
   * @param width
   *          the width
   * @param height
   *          the height
   * @return an empty {@link BufferedImage} with the given size
   */
  public static BufferedImage getCompatibleImage(final int width, final int height) {
    if (width == 0 || height == 0) {
      return null;
    }

    if (graphicsConfig == null) {
      final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice device = env.getDefaultScreenDevice();
      graphicsConfig = device.getDefaultConfiguration();
    }

    return graphicsConfig.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
  }

  /**
   * Gets a two dimensional grid that contains parts of the specified image. Splits up the specified image into a grid
   * with the defined number of rows and columns.
   *
   * @param image
   *          The base image that will be split up.
   * @param rows
   *          The number of rows.
   * @param columns
   *          The number or columns.
   * @return A two dimensional array with all the sub-images.
   */
  public static BufferedImage[][] getSubImages(
      final BufferedImage image, final int rows, final int columns) {
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
   * Flips the specified image horizontally.
   *
   * @param img
   *          The image to be flipped.
   * @return The horizontally flipped image.
   */
  public static BufferedImage horizontalFlip(final BufferedImage img) {
    final int w = img.getWidth();
    final int h = img.getHeight();
    if (w == 0 || h == 0) {
      return img;
    }

    final BufferedImage dimg = getCompatibleImage(w, h);
    final Graphics2D g = Objects.requireNonNull(dimg).createGraphics();
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
    g.dispose();
    return dimg;
  }

  /**
   * Flips the specified image vertically.
   *
   * @param img
   *          The image to be flipped.
   * @return The vertically flipped image.
   */
  public static BufferedImage verticalFlip(final BufferedImage img) {
    final int w = img.getWidth();
    final int h = img.getHeight();
    if (w == 0 || h == 0) {
      return img;
    }

    final BufferedImage dimg = getCompatibleImage(w, h);
    final Graphics2D g = Objects.requireNonNull(dimg).createGraphics();
    g.drawImage(img, 0, h, w, -h, null);
    g.dispose();
    return dimg;
  }

  /**
   * Replace colors in an image according to a Map containing source colors and target colors, then return the result.
   *
   * @param bufferedImage
   *          the original image
   * @param colorMappings
   *          a Map with source colors as keys and target colors as values
   * @return a new version of the original image, where the source colors are replaced with the target colors.
   */
  public static BufferedImage replaceColors(
      final BufferedImage bufferedImage, Map<Color, Color> colorMappings) {
    BufferedImage recoloredImage = copy(bufferedImage);
    for (Entry<Color, Color> c : colorMappings.entrySet()) {
      for (int y = 0; y < recoloredImage.getHeight(); y++) {
        for (int x = 0; x < recoloredImage.getWidth(); x++) {
          if (recoloredImage.getRGB(x, y) == c.getKey().getRGB()) {
            int newColor = c.getValue().getRGB();
            recoloredImage.setRGB(x, y, newColor);
          }
        }
      }
    }
    return recoloredImage;
  }

  /**
   * Rotate a given {@link BufferedImage} by a given {@link Rotation}.
   *
   * @param bufferedImage
   *          the input image
   * @param rotation
   *          the amount of {@link Rotation}
   * @return the rotated {@link BufferedImage}
   */
  public static BufferedImage rotate(final BufferedImage bufferedImage, final Rotation rotation) {
    return rotate(bufferedImage, rotation.getRadians());
  }

  /**
   * Rotate a {@link BufferedImage} by a given rotation.
   *
   * @param bufferedImage
   *          the input image
   * @param radians
   *          the amount of rotation in radians.
   * @return the rotated {@link BufferedImage}
   */
  public static BufferedImage rotate(final BufferedImage bufferedImage, final double radians) {
    double sin = Math.abs(Math.sin(radians));
    double cos = Math.abs(Math.cos(radians));

    int w = bufferedImage.getWidth();
    int h = bufferedImage.getHeight();

    int newWidth = (int) Math.floor(w * cos + h * sin);
    int newHeight = (int) Math.floor(h * cos + w * sin);

    BufferedImage bimg = getCompatibleImage(newWidth, newHeight);
    if (bimg == null) {
      return bufferedImage;
    }

    Graphics2D g = bimg.createGraphics();

    g.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);
    g.rotate(radians, w / 2.0, h / 2.0);
    g.drawRenderedImage(toBufferedImage(bufferedImage), null);
    g.dispose();

    return bimg;
  }

  /**
   * Scale buffered image so that the longer edge of the image will be set to a given maximum.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param max
   *          the maximum length of the longer image edge
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(final BufferedImage image, final int max) {
    Dimension2D newDimension = GeometricUtilities.scaleWithRatio(image.getWidth(), image.getHeight(), max);
    return scale(image, (int) Objects.requireNonNull(newDimension).getWidth(), (int) Objects.requireNonNull(newDimension).getHeight());
  }

  /**
   * Scale buffered image by multiplying its width and height with a given factor. By default,
   * {@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR} is used for scaling. If you want smooth interpolation, use one of the
   * method overloads with a custom interpolation parameter.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param factor
   *          the factor by which the image width and height will be multiplied
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(final BufferedImage image, final double factor) {
    return scale(image, factor, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
  }

  /**
   * Scale buffered image by multiplying its width and height with a given factor. This default overload will not generate
   * white space around the scaled image to match the full target dimensions.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param factor
   *          the factor by which the image width and height will be multiplied
   * @param interpolation
   *          the interpolation mode used for scaling. Choose one of the following:
   *          <ul>
   *          <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR}
   *          <li>{@link AffineTransformOp#TYPE_BILINEAR}
   *          <li>{@link AffineTransformOp#TYPE_BICUBIC}
   *          </ul>
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(final BufferedImage image, final double factor, final int interpolation) {
    return image == null
        ? null
        : scale(image, (int) Math.ceil(image.getWidth() * factor), (int) Math.ceil(image.getHeight() * factor), interpolation);
  }

  /**
   * Scale buffered image. The original image ratio will be kept. By default,
   * {@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR} is used for scaling. If you want smooth interpolation, use one of the
   * method overloads with a custom interpolation parameter. This default overload will not generate white space around
   * the scaled image to match the full target dimensions.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(final BufferedImage image, final int width, final int height) {
    return scale(image, width, height, AffineTransformOp.TYPE_NEAREST_NEIGHBOR, false, false);
  }


  /**
   * Scale buffered image. By default, {@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR} is used for scaling. If you want
   * smooth interpolation, use one of the method overloads with a custom interpolation parameter. This default overload
   * will not generate white space around the scaled image to match the full target dimensions.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @param keepRatio
   *          determines whether the original image ratio should be kept while scaling.
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(
      final BufferedImage image, final int width, final int height, final boolean keepRatio) {
    return scale(image, width, height, AffineTransformOp.TYPE_NEAREST_NEIGHBOR, keepRatio, false);
  }

  /**
   * Scale buffered image. By default, {@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR} is used for scaling. If you want
   * smooth interpolation, use one of the method overloads with a custom interpolation parameter.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @param keepRatio
   *          determines whether the original image ratio should be kept while scaling.
   * @param fill
   *          determines whether the target image should contain the transparent space around the scaled image to fill the
   *          full target dimensions.
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(
      final BufferedImage image, final int width, final int height, final boolean keepRatio, final boolean fill) {
    return scale(image, width, height, AffineTransformOp.TYPE_NEAREST_NEIGHBOR, keepRatio, fill);
  }

  /**
   * Scale buffered image. This default overload will not generate white space around the scaled image to match the full
   * target dimensions.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @param interpolation
   *          the interpolation mode used for scaling. Choose one of the following:
   *          <ul>
   *          <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR}
   *          <li>{@link AffineTransformOp#TYPE_BILINEAR}
   *          <li>{@link AffineTransformOp#TYPE_BICUBIC}
   *          </ul>
   * @param keepRatio
   *          determines whether the original image ratio should be kept while scaling.
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(
      final BufferedImage image,
      final int width,
      final int height,
      final int interpolation,
      final boolean keepRatio) {
    return scale(image, width, height, interpolation, keepRatio, false);
  }

  /**
   * Scale buffered image. This default overload will keep the original image ratio and not generate white space around
   * the scaled image to match the full target dimensions.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @param interpolation
   *          the interpolation mode used for scaling. Choose one of the following:
   *          <ul>
   *          <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR}
   *          <li>{@link AffineTransformOp#TYPE_BILINEAR}
   *          <li>{@link AffineTransformOp#TYPE_BICUBIC}
   *          </ul>
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(
      final BufferedImage image,
      final int width,
      final int height,
      final int interpolation) {
    return scale(image, width, height, interpolation, true, false);
  }

  /**
   * Scale buffered image.
   *
   * @param image
   *          the input {@link BufferedImage} to be scaled
   * @param width
   *          the width of the scaled image in pixels
   * @param height
   *          the height of the scaled image in pixels
   * @param interpolation
   *          the interpolation mode used for scaling. Choose one of the following:
   *          <ul>
   *          <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR}
   *          <li>{@link AffineTransformOp#TYPE_BILINEAR}
   *          <li>{@link AffineTransformOp#TYPE_BICUBIC}
   *          </ul>
   * @param keepRatio
   *          determines whether the original image ratio should be kept while scaling.
   * @param fill
   *          determines whether the target image should contain the transparent space around the scaled image to fill the
   *          full target dimensions.
   * @return the scaled {@link BufferedImage}.
   */
  public static BufferedImage scale(
      final BufferedImage image,
      final int width,
      final int height,
      final int interpolation,
      final boolean keepRatio,
      final boolean fill) {
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
    final AffineTransformOp bilinearScaleOp =
        new AffineTransformOp(scaleTransform, interpolation);
    final BufferedImage scaled =
        bilinearScaleOp.filter(image, getCompatibleImage((int) newWidth, (int) newHeight));
    final BufferedImage newImg = getCompatibleImage((int) newWidth, (int) newHeight);
    if (newImg == null) {
      Game.log().log(Level.WARNING, "Image could not be scaled to {0} * {1}: {2}", new Object[] {newWidth, newHeight, image.toString()});
      return null;
    }

    final Graphics2D g = (Graphics2D) newImg.getGraphics();
    g.drawImage(scaled, 0, 0, null);
    g.dispose();

    if (fill && (newWidth != width || newHeight != height)) {
      final BufferedImage wrapperImage = getCompatibleImage(width, height);
      final Graphics2D g2 = (Graphics2D) Objects.requireNonNull(wrapperImage).getGraphics();
      g2.drawImage(
          newImg, (int) ((width - newWidth) / 2.0), (int) ((height - newHeight) / 2.0), null);
      g2.dispose();
      return wrapperImage;
    }

    return newImg;
  }

  /**
   * Sets the opacity of a given image.
   *
   * @param img
   *          the original {@link Image}
   * @param alpha
   *          the constant alpha to be multiplied with the alpha of the source. alpha must be a floating point number in
   *          the inclusive range [0.0, 1.0].
   * @return the {@link BufferedImage} produced by applying the given alpha on the source image
   */
  public static BufferedImage setAlpha(final Image img, final float alpha) {
    if (img == null) {
      return null;
    }
    final BufferedImage bimage = getCompatibleImage(img.getWidth(null), img.getHeight(null));
    if (bimage == null) {
      return null;
    }

    // Draw the image on to the buffered image
    final Graphics2D g2d = bimage.createGraphics();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();

    return bimage;
  }

  /**
   * Converts a given {@link Image} instance to a {@link BufferedImage}.
   *
   * @param img
   *          the original {@link Image}
   * @return the {@link BufferedImage} produced from the original {@link Image}
   */
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

  /**
   * Creates a compatible buffered image that contains the source image.
   *
   * @param image
   *          the source image
   * @return the compatible buffered image
   */
  public static BufferedImage toCompatibleImage(final BufferedImage image) {
    if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
      return image;
    }

    final BufferedImage compatibleImg = getCompatibleImage(image.getWidth(), image.getHeight());
    if (compatibleImg == null) {
      return null;
    }
    compatibleImg.createGraphics().drawImage(image, 0, 0, null);

    return compatibleImg;
  }

  private static BufferedImage flipSprites(
      final Spritesheet sprite, UnaryOperator<BufferedImage> flipFunction) {
    final BufferedImage flippedSprite =
        Imaging.getCompatibleImage(
            sprite.getSpriteWidth() * sprite.getColumns(),
            sprite.getSpriteHeight() * sprite.getRows());
    if (flippedSprite == null) {
      return null;
    }

    final Graphics2D g = (Graphics2D) flippedSprite.getGraphics();
    int index = 0;
    for (int column = 0; column < sprite.getColumns(); column++) {
      for (int row = 0; row < sprite.getRows(); row++) {
        g.drawImage(
            flipFunction.apply(sprite.getSprite(index)),
            column * sprite.getSpriteWidth(),
            row * sprite.getSpriteHeight(),
            null);
        index++;
      }
    }

    g.dispose();
    return flippedSprite;
  }
}
