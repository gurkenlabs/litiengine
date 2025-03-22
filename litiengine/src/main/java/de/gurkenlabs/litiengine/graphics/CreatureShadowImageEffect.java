package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Represents an image effect that applies a shadow to a creature. Extends the ImageEffect class.
 */
public class CreatureShadowImageEffect extends ImageEffect {
  private static final Color DEFAULT_SHADOW_COLOR = new Color(124, 164, 174, 120);

  private final Creature creature;
  private final Color shadowColor;

  private float offsetX = 0;
  private float offsetY = 0;

  /**
   * Initializes a new instance of the {@code CreatureShadowImageEffect}.
   *
   * @param creature The creature to which this affect will be applied to.
   */
  public CreatureShadowImageEffect(final Creature creature) {
    this(creature, DEFAULT_SHADOW_COLOR);
  }

  /**
   * Initializes a new instance of the {@code CreatureShadowImageEffect}.
   *
   * @param creature    The creature to which this affect will be applied to.
   * @param shadowColor The color of the shadow.
   */
  public CreatureShadowImageEffect(final Creature creature, final Color shadowColor) {
    super(0, "shadow");
    this.creature = creature;
    this.shadowColor = shadowColor;
  }

  /**
   * Gets the creature associated with this image effect.
   *
   * @return the creature associated with this image effect
   */
  public Creature getCreature() {
    return this.creature;
  }

  /**
   * Gets the horizontal offset for the shadow.
   *
   * @return the horizontal offset for the shadow
   */
  public float getOffsetX() {
    return this.offsetX;
  }

  /**
   * Sets the horizontal offset for the shadow.
   *
   * @param offsetX the new horizontal offset for the shadow
   * @return the updated CreatureShadowImageEffect instance
   */
  public CreatureShadowImageEffect setOffsetX(float offsetX) {
    this.offsetX = offsetX;
    return this;
  }

  /**
   * Gets the vertical offset for the shadow.
   *
   * @return the vertical offset for the shadow
   */
  public float getOffsetY() {
    return this.offsetY;
  }

  /**
   * Sets the vertical offset for the shadow.
   *
   * @param offsetY the new vertical offset for the shadow
   * @return the updated CreatureShadowImageEffect instance
   */
  public CreatureShadowImageEffect setOffsetY(float offsetY) {
    this.offsetY = offsetY;
    return this;
  }

  /**
   * Applies the shadow effect to the specified BufferedImage.
   *
   * @param image the BufferedImage to apply the effect to
   * @return the BufferedImage with the shadow effect applied
   */
  @Override
  public BufferedImage apply(BufferedImage image) {
    if (this.getCreature().isDead()) {
      return image;
    }

    final BufferedImage buffer =
      Imaging.getCompatibleImage(image.getWidth() * 2 + 2, image.getHeight() * 2);
    final Graphics2D graphics = Objects.requireNonNull(buffer).createGraphics();
    float x = image.getWidth() / 2.0f;
    float y = image.getHeight() / 2.0f;

    this.drawShadow(
      graphics, image.getWidth(), image.getHeight(), x + this.offsetX, y + this.offsetY);

    ImageRenderer.render(graphics, image, x, y);
    graphics.dispose();
    return buffer;
  }

  /**
   * Creates an ellipse representing the shadow based on the sprite dimensions and offsets.
   *
   * @param spriteWidth  the width of the sprite
   * @param spriteHeight the height of the sprite
   * @param offsetX      the horizontal offset for the shadow
   * @param offsetY      the vertical offset for the shadow
   * @return an Ellipse2D object representing the shadow
   */
  protected Ellipse2D getShadowEllipse(
    final float spriteWidth, final float spriteHeight, float offsetX, float offsetY) {
    final double ellipseWidth = 0.60 * spriteWidth;
    final double ellipseHeight = 0.20 * spriteWidth;
    final double startX = (spriteWidth - ellipseWidth) / 2.0;
    final double startY = spriteHeight - ellipseHeight;
    return new Ellipse2D.Double(startX + offsetX, startY + offsetY, ellipseWidth, ellipseHeight);
  }

  /**
   * Draws the shadow on the specified Graphics2D context.
   *
   * @param graphics     the Graphics2D context to draw the shadow on
   * @param spriteWidth  the width of the sprite
   * @param spriteHeight the height of the sprite
   * @param offsetX      the horizontal offset for the shadow
   * @param offsetY      the vertical offset for the shadow
   */
  protected void drawShadow(
    final Graphics2D graphics,
    final float spriteWidth,
    final float spriteHeight,
    float offsetX,
    float offsetY) {
    graphics.setColor(this.shadowColor);
    final RenderingHints hints = graphics.getRenderingHints();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    graphics.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    graphics.fill(getShadowEllipse(spriteWidth, spriteHeight, offsetX, offsetY));
    graphics.setRenderingHints(hints);
  }
}
