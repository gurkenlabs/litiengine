package de.gurkenlabs.utiliti.renderers;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.utiliti.components.Editor;

public class GridRenderer implements IEditorRenderer {
  @Override
  public String getName() {
    return "GRID";
  }

  @Override
  public void render(Graphics2D g) {
    // render the grid
    if (Editor.preferences().showGrid() && Game.world().camera().getRenderScale() >= 1 && Game.world().environment() != null) {

      final IMap map = Game.world().environment().getMap();
      if (map == null) {
        return;
      }

      g.setColor(Editor.preferences().getGridColor());
      final Stroke stroke = new BasicStroke(Editor.preferences().getGridLineWidth() / Game.world().camera().getRenderScale());
      for (int x = 0; x < map.getWidth(); x++) {
        for (int y = 0; y < map.getHeight(); y++) {
          Shape tile = map.getOrientation().getShape(x, y, map);
          if (Game.world().camera().getViewport().intersects(tile.getBounds2D())) {
            RenderEngine.renderOutline(g, tile, stroke);
          }
        }
      }
    }
  }
}
