package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;

/**
 * The ImageComponent class extends the GuiComponent class to provide functionality for handling images and spritesheets within a GUI component. It
 * supports various image scaling modes, alignment, and a caching mechanism to retain the scaled images in memory.
 */
public class ImageComponent extends GuiComponent {

  public static final int BACKGROUND_INDEX = 0;
  public static final int BACKGROUND_HOVER_INDEX = 1;
  public static final int BACKGROUND_PRESSED_INDEX = 2;
  public static final int BACKGROUND_DISABLED_INDEX = 3;

  private BufferedImage baseImage;
  private BufferedImage scaledImage;

  private Spritesheet spritesheet;

  private ImageScaleMode imageScaleMode = ImageScaleMode.NORMAL;
  private ImageScaleMode spritesheetScaleMode = ImageScaleMode.NORMAL;
  private int imageScaleInterpolation = AffineTransformOp.TYPE_BICUBIC;
  private int spritesheetScaleInterpolation = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
  private float spritesheetScaleFactor = 1f;
  private Align imageAlign = Align.CENTER;
  private Valign imageValign = Valign.MIDDLE;

  /**
   * Constructs an ImageComponent with the specified position and image.
   *
   * @param x     The x-coordinate of the component.
   * @param y     The y-coordinate of the component.
   * @param image The image to be displayed by the component.
   */
  public ImageComponent(final double x, final double y, final Image image) {
    super(x, y, image.getWidth(null), image.getHeight(null));
    this.baseImage = (BufferedImage) image;
  }

  /**
   * Constructs an ImageComponent with the specified position and dimensions.
   *
   * @param x      The x-coordinate of the component.
   * @param y      The y-coordinate of the component.
   * @param width  The width of the component.
   * @param height The height of the component.
   */
  public ImageComponent(final double x, final double y, final double width, final double height) {
    super(x, y, width, height);
  }

  /**
   * Constructs an ImageComponent with the specified position, dimensions, and text.
   *
   * @param x      The x-coordinate of the component.
   * @param y      The y-coordinate of the component.
   * @param width  The width of the component.
   * @param height The height of the component.
   * @param text   The text to be displayed by the component.
   */
  public ImageComponent(final double x, final double y, final double width, final double height, final String text) {
    super(x, y, width, height);
    Font defFont = new JLabel().getFont().deriveFont((float) (this.getHeight() * 3 / 6f));
    if (this.getFont() == null) {
      this.setFont(defFont);
    }
    this.setText(text);
  }

  /**
   * Constructs an ImageComponent with the specified position, dimensions, and image.
   *
   * @param x      The x-coordinate of the component.
   * @param y      The y-coordinate of the component.
   * @param width  The width of the component.
   * @param height The height of the component.
   * @param image  The image to be displayed by the component.
   */
  public ImageComponent(final double x, final double y, final double width, final double height, final Image image) {
    super(x, y, width, height);
    this.setImage(image);
  }

