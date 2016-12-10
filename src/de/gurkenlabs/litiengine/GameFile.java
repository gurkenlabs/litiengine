package de.gurkenlabs.litiengine;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.xml.Map;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  private static final long serialVersionUID = -2101786184799276518L;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private List<Map> maps;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpriteSheetInfo> spriteSheets;

  @XmlElement
  private GameInfo info;

  public GameFile() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.info = new GameInfo();
  }

  @XmlTransient
  public List<Map> getMaps() {
    return maps;
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
