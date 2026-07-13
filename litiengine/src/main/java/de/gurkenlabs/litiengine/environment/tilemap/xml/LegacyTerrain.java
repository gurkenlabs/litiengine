package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
class LegacyTerrain extends CustomPropertyProvider {
  @XmlAttribute
  private String name;

  @XmlAttribute
  private int tile;

  String getName() {
    return this.name;
  }

  int getTileId() {
    return this.tile;
  }
}
