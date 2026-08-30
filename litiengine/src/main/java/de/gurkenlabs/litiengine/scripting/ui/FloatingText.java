package de.gurkenlabs.litiengine.scripting.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;

/// Represents a transient floating text item displayed in the game world.
public final class FloatingText {
  private final String text;
  private final Point2D location;
  private final Color color;
  private final Font font;
  private final int duration;
  private final double velocityY;
  private int elapsed;

  public FloatingText(String text, Point2D location, Color color, Font font, int duration, double velocityY) {
    this.text = text == null ? "" : text;
    this.location = new Point2D.Double(location.getX(), location.getY());
    this.color = color == null ? Color.WHITE : color;
    this.font = font;
    this.duration = Math.max(1, duration);
    this.velocityY = velocityY;
  }

  public String getText() {
    return this.text;
  }

  public Point2D getLocation() {
    return this.location;
  }

  public Color getColor() {
    return this.color;
  }

  public Font getFont() {
    return this.font;
  }

  public int getDuration() {
    return this.duration;
  }

  public int getElapsed() {
    return this.elapsed;
  }

  public float getProgress() {
    return Math.clamp((float) this.elapsed / (float) this.duration, 0f, 1f);
  }

  public boolean isFinished() {
    return this.elapsed >= this.duration;
  }

  void update(int deltaMs) {
    this.elapsed += deltaMs;
    double deltaSeconds = deltaMs / 1000.0;
    this.location.setLocation(this.location.getX(), this.location.getY() + this.velocityY * deltaSeconds);
  }
}
