package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import jakarta.xml.bind.annotation.XmlElement;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TileLayer extends Layer implements ITileLayer {

  @XmlElement
  private TileData data = null;

  private transient List<ITile> tileList;


  /**
   * Instantiates a new {@code TileLayer} instance.
   */
  public TileLayer() {
    // keep for serialization
  }

  /**
   * Instantiates a new {@code TileLayer} instance with the specified data.
   *
   * @param data The tile data of this instance.
   */
  public TileLayer(TileData data) {
    this.data = data;
  }

  @Override
  public ITile getTileByLocation(final Point2D location) {
    return getTiles().stream().filter(x -> x.getTileCoordinate().equals(location)).findFirst().orElse(null);
  }

  @Override
  public ITile getTile(int x, int y) {
    return getTiles().stream().filter(t -> t.getTileCoordinate().x == x && t.getTileCoordinate().y == y).findFirst().orElse(null);
  }

  @Override
  public void setTile(int x, int y, ITile tile) {
    this.setTile(x, y, tile.getGridId());
  }

  @Override
  public void setTile(int x, int y, int gid) {
    if (getRawTileData() == null) {
      return;
    }

    Tile tile = getRawTileData().getTiles().get(x + y * getWidth());
    if (tile == null) {
      return;
    }

    tile.setGridId(gid);
    if (getMap() != null) {
      ITilesetEntry entry = getMap().getTilesetEntry(gid);
      if (entry != null) {
        tile.setTilesetEntry(entry);
      }
    }
  }

  @Override
  public List<ITile> getTiles() {
    return tileList;
  }

  @Override
  public int getWidth() {
    if (data != null && data.isInfinite()) {
      return data.getWidth();
    }

    return super.getWidth();
  }

  @Override
  public int getHeight() {
    if (data != null && data.isInfinite()) {
      return data.getHeight();
    }

    return super.getHeight();
  }

  protected List<Tile> getData() {
    return data.getTiles();
  }

  protected TileData getRawTileData() {
    return data;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    this.tileList = new CopyOnWriteArrayList<>(getData());
    for (int i = 0; i < getData().size(); i++) {
      final int x = i % getWidth();
      final int y = i / getWidth();

      final Tile tile = getData().get(i);
      tile.setTileCoordinate(new Point(x, y));
      tile.setTilesetEntry(getMap().getTilesetEntry(tile.getGridId()));
    }
  }
}
