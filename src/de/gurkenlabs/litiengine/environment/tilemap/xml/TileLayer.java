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
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;

public class TileLayer extends Layer implements ITileLayer {

  @XmlElement
  private TileData data = null;

  private transient List<ITile> tileList;

  private transient Tile[][] tiles;

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

    if (x < 0 || y < 0 || x > this.tiles.length - 1 || y > this.tiles[0].length - 1) {
      return null;
    }

    return this.tiles[x][y];
  }

  @Override
  public List<ITile> getTiles() {
    if (this.tileList != null) {
      return this.tileList;
    }

    try {
      this.tileList = new CopyOnWriteArrayList<>(this.getData());
      if (this.data == null) {
        return this.tileList;
      }

      this.tiles = new Tile[this.getWidth()][this.getHeight()];
      for (int i = 0; i < this.getData().size(); i++) {
        final int x = i % this.getWidth();
        final int y = i / this.getWidth();
  
        final Tile tile = this.getData().get(i);
        tile.setTileCoordinate(new Point(x, y));
        this.tileList.add(tile);
        this.tiles[x][y] = tile;
      }

      return this.tileList;
    } catch (InvalidTileLayerException e) {
      throw new TmxError(e);
    }
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

  protected List<Tile> getData() throws InvalidTileLayerException {
    return this.data.parseTiles();
  }

  protected TileData getRawTileData() {
    return this.data;
  }

  @Override
  void finish(URL location) throws TmxException {
    for (Tile tile : getData()) {
      for (ITileset tileset : this.getMap().getTilesets()) {
        if (tileset.containsTile(tile.getGridId())) {
          tile.setTilesetEntry(tileset.getTile(tile.getGridId() - tileset.getFirstGridId()));
          break;
        }
      }
    }
  }
}
