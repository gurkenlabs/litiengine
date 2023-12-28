package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.environment.tilemap.*;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@XmlRootElement(name = "tileset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tileset extends CustomPropertyProvider implements ITileset {
  private static final Logger log = Logger.getLogger(Tileset.class.getName());
  public static final String FILE_EXTENSION = "tsx";

  @XmlAttribute
  private int firstgid;

  @XmlElement
  private MapImage image;

  @XmlAttribute
  private Integer margin;

  @XmlAttribute
  private String name;

  @XmlAttribute(name = "class")
  private String tilesetClass;

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

  @XmlAttribute
  private String objectalignment;

  @XmlAttribute
  private String tilerendersize;

  @XmlAttribute
  private String fillmode;

  @XmlElement(name = "tile")
  private List<TilesetEntry> tiles = null;

  @XmlElement(name = "wangset", type = WangSet.class)
  @XmlElementWrapper(name = "wangsets")
  private List<ITerrainSet> wangsets;

  @XmlElement
  private TileTransformations transformations;

  @XmlTransient
  private List<TilesetEntry> allTiles;

  @XmlTransient
  protected Tileset sourceTileset;

  private transient Spritesheet spriteSheet;

  public Tileset() {
    Resources.images().addClearedListener(() -> this.spriteSheet = null);
  }

  public Tileset(Tileset source) {
    this.source = source.getName() + "." + FILE_EXTENSION;
    this.sourceTileset = source;
    this.firstgid = 1;
  }

  @Override
  public Map<String, ICustomProperty> getProperties() {
    return this.sourceTileset != null ? this.sourceTileset.getProperties() : super.getProperties();
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

  @Override
  @XmlTransient
  public Spritesheet getSpritesheet() {
    if (this.spriteSheet == null && this.getImage() != null) {
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
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTile(id);
    }

    if (id < 0 || id >= this.getTileCount()) {
      return null;
    }

    return this.allTiles.get(id);
  }

  public TileTransformations getTransformations() {
    return this.transformations;
  }

  public String getTilesetClass() {
    return this.tilesetClass;
  }

  public String getObjectalignment() {
    return this.objectalignment;
  }

  public String getTilerendersize() {
    return this.tilerendersize;
  }

  public String getFillmode() {
    return this.fillmode;
  }

  @Override
  public boolean containsTile(ITile tile) {
    ITilesetEntry entry = tile.getTilesetEntry();
    return entry == null ? this.containsTile(tile.getGridId()) : this.containsTile(tile.getTilesetEntry());
  }

  @Override
  public boolean containsTile(int tileId) {
    return tileId >= this.firstgid && tileId < this.firstgid + this.getTileCount();
  }

  @Override
  public List<ITerrainSet> getTerrainSets() {
    return this.wangsets;
  }

  @Override
  public boolean containsTile(ITilesetEntry entry) {
    if (entry == null) {
      return false;
    }

    if (this.sourceTileset != null) {
      return this.sourceTileset.containsTile(entry);
    }

    return this.allTiles != null && this.allTiles.contains(entry);
  }

  @Override
  public void finish(URL location) throws TmxException {
    super.finish(location);
    if (this.source != null) {
      // don't reload the source if it's already been loaded in a resource bundle
      if (this.sourceTileset == null) {
        try {
          URL url = new URL(location, this.source);
          this.sourceTileset = Resources.tilesets().get(url);
          if (this.sourceTileset == null) {
            throw new MissingExternalTilesetException(this.source);
          }
        } catch (MalformedURLException e) {
          throw new MissingExternalTilesetException(e);
        }
      }
    } else {
      super.finish(location);
      if (this.image != null) {
        this.image.finish(location);
      }
      if (this.tiles != null) {
        // unsaved tiles don't need any post-processing
        for (TilesetEntry entry : this.tiles) {
          entry.finish(location);
        }
      }
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
    if (this.source == null) {
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

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.source == null) {
      this.allTiles = new ArrayList<>(this.getTileCount());
      if (this.tiles != null) {
        this.allTiles.addAll(this.tiles);
      }
      // add missing entries
      ListIterator<TilesetEntry> iter = this.allTiles.listIterator();
      for (int i = 0; i < this.getTileCount(); i++) {
        if (add(iter)) {
          iter.add(new TilesetEntry(this, iter.nextIndex()));
        }
      }
      if (iter.hasNext()) {
        log.log(Level.WARNING, "tileset \"{0}\" had a tilecount attribute of {1} but had tile IDs going beyond that",
            new Object[] {this.name, this.getTileCount()});
        while (iter.hasNext()) {
          int nextId = iter.next().getId();
          iter.previous();
          while (iter.nextIndex() < nextId) {
            iter.add(new TilesetEntry(this, iter.nextIndex()));
          }
        }
        this.tilecount = this.allTiles.size();
      }
    }
  }

  private static boolean add(ListIterator<TilesetEntry> iter) {
    if (!iter.hasNext()) {
      return true;
    }
    if (iter.next().getId() != iter.previousIndex()) {
      iter.previous(); // move the cursor back
      return true;
    }
    return false;
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    if (this.sourceTileset != null) {
      this.tilewidth = null;
      this.tileheight = null;
      this.tilecount = null;
      this.columns = null;
    } else {
      this.tiles = new ArrayList<>(this.allTiles);
      this.tiles.removeIf(tilesetEntry -> !tilesetEntry.shouldBeSaved());
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
}
