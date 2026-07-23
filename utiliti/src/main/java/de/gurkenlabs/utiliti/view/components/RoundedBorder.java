package de.gurkenlabs.utiliti.view.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {
  private final java.awt.Color color;
  private final int arc;
  private final int padding;

  public RoundedBorder(java.awt.Color color, int arc, int padding) {
    this.color = color;
    this.arc = arc;
    this.padding = padding;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
    } finally {
      g2.dispose();
    }
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return new Insets(padding, padding + 2, padding, padding + 2);
  }

  @Override
  public boolean isBorderOpaque() {
    return false;
  }
}
