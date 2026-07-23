package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Graphics2D;

final class TransparencyGrid {
  private static final int CELL_SIZE = 10;

  private TransparencyGrid() {
  }

  static void paint(Graphics2D graphics, int width, int height) {
    Color first = blend(Style.background(), Style.raisedSurface(), 0.25f);
    Color second = blend(Style.background(), Style.raisedSurface(), 0.45f);
    for (int y = 0; y < height; y += CELL_SIZE) {
      for (int x = 0; x < width; x += CELL_SIZE) {
        graphics.setColor(((x / CELL_SIZE + y / CELL_SIZE) & 1) == 0 ? first : second);
        graphics.fillRect(x, y, CELL_SIZE, CELL_SIZE);
      }
    }
  }

  private static Color blend(Color first, Color second, float amount) {
    float inverse = 1f - amount;
    return new Color(
        Math.round(first.getRed() * inverse + second.getRed() * amount),
        Math.round(first.getGreen() * inverse + second.getGreen() * amount),
        Math.round(first.getBlue() * inverse + second.getBlue() * amount));
  }
}
