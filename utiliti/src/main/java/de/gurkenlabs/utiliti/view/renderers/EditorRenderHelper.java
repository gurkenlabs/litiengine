package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import java.awt.geom.Rectangle2D;

final class EditorRenderHelper {
  private EditorRenderHelper() {}

  static Rectangle2D toScreen(Rectangle2D rect) {
    double scale = Game.world().camera().getRenderScale();
    double x = (rect.getX() + Game.world().camera().getPixelOffsetX()) * scale;
    double y = (rect.getY() + Game.world().camera().getPixelOffsetY()) * scale;
    return new Rectangle2D.Double(x, y, rect.getWidth() * scale, rect.getHeight() * scale);
  }
}
