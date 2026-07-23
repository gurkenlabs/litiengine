package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

public class StampBrushTool extends TileBrushTool {
  private TileStamp strokeStamp = TileStamp.empty();

  @Override
  public String getName() {
    return Resources.strings().get("tool_stampBrush");
  }

  @Override
  public Icon getIcon() {
    return Icons.PENCIL_24;
  }

  @Override
  public Cursor getCursor() {
    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }

  @Override
  public void mousePressed(ComponentMouseEvent event) {
    TileStamp selectedStamp = ToolManager.instance().getSelectedTileStamp();
    if (!javax.swing.SwingUtilities.isLeftMouseButton(event.getEvent()) || selectedStamp.isEmpty()) {
      return;
    }
    this.strokeStamp = selectedStamp;
    beginPainting();
    paintStamp(activeLayer(), currentTile(event), this.strokeStamp);
  }

  @Override
  public void mouseDragged(ComponentMouseEvent event) {
    paintStamp(activeLayer(), currentTile(event), this.strokeStamp);
  }

  @Override
  public void mouseReleased(ComponentMouseEvent event) {
    endPainting();
    this.strokeStamp = TileStamp.empty();
  }

  void paintStamp(
      ITileLayer layer, Point cursor, TileStamp stamp) {
    if (layer == null || cursor == null || stamp == null || stamp.isEmpty()) {
      return;
    }
    for (StampCell cell : stampCells(cursor, stamp)) {
      repaintTile(layer, cell.location(), cell.gid());
    }
  }

  static List<StampCell> stampCells(Point cursor, TileStamp stamp) {
    if (cursor == null || stamp == null || stamp.isEmpty()) {
      return List.of();
    }
    List<StampCell> cells = new ArrayList<>();
    int originX = cursor.x - stamp.width() / 2;
    int originY = cursor.y - stamp.height() / 2;
    for (int y = 0; y < stamp.height(); y++) {
      for (int x = 0; x < stamp.width(); x++) {
        int gid = stamp.gidAt(x, y);
        if (gid != 0) {
          cells.add(new StampCell(new Point(originX + x, originY + y), gid));
        }
      }
    }
    return List.copyOf(cells);
  }

  @Override
  public void deactivated() {
    super.deactivated();
    this.strokeStamp = TileStamp.empty();
  }

  @Override
  public boolean showInToolbar() {
    return true;
  }

  record StampCell(Point location, int gid) {}
}
