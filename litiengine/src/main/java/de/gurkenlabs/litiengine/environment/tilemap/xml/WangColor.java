package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.awt.*;

@XmlRootElement(name = "wangcolor")
@XmlAccessorType(XmlAccessType.FIELD)
public class WangColor implements ITerrain {

  @XmlAttribute
  private String name;

  @XmlAttribute(name = "class")
  private String wangColorClass;

  @XmlJavaTypeAdapter(ColorAdapter.class)
  @XmlAttribute
  private Color color;

  @XmlAttribute
  private int tile;

  @XmlAttribute
  private double probability;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Color getColor() {
    return this.color;
  }

  @Override
  public double getProbability() {
    return this.probability;
  }
}
