package de.gurkenlabs.litiengine.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class ImageComponent extends GuiComponent {

  /** The font. */
  private Font font;

  private Image image;

  private double imageX, imageY, imageWidth, imageHeight, textX, textY, defaultTextX, defaultTextY;

  private final Spritesheet spritesheet;

  /** The text. */
  private String text;

  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text, final Image image, final Sound hoverSound) {
    super(x, y, width, height);
    this.spritesheet = spritesheet;
    if (FontLoader.getGuiFont() != null) {
      this.setFont(FontLoader.getGuiFont().deriveFont((float) (this.getHeight() * 3 / 6f)));
    }

    this.setText(text);
    this.textX = -1;
    this.textY = -1;

    this.imageWidth = this.getWidth() - this.getWidth() * 9 / 10;
    this.imageHeight = this.getHeight() - this.getHeight() * 9 / 10;
    this.imageX = this.getX() + this.getWidth() * 1 / 20;
    this.imageY = this.getY() + this.getWidth() * 1 / 20;
    if (image != null) {
      this.image = image;
    }
    this.onHovered(e -> {
      if (hoverSound != null) {
        Game.getSoundEngine().playSound(hoverSound);
      }
    });
  }

  /**
   * Gets the font.
   *
   * @return the font
   */
  public Font getFont() {
    return this.font;
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

  public Spritesheet getSpritesheet() {
    return this.spritesheet;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */
  public String getText() {
    return this.text;
  }

  public double getTextX() {
    return this.textX;
  }

  public double getTextY() {
    return this.textY;
  }

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

    g.setColor(this.getTextColor());
    g.setFont(this.getFont());
    final FontMetrics fm = g.getFontMetrics();

    this.defaultTextY = this.getY() + fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
    this.defaultTextX = this.getX() + this.getWidth() / 2 - fm.stringWidth(this.getText()) / 2;

    float y;
    if (this.getTextY() != this.defaultTextY && this.getTextY() >= 0) {
      y = (float) this.getTextY();
    } else {
      y = (float) this.defaultTextY;
    }

    float x;
    if (this.getTextX() != this.defaultTextX && this.getTextX() >= 0) {
      x = (float) this.getTextX();
    } else {
      x = (float) this.defaultTextX;
    }
    RenderEngine.drawText(g, this.getText(), x, y);
    super.render(g);
  }

  /**
   * Sets the font.
   *
   * @param font
   *          the new font
   */
  public void setFont(final Font font) {
    this.font = font;
  }

  public void setImage(final Image image) {
    this.image = image;
  }

  /**
   * Sets the text.
   *
   * @param text
   *          the new text
   */
  public void setText(final String text) {
    this.text = text;
  }

  public void setFontSize(final int size) {
    this.font = new Font(this.getFont().getName(), Font.PLAIN, size);
  }

  public void setTextX(final double x) {
    this.textX = x;
  }

  public void setTextY(final double y) {
    this.textY = y;
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
    ImageCache.SPRITES.putPersistent(cacheKey, img);
    return img;
  }

  @Override
  protected void initializeComponents() {
  }
}
