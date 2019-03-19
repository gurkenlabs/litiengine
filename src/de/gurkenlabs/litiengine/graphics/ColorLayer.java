package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.MathUtilities;

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
    this.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), MathUtilities.clamp(ambientAlpha, 0, 255)));
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void setColor(final Color color) {
    this.color = color;
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void updateSection(Rectangle2D section) {
    if (this.getColor() == null) {
      return;
    }

    int minX = (int) Math.floor(section.getX());
    int minY = (int) Math.floor(section.getY());
    int maxX = (int) Math.ceil(section.getMaxX());
    int maxY = (int) Math.ceil(section.getMaxY());
    Rectangle aligned = new Rectangle(minX, minY, maxX - minX, maxY - minY);

    final Graphics2D g = this.layer.createGraphics();
    g.setColor(new Color(0, 0, 0, 0));
    g.clearRect(aligned.x, aligned.y, aligned.width, aligned.height);
    g.setClip(aligned.x, aligned.y, aligned.width, aligned.height);
    g.translate(aligned.x, aligned.y);
    this.renderSection(g, aligned);
    g.dispose();
  }

  protected abstract void renderSection(Graphics2D g, Rectangle2D section);

  protected Environment getEnvironment() {
    return this.environment;
  }
}
