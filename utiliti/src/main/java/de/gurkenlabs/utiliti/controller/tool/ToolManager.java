package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ToolManager {
  private static ToolManager instance;

  private final List<Tool> tools;
  private final List<Runnable> listeners;
  private Tool activeTool;
  private ITileLayer activeTileLayer;
  private int selectedTileGid;
  private WangSet selectedTerrainSet;
  private WangColor selectedTerrain;

  private ToolManager() {
    this.tools = new ArrayList<>();
    this.listeners = new ArrayList<>();
  }

  public static ToolManager instance() {
    if (instance == null) {
      instance = new ToolManager();
    }
    return instance;
  }

  public void register(Tool tool) {
    tools.add(tool);
    if (activeTool == null) {
      activeTool = tool;
    }
  }

  public List<Tool> getTools() {
    return Collections.unmodifiableList(tools);
  }

  public Tool getActiveTool() {
    return activeTool;
  }

  public void setActiveTool(Tool tool) {
    if (activeTool != null && activeTool.equals(tool)) {
      return;
    }
    if (activeTool != null) {
      activeTool.deactivated();
    }
    activeTool = tool;
    if (activeTool != null) {
      activeTool.activated();
    }
    for (Runnable listener : listeners) {
      listener.run();
    }
  }

  public ITileLayer getActiveTileLayer() {
    return activeTileLayer;
  }

  public void setActiveTileLayer(ITileLayer activeTileLayer) {
    this.activeTileLayer = activeTileLayer;
  }

  public int getSelectedTileGid() {
    return selectedTileGid;
  }

  public void setSelectedTileGid(int selectedTileGid) {
    this.selectedTileGid = selectedTileGid;
  }

  public WangSet getSelectedTerrainSet() {
    return this.selectedTerrainSet;
  }

  public WangColor getSelectedTerrain() {
    return this.selectedTerrain;
  }

  public void setSelectedTerrain(WangSet terrainSet, WangColor terrain) {
    this.selectedTerrainSet = terrainSet;
    this.selectedTerrain = terrain;
    for (Runnable listener : this.listeners) {
      listener.run();
    }
  }

  public void clearSelections() {
    this.activeTileLayer = null;
    this.selectedTileGid = 0;
    this.selectedTerrainSet = null;
    this.selectedTerrain = null;
    for (Runnable listener : this.listeners) {
      listener.run();
    }
  }

  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  public void removeListener(Runnable listener) {
    listeners.remove(listener);
  }

  public static void reset() {
    instance = null;
  }
}
