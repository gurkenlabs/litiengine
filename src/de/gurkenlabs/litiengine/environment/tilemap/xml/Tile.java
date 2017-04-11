/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Point;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;

// TODO: Auto-generated Javadoc
/**
 * The Class Tile.
 */
@XmlRootElement(name = "tile")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tile extends CustomPropertyProvider implements ITile {

  /** The gid. */
  @XmlAttribute
  private int gid;

  @XmlAttribute
  private int id;

  @XmlAttribute
  private String terrain;

  @XmlElement(required = false)
  private Animation animation;

  /** The tile coordinate. */
  @XmlTransient
  private Point tileCoordinate;

  @XmlTransient
  private ITerrain[] terrains;

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ITile#GetGridId()
   */
  @Override
  public int getGridId() {
    return this.gid;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.ITile#getTileCoordinate()
   */
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
    return this.id;
  }

  @Override
  public ITerrain[] getTerrain() {
    return this.terrains;
  }
  
  public ITileAnimation getAnimation() {
    return this.animation;
  }

  protected int[] getTerrainIds() {
    int[] terrains = new int[] { -1, -1, -1, -1 };
    if (this.terrain == null || this.terrain.isEmpty()) {
      return terrains;
    }

    String[] split = this.terrain.split(",");
    if (split.length != 4) {
      return terrains;
    }

    for (int i = 0; i < split.length; i++) {
      try {
        terrains[i] = Integer.parseInt(split[i]);
      } catch (NumberFormatException nfe) {
        continue;
      }
    }

    return terrains;
  }

  protected void setTerrains(ITerrain[] terrains) {
    this.terrains = terrains;
  }
}
