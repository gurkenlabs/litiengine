package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Represents an abstract color layer that can be rendered and updated.
 */
public abstract class ColorLayer implements IRenderable {
  private final Environment environment;
  private final BufferedImage layer;

  private Color color;

  /**
   * Constructs a new ColorLayer with the specified environment and color.
   *
   * @param env   The environment associated with this color layer.
   * @param color The initial color of the layer.
   */
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

  /**
   * Gets the current color of the layer.
   *
   * @return The current color of the layer.
   */
  public Color getColor() {
    return this.color;
  }

  /**
   * Sets the alpha value of the current color.
   *
   * @param ambientAlpha The alpha value to set, clamped between 0 and 255.
   */
  public void setAlpha(int ambientAlpha) {
    this.setColor(
      new Color(
        this.getColor().getRed(),
        this.getColor().getGreen(),
        this.getColor().getBlue(),
        Math.clamp(ambientAlpha, 0, 255)));
    this.updateSection(this.environment.getMap().getBounds());
  }

  /**
   * Sets the color of the layer.
   *
   * @param color The color to set. If null, the method returns without making changes.
   */
  public void setColor(final Color color) {
    if (color == null) {
      return;
    }

    this.color = color;
    this.updateSection(this.environment.getMap().getBounds());
  }

  /**
   * Updates a specific section of the layer.
   *
   * @param section The section of the layer to update.
   */
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

  /**
   * Renders a specific section of the layer.
   *
   * @param g       The graphics context to use for rendering.
   * @param section The section of the layer to render.
   */
  protected abstract void renderSection(Graphics2D g, Rectangle2D section);

  /**
   * Clears a specific section of the layer.
   *
   * @param g       The graphics context to use for clearing.
   * @param section The section of the layer to clear.
   */
  protected abstract void clearSection(Graphics2D g, Rectangle2D section);

  /**
   * Gets the environment associated with this color layer.
   *
   * @return The environment associated with this color layer.
   */
  protected Environment getEnvironment() {
    return this.environment;
  }
}
