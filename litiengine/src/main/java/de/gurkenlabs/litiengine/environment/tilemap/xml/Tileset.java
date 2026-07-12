package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code Tileset} class represents a collection of tiles used in a tile-based map. It extends the {@code CustomPropertyProvider} class and
 * implements the {@code ITileset} interface. This class provides various properties and methods to manage and access tileset information.
 */
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

  /**
   * Default constructor for the {@code Tileset} class. Initializes a new instance of the {@code Tileset} class and sets up a listener to clear the
   * sprite sheet when images are cleared.
   */
  public Tileset() {
    Resources.images().addClearedListener(() -> this.spriteSheet = null);
  }

  /**
   * Copy constructor for the {@code Tileset} class. Creates a new instance of the {@code Tileset} class by copying the properties from the provided
   * {@code Tileset} object.
   *
   * @param original The original {@code Tileset} object to copy from.
   */
  public Tileset(Tileset original) {
    super(original);

    this.firstgid = original.getFirstGridId();
    this.image = original.getImage() instanceof MapImage mapImage ? new MapImage(mapImage) : null;
    this.margin = original.getMargin();
    this.name = original.getName();
    this.tilesetClass = original.getTilesetClass();
    this.tilewidth = original.getTileWidth();
    this.tileheight = original.getTileHeight();
    this.tileoffset = original.getTileOffset() != null ? new TileOffset(original.getTileOffset().getX(), original.getTileOffset().getY()) : null;
    this.tilecount = original.getTileCount();
    this.columns = original.getColumns();
    this.spacing = original.getSpacing();
    this.source = original.source;
    this.objectalignment = original.getObjectalignment();
    this.tilerendersize = original.getTilerendersize();
    this.fillmode = original.getFillmode();
    this.tiles = null;
    this.wangsets = original.wangsets != null ? new ArrayList<>(original.wangsets) : null;
    this.transformations = original.getTransformations() != null ? new TileTransformations(original.getTransformations()) : null;
    this.allTiles = new ArrayList<>();
    if (original.allTiles != null) {
      for (TilesetEntry entry : original.allTiles) {
        this.allTiles.add(new TilesetEntry(this, entry));
      }
    }
    this.sourceTileset = null;
    this.spriteSheet = original.getSpritesheet();
  }

  public void copyFrom(Tileset original) {
    if (this.sourceTileset != null) {
      int firstGridId = this.sourceTileset.firstgid;
      String source = this.sourceTileset.source;
      this.sourceTileset.copyFrom(original);
      this.sourceTileset.firstgid = firstGridId;
      this.sourceTileset.source = source;
      return;
    }
    this.firstgid = original.firstgid;
    this.image = original.image != null ? new MapImage(original.image) : null;
    this.margin = original.margin;
    this.name = original.name;
    this.tilesetClass = original.tilesetClass;
    this.tilewidth = original.tilewidth;
    this.tileheight = original.tileheight;
    this.tileoffset = original.tileoffset != null ? new TileOffset(original.tileoffset.getX(), original.tileoffset.getY()) : null;
    this.tilecount = original.tilecount;
    this.columns = original.columns;
    this.spacing = original.spacing;
    this.source = original.source;
    this.objectalignment = original.objectalignment;
    this.tilerendersize = original.tilerendersize;
    this.fillmode = original.fillmode;
    this.transformations = original.transformations != null ? new TileTransformations(original.transformations) : null;
    this.setProperties(copyProperties(original.getProperties()));
    this.allTiles = new ArrayList<>();
    if (original.allTiles != null) {
      for (TilesetEntry entry : original.allTiles) {
        this.allTiles.add(new TilesetEntry(this, entry));
      }
    }
    this.tiles = null;
    this.spriteSheet = original.spriteSheet;
  }

  private static Map<String, ICustomProperty> copyProperties(Map<String, ICustomProperty> properties) {
    Map<String, ICustomProperty> copy = new HashMap<>();
    if (properties != null) {
      properties.forEach((name, property) -> copy.put(name, new CustomProperty(property)));
    }
    return copy;
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

  public void setImage(MapImage image) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setImage(image);
      return;
    }
    this.image = image;
    this.spriteSheet = null;
  }

  public void setMargin(int margin) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setMargin(margin);
      return;
    }
    this.margin = margin;
  }

  public void setSpacing(int spacing) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setSpacing(spacing);
      return;
    }
    this.spacing = spacing;
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
    return this.sourceTileset != null ? this.sourceTileset.getTileHeight() : this.tileheight != null ? this.tileheight : 0;
  }

  /**
   * Gets the tile width.
   *
   * @return the tile width
   */
  @Override
  public int getTileWidth() {
    return this.sourceTileset != null ? this.sourceTileset.getTileWidth() : this.tilewidth != null ? this.tilewidth : 0;
  }

  public void setTileWidth(int tileWidth) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTileWidth(tileWidth);
      return;
    }
    this.tilewidth = tileWidth;
  }

  @Override
  public int getColumns() {
    return this.sourceTileset != null ? this.sourceTileset.getColumns() : this.columns != null ? this.columns : 0;
  }

  public void setTileHeight(int tileHeight) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTileHeight(tileHeight);
      return;
    }
    this.tileheight = tileHeight;
  }

  public void setColumns(int columns) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setColumns(columns);
      return;
    }
    this.columns = columns;
  }

  public void setTileCount(int tileCount) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTileCount(tileCount);
      return;
    }
    this.tilecount = tileCount;
  }

  @Override
  public ITileOffset getTileOffset() {
    return this.sourceTileset != null ? this.sourceTileset.getTileOffset() : this.tileoffset;
  }

  public void setTileOffset(int x, int y) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTileOffset(x, y);
      return;
    }
    if (x == 0 && y == 0) {
      this.tileoffset = null;
      return;
    }
    if (this.tileoffset == null) {
      this.tileoffset = new TileOffset(x, y);
    } else {
      this.tileoffset.setX(x);
      this.tileoffset.setY(y);
    }
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

  /**
   * Gets the tile transformations.
   *
   * @return the tile transformations
   */
  public TileTransformations getTransformations() {
    return this.transformations;
  }

  /**
   * Gets the tileset class.
   *
   * @return the tileset class
   */
  public String getTilesetClass() {
    return this.tilesetClass;
  }

  /**
   * Gets the object alignment.
   *
   * @return the object alignment
   */
  public String getObjectalignment() {
    return this.objectalignment;
  }

  public void setObjectalignment(String objectalignment) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setObjectalignment(objectalignment);
      return;
    }
    this.objectalignment = objectalignment == null || objectalignment.isBlank() ? null : objectalignment;
  }

  /**
   * Gets the tile render size.
   *
   * @return the tile render size
   */
  public String getTilerendersize() {
    return this.tilerendersize;
  }

  public void setTilerendersize(String tilerendersize) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTilerendersize(tilerendersize);
      return;
    }
    this.tilerendersize = tilerendersize == null || tilerendersize.isBlank() ? null : tilerendersize;
  }

  /**
   * Gets the fill mode.
   *
   * @return the fill mode
   */
  public String getFillmode() {
    return this.fillmode;
  }

  public void setFillmode(String fillmode) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setFillmode(fillmode);
      return;
    }
    this.fillmode = fillmode == null || fillmode.isBlank() ? null : fillmode;
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

  /**
   * Saves the source tileset to the specified base path.
   *
   * @param path The base path where the source tileset should be saved.
   */
  public void saveSource(Path path) {
    if (this.sourceTileset == null) {
      return;
    }

    XmlUtilities.save(this.sourceTileset, path.resolve(source), FILE_EXTENSION);
  }

  /**
   * Checks if the tileset is external.
   *
   * @return true if the tileset is external, false otherwise.
   */
  public boolean isExternal() {
    return this.source != null;
  }

  /**
   * Loads the source tileset from the provided list of raw tilesets.
   *
   * @param rawTilesets The list of raw tilesets to load the source tileset from.
   */
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
