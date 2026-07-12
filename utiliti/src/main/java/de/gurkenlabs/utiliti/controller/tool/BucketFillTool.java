package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Cursor;
import javax.swing.Icon;

public class BucketFillTool extends TileBrushTool {
  @Override
  public String getName() {
    return Resources.strings().get("tool_bucketFill");
  }

  @Override
  public Icon getIcon() {
    return Icons.COLOR_24;
  }

  @Override
  public Cursor getCursor() {
    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }

  @Override
  public void mousePressed(ComponentMouseEvent event) {
    if (!javax.swing.SwingUtilities.isLeftMouseButton(event.getEvent())) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.ITileLayer layer = activeLayer();
    java.awt.Point start = currentTile(event);
    if (layer == null || start == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.ITile initialTile = layer.getTile(start.x, start.y);
    int replacement = ToolManager.instance().getSelectedTileGid();
    if (initialTile == null || initialTile.getGridId() == replacement) {
      return;
    }
    int target = initialTile.getGridId();
    beginPainting();
    java.util.ArrayDeque<java.awt.Point> pending = new java.util.ArrayDeque<>();
    pending.add(new java.awt.Point(start));
    while (!pending.isEmpty()) {
      java.awt.Point tileLocation = pending.removeFirst();
      de.gurkenlabs.litiengine.environment.tilemap.ITile tile = layer.getTile(tileLocation.x, tileLocation.y);
      if (tile == null || tile.getGridId() != target || !paintTile(layer, tileLocation, replacement)) {
        continue;
      }
      pending.add(new java.awt.Point(tileLocation.x - 1, tileLocation.y));
      pending.add(new java.awt.Point(tileLocation.x + 1, tileLocation.y));
      pending.add(new java.awt.Point(tileLocation.x, tileLocation.y - 1));
      pending.add(new java.awt.Point(tileLocation.x, tileLocation.y + 1));
    }
    endPainting();
  }

  @Override
  public void mouseReleased(ComponentMouseEvent event) {
  }

  @Override
  public boolean showInToolbar() {
    return false;
  }
}
