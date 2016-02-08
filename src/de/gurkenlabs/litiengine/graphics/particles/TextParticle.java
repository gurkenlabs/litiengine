package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;

public class TextParticle extends Particle {
  private final String text;
  private Font font;

  public TextParticle(final String text, float xCurrent, float yCurrent, float dx, float dy, float deltaIncX, float deltaIncY, int life, Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, 0, 0, life, color);
    this.text = text;
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(Game.getScreenManager().getCamera().getViewPortLocation(emitterOrigin));
    g.setColor(this.getColor());
    Font oldFont = g.getFont();
    if (this.getFont() != null) {
      g.setFont(this.getFont());
    }
    FontMetrics fm = g.getFontMetrics();
    int x = fm.stringWidth(this.getText()) / 2;
    g.drawString(this.getText(), (int) renderLocation.getX()- x, (int) renderLocation.getY());
    g.setFont(oldFont);
  }

  public String getText() {
    return text;
  }

  public Font getFont() {
    return this.font;
  }

  public void setFont(final Font font) {
    this.font = font;
  }
}
