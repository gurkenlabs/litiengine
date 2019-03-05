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
import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@XmlRootElement(name = "tileset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tileset extends CustomPropertyProvider implements ITileset {
  public static final String FILE_EXTENSION = "tsx";

  @XmlAttribute
  private int firstgid;

  @XmlElement
  private MapImage image;

  @XmlAttribute
  private Integer margin;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private Integer tilewidth;

  @XmlAttribute
  private Integer tileheight;

  @XmlElement(name = "tileoffset")
  private TileOffset tileoffset;

  @XmlAttribute
  private Integer tilecount;

  @XmlAttribute
  private Integer columns;

  @XmlAttribute
  private Integer spacing;

  @XmlAttribute
  private String source;

  @XmlElementWrapper(name = "terraintypes")
  @XmlElement(name = "terrain")
  private List<Terrain> terrainTypes = null;

  @XmlElement(name = "tile")
  private List<TilesetEntry> tiles = null;

  @XmlTransient
  protected Tileset sourceTileset;

  private transient Spritesheet spriteSheet;

  public Tileset() {
    Resources.images().addClearedListener(() -> this.spriteSheet = null);
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
  @Override
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

  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the spacing.
   *
   * @return the spacing
   */
  @Override
  public int getSpacing() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getSpacing();
    }

    if (this.spacing == null) {
      return 0;
    }

    return this.spacing;
  }

  @SuppressWarnings("deprecation")
  @Override
  @XmlTransient
  public Spritesheet getSpritesheet() {
    if (this.spriteSheet == null) {
      this.spriteSheet = Resources.spritesheets().get(this.getImage().getSource());
      if (this.spriteSheet == null) {
        this.spriteSheet = Resources.spritesheets().load(this);
        if (this.spriteSheet == null) {
          return null;
        }
      }
    }

    return this.spriteSheet;
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
      completePath = FileUtilities.combine(path, FileUtilities.getParentDirPath(this.source));
    }

    if (this.sourceTileset != null) {
      this.sourceTileset.setMapPath(completePath);
      return;
    }

    if (this.image == null) {
      return;
    }

    this.image.setAbsolutePath(completePath);
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

    Optional<TilesetEntry> optTile = this.tiles.stream().filter(x -> x.getId() == tileId).findFirst();
    if (!optTile.isPresent()) {
      return terrains;
    }

    TilesetEntry tile = optTile.get();
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

    Optional<TilesetEntry> optTile = this.tiles.stream().filter(x -> x.getId() == tileId).findFirst();
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
  public ITileOffset getTileOffset() {
    return this.sourceTileset != null ? this.sourceTileset.getTileOffset() : this.tileoffset;
  }

  @Override
  public int getTileCount() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTileCount();
    }

    return this.tilecount != null ? this.tilecount : 0;
  }

  @Override
  public ITilesetEntry getTile(int id) {
    if (sourceTileset != null) {
      return sourceTileset.getTile(id);
    }

    if (tiles == null) {
      return null;
    }

    for (TilesetEntry tile : tiles) {
      if (tile.getId() == id) {
        return tile;
      }
    }

    return null;
  }

  @Override
  public boolean containsTile(ITile tile) {
    ITilesetEntry entry = tile.getTilesetEntry();
    return entry == null ? this.containsTile(tile.getGridId()) : this.containsTile(tile.getTilesetEntry());
  }

  @Override
  public boolean containsTile(int tileId) {
    final int lastGridId = this.getFirstGridId() - 1 + this.getTileCount();
    if (this.getFirstGridId() - 1 > tileId) {
      return false;
    }

    return lastGridId >= tileId || this.sourceTileset != null && this.sourceTileset.containsTile(tileId);
  }

  public void loadFromSource(String basePath) throws MissingExternalTilesetException {
    if (this.source == null || this.source.isEmpty()) {
      return;
    }

    String location = FileUtilities.combine(basePath, this.source);
    this.sourceTileset = Resources.tilesets().get(location);
    if (this.sourceTileset == null) {
      throw new MissingExternalTilesetException(location);
    }
  }

  public void saveSource(String basePath) {
    if (this.sourceTileset == null) {
      return;
    }

    XmlUtilities.save(this.sourceTileset, FileUtilities.combine(basePath, this.source), FILE_EXTENSION);
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

  public void updateTileTerrain() {
    if (this.sourceTileset == null) {
      for (TilesetEntry entry : this.tiles) {
        entry.setTerrains(this.getTerrain(entry.getId()));
      }
    }
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.source == null) {
      this.updateTileTerrain();
    }
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    if (this.sourceTileset != null) {
      this.tilewidth = null;
      this.tileheight = null;
      this.tilecount = null;
      this.columns = null;
    }

    if (this.margin != null && this.margin == 0) {
      this.margin = null;
    }

    if (this.spacing != null && this.spacing == 0) {
      this.spacing = null;
    }

    if (this.getProperties() != null && this.getProperties().isEmpty()) {
      this.setProperties(null);
    }
  }

  @Override
  public boolean containsTile(ITilesetEntry entry) {
    if (entry == null) {
      return false;
    }

    if (this.sourceTileset != null) {
      return this.sourceTileset.containsTile(entry);
    }

    return this.tiles != null && this.tiles.contains(entry);
  }
}
