package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.XmlElement;

import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;

public class TileLayer extends Layer implements ITileLayer {

  @XmlElement
  private TileData data = null;

  private transient List<ITile> tileList;

  private transient Tile[][] tiles;

  /**
   * Instantiates a new <code>TileLayer</code> instance.
   */
  public TileLayer() {
    // keep for serialization
  }

  /**
   * Instantiates a new <code>TileLayer</code> instance with the specified data.
   *
   * @param data
   *          The tile data of this instance.
   */
  public TileLayer(TileData data) {
    this.data = data;
  }

  @Override
  public ITile getTileByLocation(final Point2D location) {
    final Optional<ITile> tile = this.getTiles().stream().filter(x -> x.getTileCoordinate().equals(location)).findFirst();
    return tile.isPresent() ? tile.get() : null;
  }

  @Override
  public ITile getTile(int x, int y) {
    this.getTiles();

    if (this.tiles == null || this.tiles.length == 0) {
      return null;
    }

    if (x < 0 || y < 0 || y >= this.tiles.length || x >= this.tiles[y].length) {
      return null;
    }

    return this.tiles[y][x];
  }

  @Override
  public void setTile(int x, int y, ITile tile) {
    this.setTile(x, y, tile.getGridId());
  }

  @Override
  public void setTile(int x, int y, int gid) {
    if (this.getRawTileData() == null) {
      return;
    }

    Tile tile = this.getRawTileData().getTiles().get(x + y * this.getWidth());
    if (tile == null) {
      return;
    }

    tile.setGridId(gid);

    if (this.getMap() != null) {
      ITilesetEntry entry = this.getMap().getTilesetEntry(gid);
      if (entry != null) {
        tile.setTilesetEntry(entry);
      }
    }
  }

  @Override
  public List<ITile> getTiles() {
    return this.tileList;
  }

  @Override
  public int getWidth() {
    if (this.data != null && this.data.isInfinite()) {
      return this.data.getWidth();
    }

    return super.getWidth();
  }

  @Override
  public int getHeight() {
    if (this.data != null && this.data.isInfinite()) {
      return this.data.getHeight();
    }

    return super.getHeight();
  }

  protected List<Tile> getData() {
    return this.data.getTiles();
  }

  protected TileData getRawTileData() {
    return this.data;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    this.tileList = new CopyOnWriteArrayList<>(this.getData());
    this.tiles = new Tile[this.getHeight()][this.getWidth()];
    for (int i = 0; i < this.getData().size(); i++) {
      final int x = i % this.getWidth();
      final int y = i / this.getWidth();

      final Tile tile = this.getData().get(i);
      tile.setTileCoordinate(new Point(x, y));
      this.tileList.add(tile);
      this.tiles[y][x] = tile;
    }
    for (Tile tile : getData()) {
      tile.setTilesetEntry(this.getMap().getTilesetEntry(tile.getGridId()));
    }
  }
}
