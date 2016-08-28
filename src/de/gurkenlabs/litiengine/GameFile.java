package de.gurkenlabs.litiengine;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.xml.Map;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  private static final long serialVersionUID = -2101786184799276518L;

  @XmlElementWrapper(name = "maps")
  private List<Map> maps;

  @XmlElementWrapper(name = "maplayers")
  private HashMap<String, String> mapLayer;

  @XmlElementWrapper(name = "spriteSheets")
  private List<SpriteSheetInfo> spriteSheets;

  @XmlElement
  private GameInfo info;

  public GameFile() {
    this.mapLayer = new HashMap<>();
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.info = new GameInfo();
  }

  @XmlTransient
  public List<Map> getMaps() {
    return maps;
  }

  @XmlTransient
  public HashMap<String, String> getMapLayer() {
    return mapLayer;
  }

  public void setMapLayer(HashMap<String, String> mapLayer) {
    this.mapLayer = mapLayer;
  }

  @XmlTransient
  public List<SpriteSheetInfo> getSpriteSheets() {
    return spriteSheets;
  }

  public void setSpriteSheets(List<SpriteSheetInfo> spriteSheets) {
    this.spriteSheets = spriteSheets;
  }

  @XmlTransient
  public GameInfo getInfo() {
    return this.info;
  }

  public void setInfo(GameInfo info) {
    this.info = info;
  }

}
