package de.gurkenlabs.litiengine.environment.tilemap.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlRootElement(name = "frame")
@XmlAccessorType(XmlAccessType.FIELD)
public class Frame implements ITileAnimationFrame{

  @XmlAttribute
  private int tileid;

  @XmlAttribute
  private int duration;

  @XmlTransient
  public int getTileId() {
    return this.tileid;
  }

  @XmlTransient
  public int getDuration() {
    return this.duration;
  }
}
