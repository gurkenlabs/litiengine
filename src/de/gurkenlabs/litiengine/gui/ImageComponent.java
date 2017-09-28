package de.gurkenlabs.litiengine.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import javax.swing.JLabel;

import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.ImageProcessing;

public class ImageComponent extends GuiComponent {
  public static final int BACKGROUND_INDEX = 0;
  public static final int BACKGROUND_HOVER_INDEX = 1;
  public static final int BACKGROUND_PRESSED_INDEX = 2;
  public static final int BACKGROUND_DISABLED_INDEX = 3;

  private Sound hoverSound;

  private Image image;

  private Spritesheet spritesheet;

  public ImageComponent(final double x, final double y, final Image image) {
    super(x, y, image.getWidth(null), image.getHeight(null));
    this.image = image;
  }

  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text, final Image image) {
    super(x, y, width, height);
    this.spritesheet = spritesheet;

    Font defFont = new JLabel().getFont().deriveFont((float) (this.getHeight() * 3 / 6f));
    if (this.getAppearance().getFont() == null) {
      this.getAppearance().setFont(defFont);
    }

    if (this.getAppearanceDisabled().getFont() == null) {
      this.getAppearanceDisabled().setFont(defFont);
    }

    if (this.getAppearanceHovered().getFont() == null) {
      this.getAppearanceHovered().setFont(defFont);
    }

    this.setText(text);

    if (image != null) {
      this.image = image;
    }
  }

  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }

    final String cacheKey = MessageFormat.format("{0}_{1}_{2}_{3}_{4}x{5}", this.getSpritesheet().getName().hashCode(), this.isHovered(), this.isPressed(), this.isEnabled(), this.getWidth(), this.getHeight());
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    int spriteIndex = BACKGROUND_INDEX;
    if (!this.isEnabled() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_DISABLED_INDEX) {
      spriteIndex = BACKGROUND_DISABLED_INDEX;
    } else if (this.isPressed() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_PRESSED_INDEX) {
      spriteIndex = BACKGROUND_PRESSED_INDEX;
    } else if (this.isHovered() && this.getSpritesheet().getTotalNumberOfSprites() > BACKGROUND_HOVER_INDEX) {
      spriteIndex = BACKGROUND_HOVER_INDEX;
    }

    BufferedImage img = ImageProcessing.scaleImage(this.getSpritesheet().getSprite(spriteIndex), (int) this.getWidth(), (int) this.getHeight());
    if (img != null) {
      ImageCache.SPRITES.put(cacheKey, img);
    }

    return img;
  }

  @Override
  public Sound getHoverSound() {
    return this.hoverSound;
  }

  public Image getImage() {
    return this.image;
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
      RenderEngine.renderImage(g, bg, this.getLocation());
    }

    final Image img = this.getImage();
    if (img != null) {
      RenderEngine.renderImage(g, img, this.getLocation());
    }

    super.render(g);
  }

  @Override
  public void setHoverSound(final Sound hoverSound) {
    this.hoverSound = hoverSound;
  }

  public void setImage(final Image image) {
    this.image = image;
  }

  public void setSpriteSheet(final Spritesheet spr) {
    this.spritesheet = spr;
  }
}
