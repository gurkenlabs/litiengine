package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class WangTile {
  private static final int WANG_ID_LENGTH = 8;

  @XmlAttribute
  private int tileid;

  @XmlAttribute
  @XmlJavaTypeAdapter(IntegerArrayAdapter.class)
  private int[] wangid;

  public WangTile() {
    this(0, new int[WANG_ID_LENGTH]);
  }

  public WangTile(int tileId, int[] wangId) {
    this.tileid = tileId;
    this.wangid = normalize(wangId);
  }

  public WangTile(WangTile original) {
    this(original.tileid, original.wangid);
  }

  public int getTileId() {
    return this.tileid;
  }

  public int[] getWangId() {
    this.wangid = normalize(this.wangid);
    return this.wangid.clone();
  }

  public void setWangId(int[] wangId) {
    this.wangid = normalize(wangId);
  }

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
