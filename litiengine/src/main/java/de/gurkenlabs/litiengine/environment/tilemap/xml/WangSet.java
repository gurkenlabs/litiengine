package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/// A Tiled terrain or Wang set containing terrain colors and per-tile edge/corner assignments.
///
/// Terrain references in [WangTile] are one-based indexes into [#getTerrains()]; index zero means
/// no terrain. The eight positions alternate corners and edges in the order defined by the TMX
/// format. The returned terrain and Wang-tile lists are live JAXB model collections.
///
/// @see de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet
/// @see WangColor
/// @see WangTile
@XmlAccessorType(XmlAccessType.FIELD)
public class WangSet extends CustomPropertyProvider implements ITerrainSet {

  @XmlAttribute
  private String name;

  @XmlAttribute(name = "class")
  private String wangSetClass;

  @XmlAttribute
  private TerrainType type;

  @XmlAttribute
  private int tile;

  @XmlElement(type = WangColor.class)
  private List<ITerrain> wangcolor;

  @XmlElement(name = "wangtile")
  private List<WangTile> wangtiles;

  /// Creates an empty mixed terrain set named `Terrain Set`.
  public WangSet() {
    this("Terrain Set", TerrainType.MIXED);
  }

  /// Creates an empty terrain set.
  ///
  /// @param name The set name.
  /// @param type Whether assignments describe corners, edges, or both.
  public WangSet(String name, TerrainType type) {
    this.name = name;
    this.type = type;
    this.tile = -1;
    this.wangcolor = new ArrayList<>();
    this.wangtiles = new ArrayList<>();
  }

  /// Creates a deep copy of a terrain set.
  ///
  /// @param original The set to copy.
  public WangSet(WangSet original) {
    super(original);
    this.name = original.name;
    this.wangSetClass = original.wangSetClass;
    this.type = original.type;
    this.tile = original.tile;
    this.wangcolor = new ArrayList<>();
    for (ITerrain terrain : original.getTerrains()) {
      if (terrain instanceof WangColor color) {
        this.wangcolor.add(new WangColor(color));
      }
    }
    this.wangtiles = new ArrayList<>();
    for (WangTile wangtile : original.getWangTiles()) {
      this.wangtiles.add(new WangTile(wangtile));
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  /// Sets the display name; blank values are normalized to `null`.
  ///
  /// @param name The new name.
  public void setName(String name) {
    this.name = name == null || name.isBlank() ? null : name;
  }

  @Override
  public TerrainType getType() {
    return this.type;
  }

  /// Sets which Wang positions this set uses.
  ///
  /// @param type The non-null terrain type.
  /// @throws NullPointerException if `type` is `null`.
  public void setType(TerrainType type) {
    this.type = Objects.requireNonNull(type);
  }

  /// Returns the live terrain list in Wang-index order.
  ///
  /// @return The mutable terrain list.
  @Override
  public List<ITerrain> getTerrains() {
    if (this.wangcolor == null) {
      this.wangcolor = new ArrayList<>();
    }
    return this.wangcolor;
  }

  /// Returns the live list of per-tile assignments.
  ///
  /// @return The mutable Wang-tile list.
  public List<WangTile> getWangTiles() {
    if (this.wangtiles == null) {
      this.wangtiles = new ArrayList<>();
    }
    return this.wangtiles;
  }

  /// Finds or creates the assignment for a local tile ID.
  ///
  /// @param tileId The non-negative local tile ID.
  /// @return The existing or newly added assignment.
  /// @throws IllegalArgumentException if `tileId` is negative.
  public WangTile getOrCreateWangTile(int tileId) {
    if (tileId < 0) {
      throw new IllegalArgumentException("Wang tile ID must be non-negative.");
    }
    for (WangTile wangtile : getWangTiles()) {
      if (wangtile.getTileId() == tileId) {
        return wangtile;
      }
    }
    WangTile wangtile = new WangTile(tileId, new int[8]);
    getWangTiles().add(wangtile);
    return wangtile;
  }

  /// Returns the eight Wang indexes assigned to a local tile.
  ///
  /// @param tileId The local tile ID.
  /// @return A defensive copy, or eight zeroes if the tile has no assignment.
  public int[] getWangId(int tileId) {
    for (WangTile wangtile : getWangTiles()) {
      if (wangtile.getTileId() == tileId) {
        return wangtile.getWangId().clone();
      }
    }
    return new int[8];
  }

  /// Removes a tile assignment if all eight of its indexes are zero.
  ///
  /// @param tileId The local tile ID to inspect.
  public void removeWangTileIfEmpty(int tileId) {
    getWangTiles().removeIf(wangtile -> wangtile.getTileId() == tileId
      && Arrays.stream(wangtile.getWangId()).allMatch(id -> id == 0));
  }

  @Override
  public ITerrain[] getTerrains(int tileId) {
    ITerrain[] terrains = new ITerrain[8];
    for (WangTile wangtile : getWangTiles()) {
      if (wangtile.getTileId() != tileId) {
        continue;
      }
      int[] wangIds = wangtile.getWangId();
      for (int i = 0; i < Math.min(terrains.length, wangIds.length); i++) {
        int terrain = wangIds[i];
        terrains[i] = terrain <= 0 || terrain > getTerrains().size() ? null : getTerrains().get(terrain - 1);
      }
      break;
    }
    return terrains;
  }

  @Override
  public String toString() {
    return this.name != null ? this.name : "Unnamed terrain set";
  }
}
