package com.litiengine.environment.tilemap.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import com.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlAccessorType(XmlAccessType.FIELD)
public class Frame implements ITileAnimationFrame {
  @XmlAttribute
  private int tileid;

  @XmlAttribute
  private int duration;

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
}
