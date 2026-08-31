package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/// Associates one local tile ID with the terrains at its four corners and four edges.
///
/// Wang IDs always contain eight entries in Tiled order. Zero means no terrain; positive values are
/// one-based indexes into [WangSet#getTerrains()]. Arrays supplied to or returned from this class are
/// defensively copied.
@XmlAccessorType(XmlAccessType.FIELD)
public class WangTile {
  private static final int WANG_ID_LENGTH = 8;

  @XmlAttribute
  private int tileid;

  @XmlAttribute
  @XmlJavaTypeAdapter(IntegerArrayAdapter.class)
  private int[] wangid;

  /// Creates tile zero with no assigned terrains.
  public WangTile() {
    this(0, new int[WANG_ID_LENGTH]);
  }

  /// Creates a tile assignment.
  ///
  /// @param tileId The local tileset tile ID.
  /// @param wangId Up to eight terrain indexes; missing entries become zero.
  public WangTile(int tileId, int[] wangId) {
    this.tileid = tileId;
    this.wangid = normalize(wangId);
  }

  /// Creates an independent copy.
  ///
  /// @param original The assignment to copy.
  public WangTile(WangTile original) {
    this(original.tileid, original.wangid);
  }

  /// Returns the local tileset tile ID.
  ///
  /// @return The tile ID.
  public int getTileId() {
    return this.tileid;
  }

  /// Returns the eight terrain indexes.
  ///
  /// @return A defensive copy of the Wang ID.
  public int[] getWangId() {
    this.wangid = normalize(this.wangid);
    return this.wangid.clone();
  }

  /// Replaces all terrain indexes.
  ///
  /// @param wangId Up to eight indexes; negative values become zero.
  public void setWangId(int[] wangId) {
    this.wangid = normalize(wangId);
  }

  /// Assigns one corner or edge position.
  ///
  /// @param position The Tiled Wang position from zero through seven.
  /// @param terrain The non-negative, one-based terrain index, or zero for none.
  /// @throws IndexOutOfBoundsException if `position` is outside zero through seven.
  /// @throws IllegalArgumentException if `terrain` is negative.
  public void setTerrain(int position, int terrain) {
    if (position < 0 || position >= WANG_ID_LENGTH) {
      throw new IndexOutOfBoundsException(position);
    }
    if (terrain < 0) {
      throw new IllegalArgumentException("Terrain index must be non-negative.");
    }
    this.wangid = normalize(this.wangid);
    this.wangid[position] = terrain;
  }

  private static int[] normalize(int[] wangId) {
    int[] normalized = new int[WANG_ID_LENGTH];
    if (wangId != null) {
      for (int i = 0; i < Math.min(wangId.length, normalized.length); i++) {
        normalized[i] = Math.max(0, wangId[i]);
      }
    }
    return normalized;
  }
}
