package de.gurkenlabs.litiengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.gurkenlabs.tiled.tmx.IMap;

public class GameFile implements Serializable {
  private static final long serialVersionUID = -2101786184799276518L;
  private IMap map;
  private HashMap<String, String> mapLayer;
  private List<SpriteSheetInfo> spriteSheets;

  public GameFile() {
    this.mapLayer = new HashMap<>();
    this.spriteSheets = new ArrayList<>();
  }

  public IMap getMap() {
    return map;
  }

  public void setMap(IMap map) {
    this.map = map;
  }

  public HashMap<String, String> getMapLayer() {
    return mapLayer;
  }

  public void setMapLayer(HashMap<String, String> mapLayer) {
    this.mapLayer = mapLayer;
  }

  public List<SpriteSheetInfo> getSpriteSheets() {
    return spriteSheets;
  }

  public void setSpriteSheets(List<SpriteSheetInfo> spriteSheets) {
    this.spriteSheets = spriteSheets;
  }
}
