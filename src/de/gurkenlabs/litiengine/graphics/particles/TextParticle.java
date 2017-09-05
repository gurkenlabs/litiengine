package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class TextParticle extends Particle {
  private Font font;
  private final String text;

  public TextParticle(final String text, final float xCurrent, final float yCurrent, final float dx, final float dy, final float deltaIncX, final float deltaIncY, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, 0, 0, life, color);
    this.text = text;
  }

  public Font getFont() {
    return this.font;
  }

  public String getText() {
    return this.text;
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    final Font oldFont = g.getFont();
    if (this.getFont() != null) {
      g.setFont(this.getFont());
    }
    final FontMetrics fm = g.getFontMetrics();
    final int x = fm.stringWidth(this.getText()) / 2;
    g.drawString(this.getText(), (float) renderLocation.getX() - x, (float) renderLocation.getY());
    g.setFont(oldFont);
  }

  public void setFont(final Font font) {
    this.font = font;
  }
}
