package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

abstract class TileBrushTool implements Tool {
  private final Set<Point> paintedTiles = new HashSet<>();
  private boolean painting;

  protected final void beginPainting() {
    if (painting || Game.world().environment() == null) {
      return;
    }
    painting = true;
    paintedTiles.clear();
    UndoManager.instance().beginOperation();
  }

  protected final void endPainting() {
    if (!painting) {
      return;
    }
    painting = false;
    UndoManager.instance().endOperation();
  }

  protected final Point currentTile(ComponentMouseEvent event) {
    if (Game.world().environment() == null) {
      return null;
    }
    IMap map = Game.world().environment().getMap();
    if (map == null || Game.world().camera() == null) {
      return null;
    }
    return Input.mouse().getTile();
  }

  protected final boolean paintTile(ITileLayer layer, Point location, int gid) {
    return paintTile(layer, location, gid, true);
  }

  protected final boolean repaintTile(ITileLayer layer, Point location, int gid) {
    return paintTile(layer, location, gid, false);
  }

  private boolean paintTile(ITileLayer layer, Point location, int gid, boolean oncePerStroke) {
    if (!painting || layer == null || location == null) {
      return false;
    }
    // Input reuses its tile Point. Capture coordinates before recording undo actions.
    int x = location.x;
    int y = location.y;
    if (oncePerStroke && !paintedTiles.add(new Point(x, y))) {
      return false;
    }
    ITile tile = layer.getTile(x, y);
    if (tile == null || tile.getGridId() == gid) {
      return false;
    }
    if (gid != 0 && Game.world().environment().getMap().getTilesetEntry(gid) == null) {
      Editor.instance().setCurrentStatus(Resources.strings().get("status_tileNotInMap"));
      return false;
    }
    int previousGid = tile.getGridId();
    layer.setTile(x, y, gid);
    UndoManager.instance().resourceChanged(
      () -> layer.setTile(x, y, previousGid),
      () -> layer.setTile(x, y, gid));
    Editor.instance().setCurrentStatus(Resources.strings().get(
      "status_paintedTile", Integer.toString(x), Integer.toString(y)));
    return true;
  }

  protected final ITileLayer activeLayer() {
    ITileLayer selectedLayer = ToolManager.instance().getActiveTileLayer();
    if (selectedLayer != null && Game.world().environment() != null && Game.world().environment().getMap() != null
      && Game.world().environment().getMap().getTileLayers().contains(selectedLayer)) {
      return selectedLayer;
    }
    Editor.instance().setCurrentStatus(Resources.strings().get("status_selectTileLayer"));
    return null;
  }

  @Override
  public void deactivated() {
    endPainting();
  }
}
