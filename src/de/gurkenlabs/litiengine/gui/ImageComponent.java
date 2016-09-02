package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class ImageComponent extends GuiComponent {
  private final List<Consumer<String>> textChangedConsumer;
  /** The font. */
  private Font font;

  private Image image;

  private double imageX, imageY, imageWidth, imageHeight, textX, textY, defaultTextX, defaultTextY;

  private Spritesheet spritesheet;

  /** The text. */
  private String text;

  private int textAlignment = TEXT_ALIGN_CENTER;
  private Sound hoverSound;

  public ImageComponent(final double x, final double y, final double width, final double height, final Spritesheet spritesheet, final String text, final Image image) {
    super(x, y, width, height);
    this.textChangedConsumer = new CopyOnWriteArrayList<>();
    this.spritesheet = spritesheet;
    try {
      this.setFont(FontLoader.getDefaultFont().deriveFont((float) (this.getHeight() * 3 / 6f)));
      this.setTextColor(Color.BLACK);
    } catch (Exception e) {
      System.out.println("default font not found.");
    }

    this.text = text;
    this.textX = -1;
    this.textY = -1;

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
    if (this.getText() != null) {
      final FontMetrics fm = g.getFontMetrics();

      this.defaultTextY = this.getY() + fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
      final int MINPADDING_X = 10;
      switch (this.getTextAlignment()) {
      case TEXT_ALIGN_LEFT:
        this.defaultTextX = this.getX() + MINPADDING_X;
        break;
      case TEXT_ALIGN_RIGHT:
        this.defaultTextX = this.getX() + this.getWidth() - MINPADDING_X - fm.stringWidth(this.getText());
        break;
      default:
      case TEXT_ALIGN_CENTER:
        this.defaultTextX = this.getX() + this.getWidth() / 2 - fm.stringWidth(this.getText()) / 2;
        break;
      }

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
    }
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
    for (Consumer<String> cons : this.textChangedConsumer) {
      cons.accept(this.getText());
    }
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

    if (img != null) {
      ImageCache.SPRITES.putPersistent(cacheKey, img);
    }

    return img;
  }

  @Override
  protected void initializeComponents() {
  }

  public int getTextAlignment() {
    return textAlignment;
  }

  public void setTextAlignment(int textAlignment) {
    this.textAlignment = textAlignment;
  }

  public void onTextChanged(final Consumer<String> cons) {
    this.textChangedConsumer.add(cons);
  }
}
