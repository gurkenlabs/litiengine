package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.MathUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class ColorLayer implements IRenderable {
  private final Environment environment;
  private final BufferedImage layer;

  private Color color;

  protected ColorLayer(Environment env, final Color color) {
    this.environment = env;
    this.color = color;

    Dimension size = env.getMap().getSizeInPixels();
    this.layer = Imaging.getCompatibleImage(size.width, size.height);
    this.updateSection(this.environment.getMap().getBounds());
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D viewport = Game.world().camera().getViewport();
    ImageRenderer.render(g, this.layer, -viewport.getX(), -viewport.getY());
  }

  public Color getColor() {
    return this.color;
  }

  public void setAlpha(int ambientAlpha) {
    this.setColor(
        new Color(
            this.getColor().getRed(),
            this.getColor().getGreen(),
            this.getColor().getBlue(),
            Math.clamp(ambientAlpha, 0, 255)));
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void setColor(final Color color) {
    if (color == null) {
      return;
    }

    this.color = color;
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void updateSection(Rectangle2D section) {
    if (this.getColor() == null) {
      return;
    }
    final Graphics2D g = layer.createGraphics();
    clearSection(g, section);
    g.setClip(section);
//    g.translate(section.getX(),section.getY());
    renderSection(g, section);
    g.dispose();
  }

  protected abstract void renderSection(Graphics2D g, Rectangle2D section);

  protected abstract void clearSection(Graphics2D g, Rectangle2D section);

  protected Environment getEnvironment() {
    return this.environment;
  }
}
