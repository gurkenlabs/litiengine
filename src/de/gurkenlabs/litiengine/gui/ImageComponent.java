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

  private double imageX, imageY, imageWidth, imageHeight;

  private Spritesheet spritesheet;

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

  public double getImageHeight() {
    return this.imageHeight;
  }

  public Point2D getImageLocation() {
    return new Point2D.Double(this.getImageX(), this.getImageY());
  }

  public double getImageWidth() {
    return this.imageWidth;
  }

  public double getImageX() {
    return this.imageX;
  }

  public double getImageY() {
    return this.imageY;
  }

  public Spritesheet getSpritesheet() {
    return this.spritesheet;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */

  public void relocateImage(final double x, final double y, final double width, final double height) {
    this.imageX = x;
    this.imageY = y;
    this.imageWidth = width;
    this.imageHeight = height;
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.isSuspended() || !this.isVisible()) {
      return;
    }
    final Image bg = this.getBackground();

    RenderEngine.renderImage(g, bg, this.getPosition());
    final Image img = this.getImage();
    if (img != null) {
      RenderEngine.renderImage(g, img, this.getImageLocation());
    }

    super.render(g);
  }

  @Override
  public void setHeight(final double height) {
    super.setHeight(height);
    this.imageHeight = this.getHeight() * 0.9;
    this.imageY = this.getY() + this.getHeight() * 0.05;

  }

  @Override
  public void setHoverSound(final Sound hoverSound) {
    this.hoverSound = hoverSound;
  }

  public void setImage(final Image image) {
    if (this == null || image == null) {
      return;
    }
    BufferedImage scaledImage = ImageProcessing.scaleImage((BufferedImage) image, (int) (this.getWidth() * 9 / 10), (int) (this.getHeight() * 9 / 10), true);
    if (scaledImage == null) {
      return;
    }
    this.image = scaledImage;

    final double x = this.getBoundingBox().getCenterX() - this.image.getWidth(null) / 2;
    final double y = this.getBoundingBox().getCenterY() - this.image.getHeight(null) / 2;
    this.relocateImage(x, y, image.getWidth(null), image.getHeight(null));

  }

  public void setImageHeight(final double newHeight) {
    this.imageHeight = newHeight;
  }

  public void setImageWidth(final double newWidth) {
    this.imageWidth = newWidth;
  }

  public void setImageX(final double newX) {
    this.imageX = newX;
  }

  public void setImageY(final double newY) {
    this.imageY = newY;
  }

  public void setSpriteSheet(final Spritesheet spr) {
    this.spritesheet = spr;
  }

  @Override
  public void setWidth(final double width) {
    super.setWidth(width);
    this.imageWidth = this.getWidth() * 0.9;
    this.imageX = this.getX() + this.getWidth() * 0.05;

  }

  @Override
  protected void initializeComponents() {
  }

}
