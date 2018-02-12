package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Image;

import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.util.MathUtilities;

public abstract class ColorLayer {
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

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }

  public void setAlpha(int ambientAlpha) {
    this.alpha = MathUtilities.clamp(ambientAlpha, 0, 255);
    this.createImage();
  }

  public void setColor(final Color color) {
    this.color = color;
    this.createImage();
  }

  public abstract void createImage();

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
