package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;

/**
 * The Class Tileset.
 */
@XmlRootElement(name = "tileset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tileset extends CustomPropertyProvider implements ITileset {
  private static final long serialVersionUID = 1711536300667154031L;

  /** The firstgid. */
  @XmlAttribute
  private int firstgid;

  /** The image. */
  @XmlElement
  private MapImage image;

  /** The margin. */
  @XmlAttribute
  private Integer margin;

  /** The name. */
  @XmlAttribute
  private String name;

  /** The tilewidth. */
  @XmlAttribute
  private int tilewidth;

  /** The tileheight. */
  @XmlAttribute
  private int tileheight;

  @XmlAttribute
  private int tilecount;

  @XmlAttribute
  private int columns;

  /** The spacing. */
  @XmlAttribute
  private Integer spacing;

  @XmlElementWrapper(name = "terraintypes")
  @XmlElement(name = "terrain")
  private final List<Terrain> terrainTypes = null;

  @XmlElement(name = "tile")
  private final List<Tile> tiles = null;

  @Override
  public int getFirstGridId() {
    return this.firstgid;
  }

  @Override
  public IMapImage getImage() {
    return this.image;
  }

  /**
   * Gets the margin.
   *
   * @return the margin
   */
  public int getMargin() {
    if (this.margin == null) {
      return 0;
    }

    return this.margin;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets the spacing.
   *
   * @return the spacing
   */
  public int getSpacing() {
    if (this.spacing == null) {
      return 0;
    }

    return this.spacing;
  }

  @Override
  public Dimension getTileDimension() {
    return new Dimension(this.getTileWidth(), this.getTileHeight());
  }

  /**
   * Gets the tile height.
   *
   * @return the tile height
   */
  @Override
  public int getTileHeight() {
    return this.tileheight;
  }

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  @Override
  public int getTileWidth() {
    return this.tilewidth;
  }

  public void setMapPath(final String path) {
    this.image.setAbsolutPath(path);
  }

  @Override
  public List<ITerrain> getTerrainTypes() {
    List<ITerrain> types = new ArrayList<>();
    if (this.terrainTypes == null) {
      return types;
    }

    for (int i = 0; i < this.terrainTypes.size(); i++) {
      types.add(i, this.terrainTypes.get(i));
    }

    return types;
  }

  @Override
  public ITerrain[] getTerrain(int tileId) {
    ITerrain[] terrains = new ITerrain[4];
    if (this.tiles == null) {
      return terrains;
    }

    Optional<Tile> optTile = this.tiles.stream().filter(x -> x.getId() == tileId).findFirst();
    if (!optTile.isPresent()) {
      return terrains;
    }

    Tile tile = optTile.get();
    int[] tileTerrains = tile.getTerrainIds();
    for (int i = 0; i < 4; i++) {
      if (tileTerrains[i] < 0 || tileTerrains[i] >= this.getTerrainTypes().size()) {
        continue;
      }

      ITerrain terrain = this.getTerrainTypes().get(tileTerrains[i]);
      if (terrain == null) {
        continue;
      }

      terrains[i] = terrain;
    }

    return terrains;
  }

  @Override
  public ITileAnimation getAnimation(int tileId) {
    if (this.tiles == null) {
      return null;
    }

    Optional<Tile> optTile = this.tiles.stream().filter(x -> x.getId() == tileId).findFirst();
    if (!optTile.isPresent()) {
      return null;
    }

    return optTile.get().getAnimation();
  }

  @Override
  public int getColumns() {
    return this.columns;
  }

  @Override
  public int getTilecount() {
    return this.tilecount;
  }

  @Override
  public boolean containsTile(ITile tile) {
    return this.containsTile(tile.getGridId());
  }

  @Override
  public boolean containsTile(int tileId) {
    final int lastGridId = this.getFirstGridId() - 1 + this.getTilecount();
    if (this.getFirstGridId() - 1 > tileId) {
      return false;
    }

    return lastGridId >= tileId;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.margin != null && this.margin == 0) {
      this.margin = null;
    }

    if (this.spacing != null && this.spacing == 0) {
      this.spacing = null;
    }
  }
}
