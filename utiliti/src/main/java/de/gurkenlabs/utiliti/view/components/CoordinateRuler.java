package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Camera-aware world-coordinate ruler for the map viewport. */
final class CoordinateRuler extends JPanel {
  private static final int HORIZONTAL_BASE_HEIGHT = 24;
  private static final int VERTICAL_BASE_WIDTH = 42;
  private static final int MIN_MAJOR_SPACING = 78;
  private final int orientation;

  CoordinateRuler(int orientation) {
    if (orientation != Adjustable.HORIZONTAL && orientation != Adjustable.VERTICAL) {
      throw new IllegalArgumentException("Unsupported ruler orientation: " + orientation);
    }
    this.orientation = orientation;
    setOpaque(true);
    setPreferredSize(orientation == Adjustable.HORIZONTAL
        ? new Dimension(0, horizontalHeight())
        : new Dimension(verticalWidth(), 0));
    getAccessibleContext().setAccessibleName(
        orientation == Adjustable.HORIZONTAL ? "Horizontal map ruler" : "Vertical map ruler");
  }

  @Override public void updateUI() {
    super.updateUI();
    setBackground(Style.workspaceTop());
  }

  @Override protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    if (Game.world() == null || Game.world().camera() == null) {
      return;
    }

    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(Style.border());
      if (this.orientation == Adjustable.HORIZONTAL) {
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
      } else {
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
      }

      double scale = Math.max(0.0001, Game.world().camera().getRenderScale());
      double majorStep = majorStep(scale);
      double minorStep = majorStep / 4.0;
      int canvasExtent = this.orientation == Adjustable.HORIZONTAL
          ? Game.window().getRenderComponent().getWidth()
          : Game.window().getRenderComponent().getHeight();
      double focus = this.orientation == Adjustable.HORIZONTAL
          ? Game.world().camera().getFocus().getX()
          : Game.world().camera().getFocus().getY();
      double offset = -focus + canvasExtent / (2.0 * scale);
      int extent = Math.min(
          this.orientation == Adjustable.HORIZONTAL ? getWidth() : getHeight(),
          canvasExtent);
      double worldStart = -offset;
      double worldEnd = extent / scale - offset;
      double first = Math.ceil(worldStart / minorStep) * minorStep;
      FontMetrics metrics = g.getFontMetrics();

      for (double world = first; world <= worldEnd + minorStep / 2; world += minorStep) {
        int screen = (int) Math.round((world + offset) * scale);
        long minorIndex = Math.round(world / minorStep);
        boolean major = Math.floorMod(minorIndex, 4) == 0;
        g.setColor(major ? Style.mutedText() : Style.border());
        if (this.orientation == Adjustable.HORIZONTAL) {
          int tick = major ? 8 : 4;
          g.drawLine(screen, getHeight() - 1, screen, getHeight() - 1 - tick);
          if (major) {
            String label = coordinateLabel(world);
            g.drawString(label, screen + 4, Math.max(metrics.getAscent(), getHeight() - 10));
          }
        } else {
          int tick = major ? 8 : 4;
          g.drawLine(getWidth() - 1, screen, getWidth() - 1 - tick, screen);
          if (major) {
            String label = coordinateLabel(world);
            g.drawString(label, 4, screen - 3);
          }
        }
      }
    } finally {
      g.dispose();
    }
  }

  static double majorStep(double renderScale) {
    double step = 1;
    while (step * renderScale < MIN_MAJOR_SPACING) {
      step *= 2;
    }
    return step;
  }

  static int horizontalHeight() {
    return Math.max(HORIZONTAL_BASE_HEIGHT,
        Math.round(HORIZONTAL_BASE_HEIGHT * Editor.preferences().getUiScale()));
  }

  static int verticalWidth() {
    return Math.max(VERTICAL_BASE_WIDTH,
        Math.round(VERTICAL_BASE_WIDTH * Editor.preferences().getUiScale()));
  }

  private static String coordinateLabel(double coordinate) {
    return Math.abs(coordinate - Math.rint(coordinate)) < 0.001
        ? Long.toString(Math.round(coordinate))
        : String.format("%.1f", coordinate);
  }
}
