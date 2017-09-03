/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;

// TODO: Auto-generated Javadoc
/**
 * The Class TileLayer.
 */
@XmlRootElement(name = "layer")
public class TileLayer extends Layer implements ITileLayer {

  /** The data. */
  @XmlElementWrapper(name = "data")
  @XmlElement(name = "tile")
  private final List<Tile> data = null;
  private List<ITile> tileList;

  private Tile[][] tiles;

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ILayer#getDimension()
   */
  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.tiled.tmx.ITileLayer#getTileByLoctaion(java.awt.geom.Point2D)
   */
  @Override
  public ITile getTileByLoctaion(final Point2D location) {
    final Optional<ITile> tile = this.getTiles().stream().filter(x -> x.getTileCoordinate().equals(location)).findFirst();
    if (tile == null || !tile.isPresent()) {
      return null;
    }

    return tile.get();
  }

  @Override
  public ITile getTile(int x, int y) {
    this.getTiles();

    if (this.tiles == null || this.tiles.length == 0) {
      return null;
    }

    if (x < 0 || y < 0 || x > this.tiles.length - 1 || y > this.tiles[0].length - 1) {
      return null;
    }

    return this.tiles[x][y];
  }

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ILayer#getTiles()
   */
  @Override
  public List<ITile> getTiles() {
    if (this.tileList != null) {
      return this.tileList;
    }

    this.tileList = new CopyOnWriteArrayList<ITile>();
    if (this.data == null) {
      return this.tileList;
    }

    this.tiles = new Tile[this.getWidth()][this.getHeight()];
    for (int i = 0; i < this.data.size(); i++) {
      final int x = i % this.getWidth();
      final int y = i / this.getWidth();

      final Tile tile = this.data.get(i);
      tile.setTileCoordinate(new Point(x, y));
      this.tileList.add(tile);
      this.tiles[x][y] = tile;
    }

    return this.tileList;
  }

  protected List<Tile> getData() {
    return this.data;
  }
}
