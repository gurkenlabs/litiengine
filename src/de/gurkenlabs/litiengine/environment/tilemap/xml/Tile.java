package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Point;
import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

/**
 * The Class Tile.
 */
@XmlRootElement(name = "tile")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tile extends CustomPropertyProvider implements ITile, Serializable {
  protected static final long FLIPPED_HORIZONTALLY_FLAG = 0xFFFFFFFF80000000L;
  protected static final long FLIPPED_VERTICALLY_FLAG = 0xFFFFFFFF40000000L;
  protected static final long FLIPPED_DIAGONALLY_FLAG = 0xFFFFFFFF20000000L;

  protected static final long FLIPPED_HORIZONTALLY_FLAG_CSV = 0x80000000L;
  protected static final long FLIPPED_VERTICALLY_FLAG_CSV = 0x40000000L;
  protected static final long FLIPPED_DIAGONALLY_FLAG_CSV = 0x20000000L;

  private static final long serialVersionUID = -7597673646108642906L;

  /** The gid. */
  @XmlAttribute
  private Integer gid;

  @XmlAttribute
  private Integer id;

  @XmlAttribute
  private String terrain;

  @XmlElement(required = false)
  private Animation animation;

  /** The tile coordinate. */
  private transient Point tileCoordinate;

  private transient ITerrain[] terrains;

  private transient boolean flippedDiagonally;
  private transient boolean flippedHorizontally;
  private transient boolean flippedVertically;
  private transient boolean flipped;

  public Tile() {
  }

  public Tile(int gid) {
    this.gid = gid;
  }

  public Tile(long gidBitmask, boolean csv) {
    // Clear the flags
    long tileId = gidBitmask;
    if (csv) {
      tileId &= ~(FLIPPED_HORIZONTALLY_FLAG_CSV | FLIPPED_VERTICALLY_FLAG_CSV | FLIPPED_DIAGONALLY_FLAG_CSV);
      this.flippedDiagonally = (gidBitmask & FLIPPED_DIAGONALLY_FLAG_CSV) == FLIPPED_DIAGONALLY_FLAG_CSV;
      this.flippedHorizontally = (gidBitmask & FLIPPED_HORIZONTALLY_FLAG_CSV) == FLIPPED_HORIZONTALLY_FLAG_CSV;
      this.flippedVertically = (gidBitmask & FLIPPED_VERTICALLY_FLAG_CSV) == FLIPPED_VERTICALLY_FLAG_CSV;
    } else {
      tileId &= ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG);
      this.flippedDiagonally = (gidBitmask & FLIPPED_DIAGONALLY_FLAG) == FLIPPED_DIAGONALLY_FLAG;
      this.flippedHorizontally = (gidBitmask & FLIPPED_HORIZONTALLY_FLAG) == FLIPPED_HORIZONTALLY_FLAG;
      this.flippedVertically = (gidBitmask & FLIPPED_VERTICALLY_FLAG) == FLIPPED_VERTICALLY_FLAG;
    }

    this.flipped = this.isFlippedDiagonally() || this.isFlippedHorizontally() || this.isFlippedVertically();
    this.gid = (int) tileId;
  }

  @Override
  public boolean isFlippedDiagonally() {
    return this.flippedDiagonally;
  }

  @Override
  public boolean isFlippedHorizontally() {
    return this.flippedHorizontally;
  }

  @Override
  public boolean isFlippedVertically() {
    return this.flippedVertically;
  }

  @Override
  public boolean isFlipped() {
    return this.flipped;
  }

  @Override
  public int getGridId() {
    if (this.gid == null) {
      return 0;
    }

    return this.gid;
  }

  @Override
  public Point getTileCoordinate() {
    return this.tileCoordinate;
  }

  /**
   * Sets the tile coordinate.
   *
   * @param tileCoordinate
   *          the new tile coordinate
   */
  public void setTileCoordinate(final Point tileCoordinate) {
    this.tileCoordinate = tileCoordinate;
  }

  @Override
  public int getId() {
    if (this.id == null) {
      return 0;
    }

    return this.id;
  }

  @Override
  public ITerrain[] getTerrain() {
    return this.terrains;
  }

  @Override
  public ITileAnimation getAnimation() {
    return this.animation;
  }

  @Override
  public String toString() {
    return this.getGridId() + " (" + Arrays.toString(this.getTerrainIds()) + ")";
  }

  protected int[] getTerrainIds() {
    int[] terrainIds = new int[] { -1, -1, -1, -1 };
    if (this.terrain == null || this.terrain.isEmpty()) {
      return terrainIds;
    }

    int[] ids = ArrayUtilities.getIntegerArray(this.terrain);
    if (ids.length != 4) {
      return terrainIds;
    } else {
      terrainIds = ids;
    }

    return terrainIds;
  }

  protected void setTerrains(ITerrain[] terrains) {
    this.terrains = terrains;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }

    if (this.id != null && this.id == 0) {
      this.id = null;
    }
  }
}