  /**
   * Constructs an ImageComponent with the specified position, dimensions, spritesheet, text, and image.
   *
   * @param x           The x-coordinate of the component.
   * @param y           The y-coordinate of the component.
   * @param width       The width of the component.
   * @param height      The height of the component.
   * @param spritesheet The spritesheet to be used by the component.
   * @param text        The text to be displayed by the component.
   * @param image       The image to be displayed by the component.
   */
  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text,
    final Image image) {
    this(x, y, width, height, text);
    this.spritesheet = spritesheet;
    this.setImageAlign(Align.LEFT);
    this.setImageValign(Valign.TOP);
    if (image != null) {
      this.baseImage = (BufferedImage) image;
    }
  }

  /**
   * Retrieves the background image for the component based on its current state. The image is fetched from a cache if available, otherwise it is
   * generated and added to the cache.
   *
   * @return The background image, or null if no spritesheet is set.
   */
  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }

    final String cacheKey =
      String.format("%d_%b_%b_%b_%.0f_%.0f_%s_%d", getSpritesheet().getName().hashCode(), isHovered(), isPressed(), isEnabled(), getWidth(),
        getHeight(), getSpritesheetScaleMode().name().toLowerCase(), getSpritesheetScaleInterpolation());

    return Resources.images().tryGet(cacheKey).orElseGet(() -> {
      BufferedImage img;
      if (getSpritesheetScaleMode() == ImageScaleMode.SLICE) {
        img = Imaging.nineSlice(getSpritesheet(), (int) getWidth(), (int) getHeight(), getSpritesheetScaleFactor());
      } else {
        int spriteIndex = BACKGROUND_INDEX;
        if (!this.isEnabled() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_DISABLED_INDEX) {
          spriteIndex = BACKGROUND_DISABLED_INDEX;
        } else if (this.isPressed() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_PRESSED_INDEX) {
          spriteIndex = BACKGROUND_PRESSED_INDEX;
        } else if (this.isHovered() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_HOVER_INDEX) {
          spriteIndex = BACKGROUND_HOVER_INDEX;
        }

        img =
          Imaging.scale(this.getSpritesheet().getSprite(spriteIndex), (int) this.getWidth(), (int) this.getHeight(), spritesheetScaleInterpolation);
      }
      if (img != null) {
        Resources.images().add(cacheKey, img);
      }
      return img;
    });
  }

  /**
   * Rescales the base image according to the component's dimensions and the specified image scale mode. The rescaled image is cached to improve
   * performance on subsequent calls.
   */
  public void rescaleImage() {
    if (baseImage == null) {
      return;
    }
    int imageWidth = (int) this.getWidth();
    int imageHeight = (int) this.getHeight();
    boolean keepRatio;

    switch (getImageScaleMode()) {
      case STRETCH -> keepRatio = false;
      case FIT -> keepRatio = true;
      default -> {
        return;
      }
    }

    final String cacheKey = String.format("%s_%dx%d_%b", baseImage.hashCode(), imageWidth, imageHeight, keepRatio);

    this.scaledImage = Resources.images().tryGet(cacheKey)
      .orElseGet(() -> {
        BufferedImage scaled = Imaging.scale(baseImage, imageWidth, imageHeight, getImageScaleInterpolation(), keepRatio);
        Resources.images().add(cacheKey, scaled);
        return scaled;
      });
  }

  /**
   * Retrieves the image to be displayed by the component. If a scaled image is available, it returns the scaled image; otherwise, it returns the base
   * image.
   *
   * @return The image to be displayed by the component.
   */
  public BufferedImage getImage() {
    return scaledImage != null ? scaledImage : baseImage;
  }

  /**
   * Gets the horizontal alignment of the image within the component.
   *
   * @return The horizontal alignment of the image.
   */
  public Align getImageAlign() {
    return imageAlign;
  }

  /**
   * Gets the scale mode for the image.
   *
   * @return The scale mode for the image.
   */
  public ImageScaleMode getImageScaleMode() {
    return imageScaleMode;
  }

  /**
   * Gets the scale mode for the spritesheet.
   *
   * @return The scale mode for the spritesheet.
   */
  public ImageScaleMode getSpritesheetScaleMode() {
    return spritesheetScaleMode;
  }

  /**
   * Gets the scale factor for the spritesheet.
   *
   * @return The scale factor for the spritesheet.
   */
  public float getSpritesheetScaleFactor() {
    return spritesheetScaleFactor;
  }

  /**
   * Gets the vertical alignment of the image within the component.
   *
   * @return The vertical alignment of the image.
   */
  public Valign getImageValign() {
    return imageValign;
  }

  /**
   * Renders the component using the provided Graphics2D context. If the component is suspended or not visible, the method returns immediately.
   * Otherwise, it renders the background image (if available) and the main image (if available), and then calls the superclass's render method.
   *
   * @param g The Graphics2D context used for rendering.
   */
  @Override
  public void render(final Graphics2D g) {
    if (isSuspended() || !isVisible()) {
      return;
    }

    final Image bg = getBackground();
    if (bg != null) {
      ImageRenderer.render(g, bg, getLocation());
    }

    final BufferedImage img = getImage();
    if (img != null) {
      ImageRenderer.render(g, img, getImageLocation(img));
    }
    super.render(g);
  }

  /**
   * Sets the image to be displayed by the component and rescales it.
   *
   * @param image The image to be displayed by the component.
   */
  public void setImage(final Image image) {
    this.baseImage = (BufferedImage) image;
    rescaleImage();
  }

  /**
   * Sets the scale mode for the image and rescales it.
   *
   * @param imageScaleMode The scale mode for the image.
   */
  public void setImageScaleMode(ImageScaleMode imageScaleMode) {
    this.imageScaleMode = imageScaleMode;
    rescaleImage();
  }

  /**
   * Gets the interpolation type used for scaling the image. The interpolation type is one of the AffineTransformOp integer constants: -
   * AffineTransformOp.TYPE_NEAREST_NEIGHBOR - AffineTransformOp.TYPE_BILINEAR - AffineTransformOp.TYPE_BICUBIC
   *
   * @return The interpolation type used for scaling the image.
   */
  public int getImageScaleInterpolation() {
    return imageScaleInterpolation;
  }

  /**
   * Sets the interpolation type used for scaling the image and rescales it. The interpolation type should be one of the AffineTransformOp integer
   * constants: - AffineTransformOp.TYPE_NEAREST_NEIGHBOR - AffineTransformOp.TYPE_BILINEAR - AffineTransformOp.TYPE_BICUBIC
   *
   * @param imageScaleInterpolation The interpolation type used for scaling the image.
   */
  public void setImageScaleInterpolation(int imageScaleInterpolation) {
    this.imageScaleInterpolation = imageScaleInterpolation;
    this.rescaleImage();
  }

  /**
   * Sets the scale mode for the spritesheet and rescales the image.
   *
   * @param spritesheetScaleMode The scale mode for the spritesheet.
   */
  public void setSpritesheetScaleMode(ImageScaleMode spritesheetScaleMode) {
    this.spritesheetScaleMode = spritesheetScaleMode;
    this.rescaleImage();
  }

  /**
   * Sets the scale factor for the spritesheet.
   *
   * @param spritesheetScaleFactor The scale factor for the spritesheet.
   */
  public void setSpritesheetScaleFactor(float spritesheetScaleFactor) {
    this.spritesheetScaleFactor = spritesheetScaleFactor;
  }

  /**
   * Gets the interpolation type used for scaling the spritesheet. The interpolation type should be one of the {@code AffineTransformOp} interpolation
   * constants:
   * <ul>
   *   <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR},
   *   <li>{@link AffineTransformOp#TYPE_BILINEAR},
   *   <li>{@link AffineTransformOp#TYPE_BICUBIC}
   * </ul>
   *
   * @return The interpolation type used for scaling the spritesheet.
   */
  public int getSpritesheetScaleInterpolation() {
    return spritesheetScaleInterpolation;
  }

  /**
   * Sets the interpolation type used for scaling the spritesheet and rescales the image. The interpolation type should be one of the
   * {@code AffineTransformOp} interpolation constants:
   * <ul>
   *   <li>{@link AffineTransformOp#TYPE_NEAREST_NEIGHBOR},
   *   <li>{@link AffineTransformOp#TYPE_BILINEAR},
   *   <li>{@link AffineTransformOp#TYPE_BICUBIC}
   * </ul>
   *
   * @param spritesheetScaleInterpolation The interpolation type used for scaling the spritesheet.
   */
  public void setSpritesheetScaleInterpolation(int spritesheetScaleInterpolation) {
    this.spritesheetScaleInterpolation = spritesheetScaleInterpolation;
    this.rescaleImage();
  }

  /**
   * Sets the spritesheet to be used by the component.
   *
   * @param spr The spritesheet to be used by the component.
   */
  public void setSpritesheet(final Spritesheet spr) {
    this.spritesheet = spr;
  }

  /**
   * Sets the spritesheet and its scale mode to be used by the component.
   *
   * @param spr       The spritesheet to be used by the component.
   * @param scaleMode The scale mode for the spritesheet.
   */
  public void setSpritesheet(final Spritesheet spr, ImageScaleMode scaleMode) {
    setSpritesheet(spr);
    setSpritesheetScaleMode(scaleMode);
  }

  /**
   * Sets the spritesheet, its scale mode, and scale factor to be used by the component.
   *
   * @param spr         The spritesheet to be used by the component.
   * @param scaleMode   The scale mode for the spritesheet.
   * @param scaleFactor The scale factor for the spritesheet.
   */
  public void setSpritesheet(final Spritesheet spr, ImageScaleMode scaleMode, float scaleFactor) {
    setSpritesheet(spr, scaleMode);
    setSpritesheetScaleFactor(scaleFactor);
  }

  /**
   * Sets the horizontal alignment of the image within the component.
   *
   * @param imageAlign The horizontal alignment of the image.
   */
  public void setImageAlign(Align imageAlign) {
    this.imageAlign = imageAlign;
  }

  /**
   * Sets the vertical alignment of the image within the component.
   *
   * @param imageValign The vertical alignment of the image.
   */
  public void setImageValign(Valign imageValign) {
    this.imageValign = imageValign;
  }

  /**
   * Sets the height of the component and rescales the image.
   *
   * @param height The new height of the component.
   */
  @Override
  public void setHeight(double height) {
    super.setHeight(height);
    rescaleImage();
  }

  /**
   * Sets the width of the component and rescales the image.
   *
   * @param width The new width of the component.
   */
  @Override
  public void setWidth(double width) {
    super.setWidth(width);
    rescaleImage();
  }

  /**
   * Retrieves the spritesheet used by the component.
   *
   * @return The spritesheet used by the component.
   */
  protected Spritesheet getSpritesheet() {
    return spritesheet;
  }

  /**
   * Calculates the location of the image within the component based on the alignment and scaling mode.
   *
   * @param img The image for which the location is to be calculated.
   * @return A {@code Point2D} object representing the location of the image.
   */

  private Point2D getImageLocation(final Image img) {
    double x = getX();
    double y = getY();
    if (getImageScaleMode() == ImageScaleMode.STRETCH) {
      return new Point2D.Double(x, y);
    }

    if (getImageAlign() == Align.RIGHT) {
      x += getWidth() - img.getWidth(null);
    } else if (getImageAlign() == Align.CENTER) {
      x += (getWidth() - img.getWidth(null)) / 2.0;
    }

    if (getImageValign() == Valign.DOWN) {
      y += getHeight() - img.getHeight(null);
    } else if (getImageValign() == Valign.MIDDLE) {
      y += (getHeight() - img.getHeight(null)) / 2.0;
    }

    return new Point2D.Double(x, y);
  }
}
