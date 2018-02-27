package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.MathUtilities;

public abstract class ColorLayer implements IRenderable {
  private final IEnvironment environment;
  private Image image;

  private int alpha;
  private Color color;

  protected ColorLayer(IEnvironment env, final Color ambientColor, final int ambientAlpha) {
    this.environment = env;
    this.color = ambientColor;
    this.alpha = ambientAlpha;
    this.createImage();
  }
  
  @Override
  public void render(Graphics2D g) {
    RenderEngine.renderImage(g, this.getImage(), Game.getCamera().getViewPortLocation(0, 0));
  }

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }
  
  public Color getColorWithAlpha() {
    return new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());
  }

  public void setAlpha(int ambientAlpha) {
    this.alpha = MathUtilities.clamp(ambientAlpha, 0, 255);
    this.createImage();
  }

  public void setColor(final Color color) {
    this.color = color;
    this.createImage();
  }

  public void createImage(){
    if (this.getColor() == null) {
      return;
    }

    final String cacheKey = this.getCacheKey();
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      this.setImage(ImageCache.IMAGES.get(cacheKey));
      return;
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getEnvironment().getMap().getSizeInPixels().getWidth(), (int) this.getEnvironment().getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();
    
    this.renderLayer(g);
    
    g.dispose();
    this.setImage(img);

    ImageCache.IMAGES.put(cacheKey, img);
  }
  
  protected abstract void renderLayer(Graphics2D g);
  
  protected Image getImage() {
    this.createImage();
    return this.image;
  }

  protected abstract String getCacheKey();

  protected IEnvironment getEnvironment() {
    return this.environment;
  }

  protected void setImage(Image image) {
    this.image = image;
  }
}
