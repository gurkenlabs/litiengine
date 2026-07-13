package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.awt.*;
import java.util.Objects;

@XmlRootElement(name = "wangcolor")
@XmlAccessorType(XmlAccessType.FIELD)
public class WangColor extends CustomPropertyProvider implements ITerrain {

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

  public WangColor() {
    this("Terrain", Color.WHITE);
  }

  public WangColor(String name, Color color) {
    this.name = name;
    this.color = color;
    this.tile = -1;
    this.probability = 1;
  }

  public WangColor(WangColor original) {
    super(original);
    this.name = original.name;
    this.wangColorClass = original.wangColorClass;
    this.color = original.color;
    this.tile = original.tile;
    this.probability = original.probability;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name == null || name.isBlank() ? null : name;
  }

  @Override
  public Color getColor() {
    return this.color;
  }

  public void setColor(Color color) {
    this.color = Objects.requireNonNull(color);
  }

  public int getTileId() {
    return this.tile;
  }

  public void setTileId(int tileId) {
    if (tileId < -1) {
      throw new IllegalArgumentException("Representative tile ID must be -1 or greater.");
    }
    this.tile = tileId;
  }

  @Override
  public double getProbability() {
    return this.probability;
  }

  public void setProbability(double probability) {
    if (!Double.isFinite(probability) || probability < 0) {
      throw new IllegalArgumentException("Terrain probability must be finite and non-negative.");
    }
    this.probability = probability;
  }

  @Override
  public String toString() {
    return this.name != null ? this.name : "Unnamed terrain";
  }
}
