package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.awt.*;
import java.util.Objects;

/// One terrain material (a Tiled `wangcolor`) within a [WangSet].
///
/// The representative tile and probability guide terrain painting; tile references use local
/// tileset IDs and `-1` means that no representative tile is configured.
///
/// @see de.gurkenlabs.litiengine.environment.tilemap.ITerrain
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

  /// Creates a white terrain named `Terrain` without a representative tile.
  public WangColor() {
    this("Terrain", Color.WHITE);
  }

  /// Creates a terrain with default probability `1`.
  ///
  /// @param name The terrain name.
  /// @param color The editor display color.
  public WangColor(String name, Color color) {
    this.name = name;
    this.color = color;
    this.tile = -1;
    this.probability = 1;
  }

  /// Creates a copy, including custom properties.
  ///
  /// @param original The terrain to copy.
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

  /// Sets the terrain name; blank values are normalized to `null`.
  ///
  /// @param name The new name.
  public void setName(String name) {
    this.name = name == null || name.isBlank() ? null : name;
  }

  @Override
  public Color getColor() {
    return this.color;
  }

  /// Sets the editor display color.
  ///
  /// @param color The non-null color.
  /// @throws NullPointerException if `color` is `null`.
  public void setColor(Color color) {
    this.color = Objects.requireNonNull(color);
  }

  /// Returns the local ID of the representative tile.
  ///
  /// @return The tile ID, or `-1` when none is configured.
  public int getTileId() {
    return this.tile;
  }

  /// Sets the representative local tile ID.
  ///
  /// @param tileId The tile ID, or `-1` for none.
  /// @throws IllegalArgumentException if `tileId` is less than `-1`.
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

  /// Sets the relative probability used by terrain painting tools.
  ///
  /// @param probability A finite, non-negative weight.
  /// @throws IllegalArgumentException if the value is negative or not finite.
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
