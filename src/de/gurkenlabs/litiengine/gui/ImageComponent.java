package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import javax.swing.JLabel;

import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.ImageProcessing;

public class ImageComponent extends GuiComponent {

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
    this.setFont(new JLabel().getFont().deriveFont((float) (this.getHeight() * 3 / 6f)));
    this.setTextColor(Color.BLACK);

    this.setText(text);

    if (image != null) {
      this.image = image;
    }
  }

  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }
    final String cacheKey = MessageFormat.format("{0}_{1}_{2}_{3}x{4}", this.getSpritesheet().getName().hashCode(), this.isHovered(), this.isPressed(), this.getWidth(), this.getHeight());
    if (ImageCache.SPRITES.containsKey(cacheKey)) {
      return ImageCache.SPRITES.get(cacheKey);
    }

    BufferedImage img;
    if (this.isHovered() && this.getSpritesheet().getTotalNumberOfSprites() > 1) {
      if (this.isPressed()) {
        img = ImageProcessing.scaleImage(this.getSpritesheet().getSprite(2), (int) this.getWidth(), (int) this.getHeight());
      } else {
        img = ImageProcessing.scaleImage(this.getSpritesheet().getSprite(1), (int) this.getWidth(), (int) this.getHeight());
      }
    } else {
      img = ImageProcessing.scaleImage(this.getSpritesheet().getSprite(0), (int) this.getWidth(), (int) this.getHeight());
    }

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
      System.out.println(this.getLocation() + " - " + this.getX() + "/" + this.getY());
      RenderEngine.renderImage(g, img, this.getLocation());
    }

    super.render(g);
  }

  @Override
  public void setHeight(final double height) {
    super.setHeight(height);
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

  @Override
  public void setWidth(final double width) {
    super.setWidth(width);
  }

  @Override
  protected void initializeComponents() {
  }

}
