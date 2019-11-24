package de.gurkenlabs.litiengine.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.swing.JLabel;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public class ImageComponent extends GuiComponent {
  public static final int BACKGROUND_INDEX = 0;
  public static final int BACKGROUND_HOVER_INDEX = 1;
  public static final int BACKGROUND_PRESSED_INDEX = 2;
  public static final int BACKGROUND_DISABLED_INDEX = 3;

  private Image image;

  private Spritesheet spritesheet;

  private ImageScaleMode imageScaleMode;
  private Align imageAlign;
  private Valign imageValign;

  public ImageComponent(final double x, final double y, final Image image) {
    super(x, y, image.getWidth(null), image.getHeight(null));
    this.image = image;
  }

  public ImageComponent(final double x, final double y, final double width, final double height) {
    super(x, y, width, height);
  }

  public ImageComponent(final double x, final double y, final double width, final double height, final String text) {
    super(x, y, width, height);
    Font defFont = new JLabel().getFont().deriveFont((float) (this.getHeight() * 3 / 6f));
    if (this.getFont() == null) {
      this.setFont(defFont);
    }
    this.setText(text);
  }

  public ImageComponent(final double x, final double y, final double width, final double height, final Image image) {
    super(x, y, width, height);
    this.setImage(image);
  }

  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text, final Image image) {
    this(x, y, width, height,text);
    this.spritesheet = spritesheet;
    this.setImageAlign(Align.LEFT);
    this.setImageValign(Valign.TOP);
    if (image != null) {
      this.image = image;
    }
  }

  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }

    final String cacheKey = this.getSpritesheet().getName().hashCode() + "_" + this.isHovered() + "_" + this.isPressed() + "_" + this.isEnabled() + "_" + this.getWidth() + "x" + this.getHeight();
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    int spriteIndex = BACKGROUND_INDEX;
    if (!this.isEnabled() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_DISABLED_INDEX) {
      spriteIndex = BACKGROUND_DISABLED_INDEX;
    } else if (this.isPressed() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_PRESSED_INDEX) {
      spriteIndex = BACKGROUND_PRESSED_INDEX;
    } else if (this.isHovered() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_HOVER_INDEX) {
      spriteIndex = BACKGROUND_HOVER_INDEX;
    }

    BufferedImage img = Imaging.scale(this.getSpritesheet().getSprite(spriteIndex), (int) this.getWidth(), (int) this.getHeight());
    if (img != null) {
      Resources.images().add(cacheKey, img);
    }

    return img;
  }

  public Image getImage() {
    BufferedImage bufferedImage = Imaging.toBufferedImage(this.image);
    if (bufferedImage != null) {
      int imageWidth = this.image.getWidth(null);
      int imageHeight = this.image.getHeight(null);
      if (this.getImageScaleMode() != null) {
        boolean keepRatio;

        switch (this.getImageScaleMode()) {
        case STRETCH:
          imageWidth = (int) this.getWidth();
          imageHeight = (int) this.getHeight();
          keepRatio = false;
          break;
        case FIT:
          imageWidth = (int) this.getWidth();
          imageHeight = (int) this.getHeight();
          keepRatio = true;
          break;
        default:
          keepRatio = false;
          break;
        }

        bufferedImage = Imaging.scale(bufferedImage, imageWidth, imageHeight, keepRatio);
        imageWidth = bufferedImage.getWidth();
        imageHeight = bufferedImage.getHeight();
      }

      final String cacheKey = this.image.hashCode() + "_" + imageWidth + "+" + imageHeight;
      Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
      if (opt.isPresent()) {
        return opt.get();
      }

      Resources.images().add(cacheKey, bufferedImage);
    }

    return bufferedImage;
  }

  public Align getImageAlign() {
    return this.imageAlign;
  }

  public ImageScaleMode getImageScaleMode() {
    return this.imageScaleMode;
  }

  public Valign getImageValign() {
    return this.imageValign;
  }

  protected Spritesheet getSpritesheet() {
    return this.spritesheet;
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

    final Image img = this.getImage();
    if (img != null) {
      ImageRenderer.render(g, img, this.getImageLocation(img));
    }
    super.render(g);
  }

  public void setImage(final Image image) {
    this.image = image;
  }

  public void setImageScaleMode(ImageScaleMode imageScaleMode) {
    this.imageScaleMode = imageScaleMode;
  }

  public void setSpriteSheet(final Spritesheet spr) {
    this.spritesheet = spr;
  }

  public void setImageAlign(Align imageAlign) {
    this.imageAlign = imageAlign;
  }

  public void setImageValign(Valign imageValign) {
    this.imageValign = imageValign;
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
