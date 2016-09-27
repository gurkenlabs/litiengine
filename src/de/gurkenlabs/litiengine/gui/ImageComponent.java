package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class ImageComponent extends GuiComponent {


  private Image image;

  private double imageX, imageY, imageWidth, imageHeight;

  private Spritesheet spritesheet;

  private Sound hoverSound;

  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text, final Image image) {
    super(x, y, width, height);
    this.spritesheet = spritesheet;
    try {
      this.setFont(FontLoader.getDefaultFont().deriveFont((float) (this.getHeight() * 3 / 6f)));
      this.setTextColor(Color.BLACK);
    } catch (Exception e) {
      System.out.println("default font not found.");
    }

    this.setText(text);


    this.imageWidth = this.getWidth() * 0.9;
    this.imageHeight = this.getHeight() * 0.9;
    this.imageX = this.getX() + this.getWidth() * 0.05;
    this.imageY = this.getY() + this.getHeight() * 0.05;
    if (image != null) {
      this.image = image;
    }
  }

  public Sound getHoverSound() {
    return hoverSound;
  }

  public void setHoverSound(Sound hoverSound) {
    this.hoverSound = hoverSound;
  }


  public Image getImage() {
    return this.image;
  }

  public double getImageHeight() {
    return this.imageHeight;
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

  public void setImageX(double newX) {
    this.imageX = newX;
  }

  public void setImageY(double newY) {
    this.imageY = newY;
  }

  public void setImageWidth(double newWidth) {
    this.imageWidth = newWidth;
  }

  public void setImageHeight(double newHeight) {
    this.imageHeight = newHeight;
  }

  public Spritesheet getSpritesheet() {
    return this.spritesheet;
  }

  public void setSpriteSheet(Spritesheet spr) {
    this.spritesheet = spr;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */


 
  public void relocateImage(final int x, final int y, final int width, final int height) {
    this.imageX = x;
    this.imageY = y;
    this.imageWidth = width;
    this.imageHeight = height;
  }

  public Point2D getImageLocation() {
    return new Point2D.Double(this.getImageX(), this.getImageY());
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

  
  

  public void setImage(final Image image) {
    this.image = image;
  }



  public Image getBackground() {
    if (this.getSpritesheet() == null) {
      return null;
    }
    final String cacheKey = MessageFormat.format("{0}_{1}_{2}_{3}x{4}", this.getSpritesheet().getPath().hashCode(), this.isHovered(), this.isPressed(), this.getWidth(), this.getHeight());
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
      ImageCache.SPRITES.putPersistent(cacheKey, img);
    }

    return img;
  }

  @Override
  protected void initializeComponents() {
  }


}
