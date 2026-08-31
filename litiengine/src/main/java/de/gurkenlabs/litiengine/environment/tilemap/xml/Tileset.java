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
import de.gurkenlabs.litiengine.resources.ResourceLoadException;
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
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/// The `Tileset` class represents a collection of tiles used in a tile-based map. It extends the `CustomPropertyProvider` class and
/// implements the `ITileset` interface. This class provides various properties and methods to manage and access tileset information.
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

  @XmlElement(name = "terrain")
  @XmlElementWrapper(name = "terraintypes")
  private List<LegacyTerrain> terraintypes;

  @XmlElement
  private TileTransformations transformations;

  @XmlTransient
  private List<TilesetEntry> allTiles;

  @XmlTransient
  private boolean legacyTerrainsConverted;

  @XmlTransient
  private WangSet legacyTerrainSnapshot;

  @XmlTransient
  protected Tileset sourceTileset;

  private transient Spritesheet spriteSheet;

  /// Default constructor for the `Tileset` class. Initializes a new instance of the `Tileset` class and sets up a listener to clear the
  /// sprite sheet when images are cleared.
  public Tileset() {
    this.allTiles = new ArrayList<>();
    Resources.images().addClearedListener(() -> this.spriteSheet = null);
  }

  /// Copy constructor for the `Tileset` class. Creates a new instance of the `Tileset` class by copying the properties from the provided
  /// `Tileset` object.
  ///
  /// @param original The original `Tileset` object to copy from.
  public Tileset(Tileset original) {
    super(original.source == null ? original : new CustomPropertyProvider());

    this.firstgid = original.getFirstGridId();
    this.source = original.source;
    if (this.source != null) {
      this.allTiles = new ArrayList<>();
      this.sourceTileset = original.sourceTileset != null ? new Tileset(original.sourceTileset) : null;
      return;
    }

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
    this.objectalignment = original.getObjectalignment();
    this.tilerendersize = original.getTilerendersize();
    this.fillmode = original.getFillmode();
    this.tiles = null;
    this.wangsets = copyTerrainSets(original.getTerrainSets());
    this.legacyTerrainsConverted = original.legacyTerrainsConverted;
    this.legacyTerrainSnapshot = original.legacyTerrainSnapshot != null ? new WangSet(original.legacyTerrainSnapshot) : null;
    this.terraintypes = copyLegacyTerrains(original.sourceTileset != null ? original.sourceTileset.terraintypes : original.terraintypes);
    this.transformations = original.getTransformations() != null ? new TileTransformations(original.getTransformations()) : null;
    this.allTiles = new ArrayList<>();
    for (int tileId = 0; tileId < original.getTileCount(); tileId++) {
      if (original.getTile(tileId) instanceof TilesetEntry entry) {
        this.allTiles.add(new TilesetEntry(this, entry));
      } else {
        this.allTiles.add(new TilesetEntry(this, tileId));
      }
    }
    this.sourceTileset = null;
    this.spriteSheet = original.sourceTileset != null ? original.sourceTileset.spriteSheet : original.spriteSheet;
  }

  /// Creates a map-local copy of a tileset with the supplied first global tile ID.
  ///
  /// @param original the project tileset to copy
  /// @param firstGridId the first global tile ID assigned to the copy
  public Tileset(Tileset original, int firstGridId) {
    this(original);
    this.setFirstGridId(firstGridId);
  }

  public void copyFrom(Tileset original) {
    original = unwrapSource(original);
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
    this.wangsets = copyTerrainSets(original.getTerrainSets());
    this.legacyTerrainsConverted = original.legacyTerrainsConverted;
    this.legacyTerrainSnapshot = original.legacyTerrainSnapshot != null ? new WangSet(original.legacyTerrainSnapshot) : null;
    this.terraintypes = copyLegacyTerrains(original.terraintypes);
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

  private static Tileset unwrapSource(Tileset tileset) {
    return tileset.sourceTileset != null ? tileset.sourceTileset : tileset;
  }

  private static Map<String, ICustomProperty> copyProperties(Map<String, ICustomProperty> properties) {
    Map<String, ICustomProperty> copy = new HashMap<>();
    if (properties != null) {
      properties.forEach((name, property) -> copy.put(name, new CustomProperty(property)));
    }
    return copy;
  }

  private static List<ITerrainSet> copyTerrainSets(List<ITerrainSet> terrainSets) {
    if (terrainSets == null) {
      return null;
    }
    List<ITerrainSet> copy = new ArrayList<>();
    for (ITerrainSet terrainSet : terrainSets) {
      if (terrainSet instanceof WangSet wangSet) {
        copy.add(new WangSet(wangSet));
      }
    }
    return copy;
  }

  private static List<LegacyTerrain> copyLegacyTerrains(List<LegacyTerrain> terrains) {
    return terrains == null ? null : terrains.stream().map(LegacyTerrain::new).toList();
  }

  public void copyTerrainSetsFrom(Tileset original) {
    original = unwrapSource(original);
    if (this.sourceTileset != null) {
      this.sourceTileset.copyTerrainSetsFrom(original);
      return;
    }
    this.wangsets = copyTerrainSets(original.getTerrainSets());
  }

  public void enrichTerrainMetadataFrom(Tileset original) {
    original = unwrapSource(original);
    if (this.sourceTileset != null) {
      this.sourceTileset.enrichTerrainMetadataFrom(original);
      return;
    }
    List<ITerrainSet> targetSets = getTerrainSets();
    List<ITerrainSet> sourceSets = original.getTerrainSets();
    if (targetSets == null || sourceSets == null || targetSets.size() != sourceSets.size()) {
      return;
    }
    for (int setIndex = 0; setIndex < targetSets.size(); setIndex++) {
      if (!(targetSets.get(setIndex) instanceof WangSet target) || !(sourceSets.get(setIndex) instanceof WangSet source)
          || target.getType() != source.getType() || target.getTerrains().size() != source.getTerrains().size()) {
        continue;
      }
      for (int terrainIndex = 0; terrainIndex < target.getTerrains().size(); terrainIndex++) {
        if (!(target.getTerrains().get(terrainIndex) instanceof WangColor targetColor)
            || !(source.getTerrains().get(terrainIndex) instanceof WangColor sourceColor)) {
          continue;
        }
        if (targetColor.getName() == null || targetColor.getName().matches("Terrain \\d+")) {
          targetColor.setName(sourceColor.getName());
          targetColor.setTileId(sourceColor.getTileId());
        }
      }
    }
  }

  private void convertLegacyTerrains() {
    if (this.legacyTerrainsConverted) {
      return;
    }
    WangSet terrainSet = createLegacyTerrainSet();
    if (terrainSet == null) {
      return;
    }
    if (this.wangsets != null && !this.wangsets.isEmpty()) {
      if (this.wangsets.size() == 1 && this.wangsets.getFirst() instanceof WangSet existing && terrainSetsEqual(existing, terrainSet)) {
        this.legacyTerrainsConverted = true;
        this.legacyTerrainSnapshot = new WangSet(existing);
      }
      return;
    }
    this.wangsets = new ArrayList<>(List.of(terrainSet));
    this.legacyTerrainsConverted = true;
    this.legacyTerrainSnapshot = new WangSet(terrainSet);
  }

  private WangSet createLegacyTerrainSet() {
    int terrainCount = this.terraintypes != null && !this.terraintypes.isEmpty() ? this.terraintypes.size() : legacyTerrainCount();
    if (terrainCount == 0) {
      return null;
    }
    WangSet terrainSet = new WangSet("Terrains", de.gurkenlabs.litiengine.environment.tilemap.TerrainType.CORNER);
    Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK};
    for (int index = 0; index < terrainCount; index++) {
      String name = this.terraintypes != null && !this.terraintypes.isEmpty()
        ? this.terraintypes.get(index).getName()
        : "Terrain " + (index + 1);
      WangColor color = new WangColor(name, colors[index % colors.length]);
      if (this.terraintypes != null && !this.terraintypes.isEmpty()) {
        LegacyTerrain legacyTerrain = this.terraintypes.get(index);
        color.setTileId(legacyTerrain.getTileId());
        legacyTerrain.getProperties().forEach((key, value) -> color.setValue(key, new CustomProperty(value)));
      }
      terrainSet.getTerrains().add(color);
    }
    if (this.allTiles != null) {
      int[] wangCorners = {7, 1, 5, 3}; // TMX terrain order is top-left, top-right, bottom-left, bottom-right.
      for (TilesetEntry entry : this.allTiles) {
        String terrain = entry.getLegacyTerrain();
        if (terrain == null) {
          continue;
        }
        String[] corners = terrain.split(",", -1);
        int[] wangIds = new int[8];
        for (int index = 0; index < Math.min(corners.length, wangCorners.length); index++) {
          if (!corners[index].isBlank()) {
            try {
              int terrainIndex = Integer.parseInt(corners[index].trim());
              if (terrainIndex >= 0 && terrainIndex < terrainSet.getTerrains().size()) {
                wangIds[wangCorners[index]] = terrainIndex + 1;
              }
            } catch (NumberFormatException ignored) {
              // Ignore malformed legacy terrain indices rather than failing the entire tileset.
            }
          }
        }
        if (java.util.Arrays.stream(wangIds).anyMatch(id -> id != 0)) {
          terrainSet.getWangTiles().add(new WangTile(entry.getId(), wangIds));
        }
      }
    }
    return terrainSet;
  }

  private static boolean terrainSetsEqual(WangSet first, WangSet second) {
    if (!java.util.Objects.equals(first.getName(), second.getName()) || first.getType() != second.getType()
        || !java.util.Objects.equals(first.getProperties(), second.getProperties())
        || first.getTerrains().size() != second.getTerrains().size() || first.getWangTiles().size() != second.getWangTiles().size()) {
      return false;
    }
    for (int i = 0; i < first.getTerrains().size(); i++) {
      if (!(first.getTerrains().get(i) instanceof WangColor firstColor)
          || !(second.getTerrains().get(i) instanceof WangColor secondColor)
          || !java.util.Objects.equals(firstColor.getName(), secondColor.getName())
          || !java.util.Objects.equals(firstColor.getColor(), secondColor.getColor())
          || firstColor.getTileId() != secondColor.getTileId()
          || Double.compare(firstColor.getProbability(), secondColor.getProbability()) != 0
          || !java.util.Objects.equals(firstColor.getProperties(), secondColor.getProperties())) {
        return false;
      }
    }
    for (int i = 0; i < first.getWangTiles().size(); i++) {
      WangTile firstTile = first.getWangTiles().get(i);
      WangTile secondTile = second.getWangTiles().get(i);
      if (firstTile.getTileId() != secondTile.getTileId() || !java.util.Arrays.equals(firstTile.getWangId(), secondTile.getWangId())) {
        return false;
      }
    }
    return true;
  }

  private void clearEditedLegacyTerrains() {
    if (this.legacyTerrainSnapshot == null || this.wangsets != null && this.wangsets.size() == 1
        && this.wangsets.getFirst() instanceof WangSet current && terrainSetsEqual(current, this.legacyTerrainSnapshot)) {
      return;
    }
    this.terraintypes = null;
    if (this.allTiles != null) {
      this.allTiles.forEach(TilesetEntry::clearLegacyTerrain);
    }
    this.legacyTerrainSnapshot = null;
  }

  private int legacyTerrainCount() {
    int terrainCount = 0;
    if (this.allTiles == null) {
      return terrainCount;
    }
    for (TilesetEntry entry : this.allTiles) {
      String terrain = entry.getLegacyTerrain();
      if (terrain == null) {
        continue;
      }
      for (String corner : terrain.split(",", -1)) {
        try {
          terrainCount = Math.max(terrainCount, Integer.parseInt(corner.trim()) + 1);
        } catch (NumberFormatException ignored) {
          // Empty and malformed legacy terrain values do not define a terrain.
        }
      }
    }
    return terrainCount;
  }

  @Override
  public Map<String, ICustomProperty> getProperties() {
    return this.sourceTileset != null ? this.sourceTileset.getProperties() : super.getProperties();
  }

  @Override
  public int getFirstGridId() {
    return this.firstgid;
  }

  /// Sets the first global tile ID used by this tileset in its containing map.
  ///
  /// @param firstGridId the first global tile ID, starting at `1`
  public void setFirstGridId(int firstGridId) {
    if (firstGridId < 1) {
      throw new IllegalArgumentException("The first grid ID must be positive.");
    }
    this.firstgid = firstGridId;
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
    this.spriteSheet = null;
  }

  public void setSpacing(int spacing) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setSpacing(spacing);
      return;
    }
    this.spacing = spacing;
    this.spriteSheet = null;
  }

  /// Gets the margin.
  ///
  /// @return the margin
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

  public String getSource() {
    return this.source;
  }

  public Tileset getSourceTileset() {
    return this.sourceTileset;
  }

  @Override
  public void setName(String name) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setName(name);
    } else {
      this.name = name;
    }
  }

  public void setSource(String source) {
    this.source = source;
  }

  /// Gets the spacing.
  ///
  /// @return the spacing
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
    if (this.sourceTileset != null) {
      return this.sourceTileset.getSpritesheet();
    }
    if (this.spriteSheet != null && (this.spriteSheet.getSpriteWidth() != this.getTileWidth()
        || this.spriteSheet.getSpriteHeight() != this.getTileHeight())) {
      this.spriteSheet = null;
    }
    if (this.spriteSheet == null && this.getImage() != null) {
      this.spriteSheet = Resources.spritesheets().get(this.getImage().getSource());
      if (this.spriteSheet == null || this.spriteSheet.getSpriteWidth() != this.getTileWidth()
          || this.spriteSheet.getSpriteHeight() != this.getTileHeight()) {
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

  /// Gets the tile height.
  ///
  /// @return the tile height
  @Override
  public int getTileHeight() {
    return this.sourceTileset != null ? this.sourceTileset.getTileHeight() : this.tileheight != null ? this.tileheight : 0;
  }

  /// Gets the tile width.
  ///
  /// @return the tile width
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
    this.spriteSheet = null;
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
    this.spriteSheet = null;
  }

  public void setColumns(int columns) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setColumns(columns);
      return;
    }
    this.columns = columns;
    this.spriteSheet = null;
  }

  public void setTileCount(int tileCount) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTileCount(tileCount);
      return;
    }
    if (tileCount < 0) {
      throw new IllegalArgumentException("Tile count must be non-negative.");
    }
    this.tilecount = tileCount;
    if (this.allTiles == null) {
      this.allTiles = new ArrayList<>(tileCount);
    }
    while (this.allTiles.size() < tileCount) {
      this.allTiles.add(new TilesetEntry(this, this.allTiles.size()));
    }
    if (this.allTiles.size() > tileCount) {
      this.allTiles.subList(tileCount, this.allTiles.size()).clear();
    }
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

    return this.allTiles != null && id < this.allTiles.size() ? this.allTiles.get(id) : null;
  }

  /// Gets the tile transformations.
  ///
  /// @return the tile transformations
  public TileTransformations getTransformations() {
    return this.sourceTileset != null ? this.sourceTileset.getTransformations() : this.transformations;
  }

  /// Gets the tileset class.
  ///
  /// @return the tileset class
  public String getTilesetClass() {
    return this.sourceTileset != null ? this.sourceTileset.getTilesetClass() : this.tilesetClass;
  }

  /// Gets the object alignment.
  ///
  /// @return the object alignment
  public String getObjectalignment() {
    return this.sourceTileset != null ? this.sourceTileset.getObjectalignment() : this.objectalignment;
  }

  public void setObjectalignment(String objectalignment) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setObjectalignment(objectalignment);
      return;
    }
    this.objectalignment = objectalignment == null || objectalignment.isBlank() ? null : objectalignment;
  }

  /// Gets the tile render size.
  ///
  /// @return the tile render size
  public String getTilerendersize() {
    return this.sourceTileset != null ? this.sourceTileset.getTilerendersize() : this.tilerendersize;
  }

  public void setTilerendersize(String tilerendersize) {
    if (this.sourceTileset != null) {
      this.sourceTileset.setTilerendersize(tilerendersize);
      return;
    }
    this.tilerendersize = tilerendersize == null || tilerendersize.isBlank() ? null : tilerendersize;
  }

  /// Gets the fill mode.
  ///
  /// @return the fill mode
  public String getFillmode() {
    return this.sourceTileset != null ? this.sourceTileset.getFillmode() : this.fillmode;
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
    if (this.sourceTileset != null) {
      return this.sourceTileset.getTerrainSets();
    }
    convertLegacyTerrains();
    return this.wangsets;
  }

  public List<ITerrainSet> getOrCreateTerrainSets() {
    if (this.sourceTileset != null) {
      return this.sourceTileset.getOrCreateTerrainSets();
    }
    if (this.wangsets == null) {
      this.wangsets = new ArrayList<>();
    }
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

  /// Saves the source tileset to the specified base path.
  ///
  /// @param path The base path where the source tileset should be saved.
  public void saveSource(Path path) {
    if (this.sourceTileset == null) {
      return;
    }

    XmlUtilities.save(this.sourceTileset, path.resolve(source), FILE_EXTENSION);
  }

  /// Checks if the tileset is external.
  ///
  /// @return true if the tileset is external, false otherwise.
  public boolean isExternal() {
    return this.source != null;
  }

  /// Loads the source tileset from the provided list of raw tilesets.
  ///
  /// @param rawTilesets The list of raw tilesets to load the source tileset from.
  public void load(List<Tileset> rawTilesets) {
    if (this.source == null) {
      return;
    }

    String fileName = FileUtilities.getFileName(this.source);
    for (Tileset set : rawTilesets) {
      if (set.getName() != null && set.getName().equals(fileName)) {
        this.sourceTileset = set;
        return;
      }
      if (set.getSource() != null && FileUtilities.getFileName(set.getSource()).equals(fileName)) {
        this.sourceTileset = set;
        return;
      }
    }
    // Fall back to tilesets that are globally loaded as project resources. External
    // tileset references in a game file may not be registered in the game file's own
    // tileset list, but they are usually available as loaded resources (e.g. under
    // "Resources -> Tilesets"). This keeps wang sets hosted only in the external
    // .tsx available to the map.
    try {
      Tileset resolved = Resources.tilesets().get(fileName);
      if (resolved != null) {
        this.sourceTileset = resolved;
      }
    } catch (final ResourceLoadException e) {
      // tileset is not available as a loaded resource; leave the reference unresolved
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
      convertLegacyTerrains();
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
      clearEditedLegacyTerrains();
      if (this.allTiles == null) {
        this.allTiles = new ArrayList<>();
      }
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
