package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/** A panel shell that clips its complete contents to rounded corners. */
final class RoundedPanel extends JPanel {
  private final int arc;

  RoundedPanel(LayoutManager layout) {
    this(layout, Style.CORNER_RADIUS * 2);
  }

  RoundedPanel(LayoutManager layout, int arc) {
    super(layout);
    this.arc = arc;
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.background());
      g2.fill(createShape());
    } finally {
      g2.dispose();
    }
  }

  @Override
  protected void paintChildren(Graphics graphics) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.clip(createShape());
      super.paintChildren(g2);
    } finally {
      g2.dispose();
    }

    Graphics2D borderGraphics = (Graphics2D) graphics.create();
    try {
      borderGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      borderGraphics.setColor(Style.border());
      borderGraphics.draw(new RoundRectangle2D.Double(
          0.5, 0.5, Math.max(0, getWidth() - 1.0), Math.max(0, getHeight() - 1.0), this.arc, this.arc));
    } finally {
      borderGraphics.dispose();
    }
  }

  private RoundRectangle2D createShape() {
    return new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), this.arc, this.arc);
  }
}
