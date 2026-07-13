package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Cursor;
import java.awt.Point;
import javax.swing.Icon;

public class TerrainBrushTool extends TileBrushTool {
  @Override
  public String getName() {
    return "Terrain Brush";
  }

  @Override
  public Icon getIcon() {
    return Icons.TERRAIN_24;
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
    beginPainting();
    paintTerrain(currentTile(event));
  }

  @Override
  public void mouseDragged(ComponentMouseEvent event) {
    paintTerrain(currentTile(event));
  }

  @Override
  public void mouseReleased(ComponentMouseEvent event) {
    endPainting();
  }

  private void paintTerrain(Point location) {
    WangSet terrainSet = ToolManager.instance().getSelectedTerrainSet();
    WangColor terrain = ToolManager.instance().getSelectedTerrain();
    if (terrainSet == null || terrain == null) {
      Editor.instance().setCurrentStatus("Select a terrain first");
      return;
    }

    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    var map = Game.world().environment().getMap();
    if (map.getOrientation() == MapOrientations.ISOMETRIC_STAGGERED
        || map.getOrientation() == MapOrientations.HEXAGONAL) {
      Editor.instance().setCurrentStatus("Terrain Brush does not support staggered or hexagonal maps");
      return;
    }

    ITileset tileset = map.getTilesets().stream()
      .filter(set -> set.getTerrainSets() != null && set.getTerrainSets().contains(terrainSet))
      .findFirst()
      .orElse(null);
    ITileLayer layer = activeLayer();
    if (tileset == null || layer == null || location == null) {
      return;
    }

    int terrainIndex = terrainSet.getTerrains().indexOf(terrain) + 1;
    if (terrainIndex <= 0) {
      Editor.instance().setCurrentStatus("Select a terrain from the active terrain set");
      return;
    }
    TerrainResolver.Result result = TerrainResolver.resolve(layer, tileset, terrainSet, terrainIndex, location);
    for (var change : result.changes().entrySet()) {
      repaintTile(layer, change.getKey(), change.getValue());
    }
    if (result.invalidCells() > 0) {
      Editor.instance().setCurrentStatus("Missing terrain transition");
    }
  }
}
