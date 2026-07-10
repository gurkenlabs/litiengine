package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlAccessorType(XmlAccessType.FIELD)
public class Frame implements ITileAnimationFrame {
  @XmlAttribute
  private int tileid;

  @XmlAttribute
  private int duration;

  public Frame() {
  }

  public Frame(int tileid, int duration) {
    this.tileid = tileid;
    this.duration = duration;
  }

  public Frame(ITileAnimationFrame original) {
    this(original.getTileId(), original.getDuration());
  }

  @Override
  @XmlTransient
  public int getTileId() {
    return this.tileid;
  }

  @Override
  @XmlTransient
  public int getDuration() {
    return this.duration;
  }

  public void setTileId(int tileid) {
    this.tileid = tileid;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }
}
