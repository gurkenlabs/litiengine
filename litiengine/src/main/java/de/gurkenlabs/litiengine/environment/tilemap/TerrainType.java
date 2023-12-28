package de.gurkenlabs.litiengine.environment.tilemap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum TerrainType {
  @XmlEnumValue("corner")
  Corner,

  @XmlEnumValue("edge")
  Edge,

  @XmlEnumValue("mixed")
  Mixed
}
