package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.util.Imaging;

public class CreatureShadowImageEffect extends ImageEffect {
  private static final Color DEFAULT_SHADOW_COLOR = new Color(124, 164, 174, 120);

  private final Creature creature;
  private final Color shadowColor;

  public CreatureShadowImageEffect(final Creature creature) {
    this(creature, DEFAULT_SHADOW_COLOR);
  }

  public CreatureShadowImageEffect(final Creature creature, final Color shadowColor) {
    super(0, "shadow");
    this.creature = creature;
    this.shadowColor = shadowColor;
  }

  public Creature getCreature() {
    return this.creature;
  }

  @Override
  public BufferedImage apply(BufferedImage image) {
    if (this.getCreature().isDead()) {
      return image;
    }

    final BufferedImage buffer = Imaging.getCompatibleImage(image.getWidth() * 2 + 2, image.getHeight() * 2);
    final Graphics2D graphics = buffer.createGraphics();
    float offsetX = image.getWidth() / 2.0f;
    float offsetY = image.getHeight() / 2.0f;

    this.drawShadow(graphics, image.getWidth(), image.getHeight(), offsetX, offsetY);

    ImageRenderer.render(graphics, image, offsetX, offsetY);
    graphics.dispose();
    return buffer;
  }

  protected Ellipse2D getShadowEllipse(final float spriteWidth, final float spriteHeight, float offsetX, float offsetY) {
    final double ellipseWidth = 0.60 * spriteWidth;
    final double ellipseHeight = 0.20 * spriteWidth;
    final double startX = (spriteWidth - ellipseWidth) / 2.0;
    final double startY = spriteHeight - ellipseHeight;
    return new Ellipse2D.Double(startX + offsetX, startY + offsetY, ellipseWidth, ellipseHeight);
  }

  protected void drawShadow(final Graphics2D graphics, final float spriteWidth, final float spriteHeight, float offsetX, float offsetY) {
    graphics.setColor(this.shadowColor);
    final RenderingHints hints = graphics.getRenderingHints();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    graphics.fill(getShadowEllipse(spriteWidth, spriteHeight, offsetX, offsetY));
    graphics.setRenderingHints(hints);
  }
}
