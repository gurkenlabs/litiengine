package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "objecttypes")
public class ObjectTypes {
  @XmlElement(name = "objecttype")
  private final List<Tile> data = null;
}
