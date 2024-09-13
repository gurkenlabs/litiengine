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
import java.util.Optional;
import javax.swing.JLabel;

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
  private float spritesheetScaleFactor = 1f;
  private Align imageAlign = Align.CENTER;
  private Valign imageValign = Valign.MIDDLE;

  public ImageComponent(final double x, final double y, final Image image) {
    super(x, y, image.getWidth(null), image.getHeight(null));
    this.baseImage = (BufferedImage) image;
  }

  public ImageComponent(final double x, final double y, final double width, final double height) {
    super(x, y, width, height);
  }

  public ImageComponent(
    final double x, final double y, final double width, final double height, final String text) {
    super(x, y, width, height);
    Font defFont = new JLabel().getFont().deriveFont((float) (this.getHeight() * 3 / 6f));
    if (this.getFont() == null) {
      this.setFont(defFont);
    }
    this.setText(text);
  }

  public ImageComponent(
    final double x, final double y, final double width, final double height, final Image image) {
    super(x, y, width, height);
    this.setImage(image);
  }

  public ImageComponent(
    final double x,
    final double y,
    final double width,
    final double height,
    final Spritesheet spritesheet,
    final String text,
    final Image image) {
    this(x, y, width, height, text);
    this.spritesheet = spritesheet;
    this.setImageAlign(Align.LEFT);
    this.setImageValign(Valign.TOP);
    if (image != null) {
      this.baseImage = (BufferedImage) image;
    }
  }

  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }

    final String cacheKey =
      getSpritesheet().getName().hashCode()
        + "_"
        + isHovered()
        + "_"
        + isPressed()
        + "_"
        + isEnabled()
        + "_"
        + getWidth()
        + "_"
        + getSpritesheetScaleMode().name().toLowerCase()
        + "x"
        + getHeight();
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }
    BufferedImage img;
    if (getSpritesheetScaleMode() == ImageScaleMode.SLICE) {
      img = Imaging.nineSlice(getSpritesheet(), (int) getWidth(), (int) getHeight(),
        getSpritesheetScaleFactor());
    } else {
      int spriteIndex = BACKGROUND_INDEX;
      if (!this.isEnabled()
        && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_DISABLED_INDEX) {
        spriteIndex = BACKGROUND_DISABLED_INDEX;
      } else if (this.isPressed()
        && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_PRESSED_INDEX) {
        spriteIndex = BACKGROUND_PRESSED_INDEX;
      } else if (this.isHovered()
        && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_HOVER_INDEX) {
        spriteIndex = BACKGROUND_HOVER_INDEX;
      }

      img =
        Imaging.scale(
          this.getSpritesheet().getSprite(spriteIndex),
          (int) this.getWidth(),
          (int) this.getHeight());
    }
    if (img != null) {
      Resources.images().add(cacheKey, img);
    }
    return img;
  }

  public void rescaleImage() {
    if (this.baseImage == null) {
      return;
    }
    int imageWidth = (int) this.getWidth();
    int imageHeight = (int) this.getHeight();
    boolean keepRatio;

    switch (this.getImageScaleMode()) {
      case STRETCH -> keepRatio = false;
      case FIT -> keepRatio = true;
      default -> {
        return;
      }
    }

    final String cacheKey = String.format("%s_%dx%d_%b", this.baseImage.hashCode(), imageWidth, imageHeight, keepRatio);

    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      this.scaledImage = opt.get();
      return;
    } else {
      this.scaledImage = Imaging.scale(this.baseImage, imageWidth, imageHeight, AffineTransformOp.TYPE_BICUBIC, keepRatio);
    }
    Resources.images().add(cacheKey, this.scaledImage);
  }

  public BufferedImage getImage() {
    if (this.scaledImage == null) {
      return this.baseImage;
    }
    return this.scaledImage;
  }

  public Align getImageAlign() {
    return this.imageAlign;
  }

  public ImageScaleMode getImageScaleMode() {
    return imageScaleMode;
  }

  public ImageScaleMode getSpritesheetScaleMode() {
    return spritesheetScaleMode;
  }

  public float getSpritesheetScaleFactor() {
    return spritesheetScaleFactor;
  }

  public Valign getImageValign() {
    return this.imageValign;
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.isSuspended() || !this.isVisible()) {
      return;
    }

    final Image bg = this.getBackground();
    if (bg != null) {
      ImageRenderer.render(g, bg, this.getLocation());
    }

    final BufferedImage img = this.getImage();
    if (img != null) {
      ImageRenderer.render(g, img, this.getImageLocation(img));
    }
    super.render(g);
  }

  public void setImage(final Image image) {
    this.baseImage = (BufferedImage) image;
    this.rescaleImage();
  }

  public void setImageScaleMode(ImageScaleMode imageScaleMode) {
    this.imageScaleMode = imageScaleMode;
    this.rescaleImage();
  }

  public void setSpritesheetScaleMode(ImageScaleMode spritesheetScaleMode) {
    this.spritesheetScaleMode = spritesheetScaleMode;
    this.rescaleImage();
  }

  public void setSpritesheetScaleFactor(float spritesheetScaleFactor) {
    this.spritesheetScaleFactor = spritesheetScaleFactor;
  }

  public void setSpritesheet(final Spritesheet spr) {
    this.spritesheet = spr;
  }

  public void setSpritesheet(final Spritesheet spr, ImageScaleMode scaleMode) {
    setSpritesheet(spr);
    setSpritesheetScaleMode(scaleMode);
  }

  public void setSpritesheet(final Spritesheet spr, ImageScaleMode scaleMode, float scaleFactor) {
    setSpritesheet(spr, scaleMode);
    setSpritesheetScaleFactor(scaleFactor);
  }

  public void setImageAlign(Align imageAlign) {
    this.imageAlign = imageAlign;
  }

  public void setImageValign(Valign imageValign) {
    this.imageValign = imageValign;
  }

  @Override
  public void setHeight(double height) {
    super.setHeight(height);
    this.rescaleImage();
  }

  @Override
  public void setWidth(double width) {
    super.setWidth(width);
    this.rescaleImage();
  }

  protected Spritesheet getSpritesheet() {
    return this.spritesheet;
  }

  private Point2D getImageLocation(final Image img) {
    double x = this.getX();
    double y = this.getY();
    if (this.getImageScaleMode() == ImageScaleMode.STRETCH) {
      return new Point2D.Double(x, y);
    }

    if (this.getImageAlign() == Align.RIGHT) {
      x = x + this.getWidth() - img.getWidth(null);
    } else if (this.getImageAlign() == Align.CENTER) {
      x = x + this.getWidth() / 2.0 - img.getWidth(null) / 2.0;
    }

    if (this.getImageValign() == Valign.DOWN) {
      y = y + this.getHeight() - img.getHeight(null);
    } else if (this.getImageValign() == Valign.MIDDLE) {
      y = y + this.getHeight() / 2.0 - img.getHeight(null) / 2.0;
    }

    return new Point2D.Double(x, y);
  }
}
