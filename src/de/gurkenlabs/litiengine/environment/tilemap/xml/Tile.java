package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;

@XmlRootElement(name = "tile")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tile extends CustomPropertyProvider implements ITile, Serializable {
  public static final int NONE = 0;
  public static final Tile EMPTY = new Tile(NONE);
  protected static final int FLIPPED_HORIZONTALLY_FLAG = 0x80000000;
  protected static final int FLIPPED_VERTICALLY_FLAG = 0x40000000;
  protected static final int FLIPPED_DIAGONALLY_FLAG = 0x20000000;

  private static final long serialVersionUID = -7597673646108642906L;

  @XmlAttribute
  private Integer gid;

  private transient Point tileCoordinate;

  private transient ITilesetEntry tilesetEntry;

  private transient boolean flippedDiagonally;
  private transient boolean flippedHorizontally;
  private transient boolean flippedVertically;
  private transient boolean flipped;

  public Tile() {
  }

  public Tile(int gidBitmask) {
    // Clear the flags
    this.flippedDiagonally = (gidBitmask & FLIPPED_DIAGONALLY_FLAG) != 0;
    this.flippedHorizontally = (gidBitmask & FLIPPED_HORIZONTALLY_FLAG) != 0;
    this.flippedVertically = (gidBitmask & FLIPPED_VERTICALLY_FLAG) != 0;

    this.flipped = this.isFlippedDiagonally() || this.isFlippedHorizontally() || this.isFlippedVertically();
    this.gid = gidBitmask & ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG);
  }

  @Override
  public boolean hasCustomProperty(String name) {
    return tilesetEntry == null ? super.hasCustomProperty(name) : tilesetEntry.hasCustomProperty(name);
  }

  @Override
  public List<Property> getProperties() {
    return tilesetEntry == null ? super.getProperties() : tilesetEntry.getProperties();
  }

  @Override
  public void setProperties(List<Property> props) {
    if (tilesetEntry == null) {
      super.setProperties(props);
    } else {
      tilesetEntry.setProperties(props);
    }
  }

  @Override
  public String getStringValue(String name, String defaultValue) {
    return tilesetEntry == null ? super.getStringValue(name, defaultValue) : tilesetEntry.getStringValue(name, defaultValue);
  }

  @Override
  public void setValue(String name, String value) {
    if (tilesetEntry == null) {
      super.setValue(name, value);
    } else {
      tilesetEntry.setValue(name, value);
    }
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
  public String toString() {
    return this.getGridId() + String.valueOf(this.getTilesetEntry());
  }

  protected void setTilesetEntry(ITilesetEntry entry) {
    this.tilesetEntry = entry;
  }

  @Override
  public ITilesetEntry getTilesetEntry() {
    return this.tilesetEntry;
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }
  }
}
