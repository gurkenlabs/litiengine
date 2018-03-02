package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/**
 * The Class Tileset.
 */
@XmlRootElement(name = "tileset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tileset extends CustomPropertyProvider implements ITileset {
  public static final String FILE_EXTENSION = "tsx";
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
  private Integer tilewidth;

  /** The tileheight. */
  @XmlAttribute
  private Integer tileheight;

  @XmlAttribute
  private Integer tilecount;

  @XmlAttribute
  private Integer columns;

  /** The spacing. */
  @XmlAttribute
  private Integer spacing;

  @XmlAttribute
  private String source;

  @XmlElementWrapper(name = "terraintypes")
  @XmlElement(name = "terrain")
  private List<Terrain> terrainTypes = null;

  @XmlElement(name = "tile")
  private List<Tile> tiles = null;

  @XmlTransient
  protected Tileset sourceTileset;

  @Override
  public int compareTo(ITileset obj) {
    if (obj == null || obj.getName() == null) {
      return 1;
    }

    if (this.getName() == null) {
      if (obj.getName() == null) {
        return 0;
      }

      return -1;
    }

    return this.getName().compareTo(obj.getName());
  }

  @Override
  public int getFirstGridId() {
    return this.firstgid;
  }

  @Override
  public IMapImage getImage() {
    return this.sourceTileset != null ? this.sourceTileset.getImage() : this.image;
  }

  /**
   * Gets the margin.
   *
   * @return the margin
   */
  public int getMargin() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getMargin();
    }

    if (this.margin == null) {
      return 0;
    }

    return this.margin;
  }

  @Override
  public String getName() {
    return this.sourceTileset != null ? this.sourceTileset.getName() : this.name;
  }

  /**
   * Gets the spacing.
   *
   * @return the spacing
   */
  public int getSpacing() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getSpacing();
    }

    if (this.spacing == null) {
      return 0;
    }

    return this.spacing;
  }

  @Override
  public Dimension getTileDimension() {
    return this.sourceTileset != null ? this.sourceTileset.getTileDimension() : new Dimension(this.getTileWidth(), this.getTileHeight());
  }

  /**
   * Gets the tile height.
   *
   * @return the tile height
   */
  @Override
  public int getTileHeight() {
    return this.sourceTileset != null ? this.sourceTileset.getTileHeight() : this.tileheight;
  }

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  @Override
  public int getTileWidth() {
    return this.sourceTileset != null ? this.sourceTileset.getTileWidth() : this.tilewidth;
  }

  public void setMapPath(final String path) {
    String completePath = path;
    if (this.source != null) {
      completePath = FileUtilities.combinePaths(path, FileUtilities.getParentDirPath(this.source));
    }

    if (this.sourceTileset != null) {
      this.sourceTileset.setMapPath(completePath);
      return;
    }

    if (this.image == null) {
      return;
    }

    this.image.setAbsolutPath(completePath);
  }

  @Override
  public List<ITerrain> getTerrainTypes() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTerrainTypes();
    }

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
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTerrain(tileId);
    }

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
    if (this.sourceTileset != null) {
      return this.sourceTileset.getAnimation(tileId);
    }

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
    return this.sourceTileset != null ? this.sourceTileset.getColumns() : this.columns;
  }

  @Override
  public int getTilecount() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTilecount();
    }

    return this.tilecount != null ? this.tilecount : 0;
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

    return lastGridId >= tileId || this.sourceTileset != null && this.sourceTileset.containsTile(tileId);
  }

  public void loadFromSource(String basePath) {
    if (this.source == null || this.source.isEmpty()) {
      return;
    }

    this.sourceTileset = XmlUtilities.readFromFile(Tileset.class, basePath + "\\" + this.source);
  }

  public void saveSource(String basePath) {
    if (this.sourceTileset == null) {
      return;
    }

    XmlUtilities.save(this.sourceTileset, basePath + "\\" + this.source, FILE_EXTENSION);
  }

  public boolean isExternal() {
    return this.source != null;
  }

  public void load(List<Tileset> rawTilesets) {
    if (this.source == null || this.source.isEmpty()) {
      return;
    }

    for (Tileset set : rawTilesets) {
      String fileName = FileUtilities.getFileName(this.source);
      if (set.getName() != null && set.getName().equals(fileName)) {
        this.sourceTileset = set;
        break;
      }
    }
  }

  @Override
  void beforeMarshal(Marshaller m) {
    if (this.sourceTileset != null) {
      this.tilewidth = null;
      this.tileheight = null;
      this.tilecount = null;
      this.columns = null;
    }

    if (this.getAllCustomProperties() != null && this.getAllCustomProperties().isEmpty()) {
      this.setCustomProperties(null);
    }
  }

  @SuppressWarnings("unused")
  void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.margin != null && this.margin == 0) {
      this.margin = null;
    }

    if (this.spacing != null && this.spacing == 0) {
      this.spacing = null;
    }
  }
}
