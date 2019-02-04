
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Map extends CustomPropertyProvider implements IMap, Serializable, Comparable<Map> {
  public static final String FILE_EXTENSION = "tmx";

  private static final Logger log = Logger.getLogger(Map.class.getName());
  private static final long serialVersionUID = 402776584608365440L;
  private static final int[] MAX_SUPPORTED_VERSION = { 1, 2, 1 };

  @XmlAttribute
  private double version;

  @XmlAttribute
  private String tiledversion;

  @XmlAttribute
  private String orientation;

  @XmlTransient
  private MapOrientation mapOrientation = MapOrientation.UNDEFINED;

  @XmlAttribute
  private String renderorder;

  @XmlAttribute
  private int width;

  @XmlAttribute
  private int height;

  @XmlAttribute
  private int tilewidth;

  @XmlAttribute
  private int tileheight;

  @XmlAttribute
  private int infinite;

  @XmlAttribute
  private Integer hexsidelength;

  @XmlTransient
  private StaggerAxis staggerAxisEnum = StaggerAxis.UNDEFINED;

  @XmlTransient
  private StaggerIndex staggerIndexEnum = StaggerIndex.UNDEFINED;

  @XmlAttribute
  private String staggeraxis;

  @XmlAttribute
  private String staggerindex;

  @XmlAttribute
  private String backgroundcolor;

  @XmlAttribute(name = "nextobjectid")
  private int nextObjectId;

  @XmlAttribute(name = "nextlayerid")
  private int nextLayerId;

  @XmlAttribute(required = false)
  private String name;

  @XmlElement(name = "tileset")
  private List<Tileset> rawTilesets;

  @XmlElement(name = "imagelayer")
  private List<ImageLayer> rawImageLayers;

  @XmlElement(name = "layer")
  private List<TileLayer> rawTileLayers;

  @XmlElement(name = "objectgroup")
  private List<MapObjectLayer> rawMapObjectLayers;

  @XmlTransient
  private String path;

  private transient List<ITileset> tilesets;
  private transient List<ITileLayer> tileLayers;
  private transient List<IMapObjectLayer> mapObjectLayers;
  private transient List<IImageLayer> imageLayers;
  private transient List<ILayer> allRenderLayers;

  private transient Color decodedBackgroundColor;

  @XmlTransient
  private int chunkOffsetX;

  @XmlTransient
  private int chunkOffsetY;

  @Override
  public List<IImageLayer> getImageLayers() {
    return this.imageLayers;
  }

  /**
   * Gets the next object id.
   *
   * @return the next object id
   */
  @Override
  public int getNextObjectId() {
    return this.nextObjectId;
  }

  @Override
  public int getNextLayerId() {
    return this.nextLayerId;
  }

  @Override
  public MapOrientation getOrientation() {
    if (this.mapOrientation == MapOrientation.UNDEFINED) {
      this.mapOrientation = MapOrientation.valueOf(this.orientation.toUpperCase());
    }
    return this.mapOrientation;
  }

  @Override
  @XmlTransient
  public String getPath() {
    return this.path;
  }

  @Override
  public String getRenderOrder() {
    return this.renderorder;
  }

  @Override
  public List<IMapObjectLayer> getMapObjectLayers() {
    if (this.mapObjectLayers == null) {
      ArrayList<IMapObjectLayer> tmpMapObjectLayers = new ArrayList<>();
      if (this.rawMapObjectLayers != null) {
        tmpMapObjectLayers.addAll(this.rawMapObjectLayers);
      }

      this.mapObjectLayers = Collections.unmodifiableList(tmpMapObjectLayers);
    }

    return this.mapObjectLayers;
  }

  @Override
  public IMapObjectLayer getMapObjectLayer(IMapObject mapObject) {
    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      Optional<IMapObject> found = layer.getMapObjects().stream().filter(x -> x.getId() == mapObject.getId()).findFirst();
      if (found.isPresent()) {
        return layer;
      }
    }

    return null;
  }

  @Override
  public void removeMapObject(int mapId) {
    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      IMapObject remove = null;
      for (IMapObject obj : layer.getMapObjects()) {
        if (obj.getId() == mapId) {
          remove = obj;
          break;
        }
      }

      if (remove != null) {
        layer.removeMapObject(remove);
        break;
      }
    }
  }

  @Override
  public Dimension getSizeInPixels() {
    Dimension mapSizeInPixels = new Dimension(this.width * this.tilewidth, this.height * this.tileheight);
    switch (this.getOrientation()) {
    case HEXAGONAL:
      int maxX = (int) Math.max(this.getTileShape(this.getWidth() - 1, 0).getBounds2D().getMaxX(), this.getTileShape(this.getWidth() - 1, 1).getBounds2D().getMaxX());
      int maxY = (int) Math.max(this.getTileShape(0, this.getHeight() - 1).getBounds2D().getMaxY(), this.getTileShape(1, this.getHeight() - 1).getBounds2D().getMaxY());
      mapSizeInPixels.setSize(maxX, maxY);
      break;
    case ISOMETRIC:
      break;
    case ORTHOGONAL:
      break;
    case SHIFTED:
      break;
    case STAGGERED:
      break;
    case UNDEFINED:
      break;
    }
    return mapSizeInPixels;
  }

  @XmlTransient
  @Override
  public Rectangle2D getBounds() {
    return new Rectangle(this.getSizeInPixels());
  }

  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.width, this.height);
  }

  @Override
  public List<ITileLayer> getTileLayers() {
    return this.tileLayers;
  }

  @Override
  public List<ITileset> getTilesets() {
    return this.tilesets;
  }

  @Override
  public Dimension getTileSize() {
    return new Dimension(this.tilewidth, this.tileheight);
  }

  @Override
  public int getTileWidth() {
    return this.tilewidth;
  }

  @Override
  public int getTileHeight() {
    return this.tileheight;
  }

  public Shape getTileShape(int tileX, int tileY) {
    if (this.getOrientation() == MapOrientation.HEXAGONAL) {
      final StaggerAxis staggerAxis = this.getStaggerAxis();
      final StaggerIndex staggerIndex = this.getStaggerIndex();
      final int s = this.getHexSideLength();
      final int h = staggerAxis == StaggerAxis.X ? this.getTileHeight() : this.getTileWidth();
      final int r = h / 2;
      final int t = staggerAxis == StaggerAxis.X ? (this.getTileWidth() - s) / 2 : (this.getTileHeight() - s) / 2;
      Polygon hex = new Polygon();
      int widthStaggerFactor = 0;
      int heightStaggerFactor = 0;
      if (staggerAxis == StaggerAxis.X) {
        if (MapUtilities.isStaggeredRowOrColumn(staggerIndex, tileX)) {
          heightStaggerFactor = r;
        }
        hex = GeometricUtilities.getHex(widthStaggerFactor + tileX * (t + s), heightStaggerFactor + tileY * h, staggerAxis, s, r, t);
      } else if (staggerAxis == StaggerAxis.Y) {
        if (MapUtilities.isStaggeredRowOrColumn(staggerIndex, tileY)) {
          widthStaggerFactor = r;
        }
        hex = GeometricUtilities.getHex(widthStaggerFactor + tileX * h, heightStaggerFactor + tileY * (t + s), staggerAxis, s, r, t);
      }
      return hex;
    } else {
      return new Rectangle(tileX * this.getTileWidth(), tileY * this.getTileHeight(), this.getTileWidth(), this.getTileHeight());
    }
  }

  @Override
  public double getVersion() {
    return this.version;
  }

  @Override
  public String getTiledVersion() {
    return this.tiledversion;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public Collection<IMapObject> getMapObjects(String... types) {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().isEmpty() || types.length == 0) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      mapObjects.addAll(layer.getMapObjects(types));
    }

    return mapObjects;
  }

  @Override
  public Collection<IMapObject> getMapObjects() {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null) {
          mapObjects.add(mapObject);
        }
      }
    }

    return mapObjects;
  }

  @Override
  public IMapObject getMapObject(int mapId) {
    if (this.getMapObjectLayers() == null) {
      return null;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null && mapObject.getId() == mapId) {
          return mapObject;
        }
      }
    }

    return null;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getHexSideLength() {
    return this.hexsidelength;
  }

  @Override
  public StaggerAxis getStaggerAxis() {
    if (this.staggerAxisEnum == StaggerAxis.UNDEFINED) {
      this.staggerAxisEnum = StaggerAxis.valueOf(this.staggeraxis.toUpperCase());
    }
    return this.staggerAxisEnum;
  }

  @Override
  public StaggerIndex getStaggerIndex() {
    if (this.staggerIndexEnum == StaggerIndex.UNDEFINED) {
      this.staggerIndexEnum = StaggerIndex.valueOf(this.staggerindex.toUpperCase());
    }
    return this.staggerIndexEnum;
  }

  public void setPath(final String path) {
    this.path = path;
    if (this.rawImageLayers != null) {
      for (final ImageLayer imgLayer : this.rawImageLayers) {
        if (imgLayer == null) {
          continue;
        }

        imgLayer.setMapPath(path);
      }
    }

    if (this.rawTilesets != null) {
      for (final Tileset tileSet : this.rawTilesets) {
        if (tileSet == null) {
          continue;
        }

        tileSet.setMapPath(FileUtilities.getParentDirPath(path));
      }
    }
  }

  public void updateTileTerrain() {
    for (Tileset tileset : this.rawTilesets) {
      tileset.updateTileTerrain();
    }
  }

  @Override
  public void addMapObjectLayer(IMapObjectLayer layer) {
    MapObjectLayer rawLayer = (MapObjectLayer) layer;
    this.getRawMapObjectLayers().add(rawLayer);
    rawLayer.setMap(this);
    this.mapObjectLayers = null;
  }

  @Override
  public void addMapObjectLayer(int index, IMapObjectLayer layer) {
    MapObjectLayer rawLayer = (MapObjectLayer) layer;
    this.getRawMapObjectLayers().add(index, rawLayer);
    rawLayer.setMap(this);
    this.mapObjectLayers = null;
  }

  @Override
  public void removeMapObjectLayer(IMapObjectLayer layer) {
    MapObjectLayer rawLayer = (MapObjectLayer) layer;
    rawLayer.setMap(null);
    this.getRawMapObjectLayers().remove(layer);
    this.mapObjectLayers = null;
  }

  @Override
  public void removeMapObjectLayer(int index) {
    MapObjectLayer removed = this.getRawMapObjectLayers().remove(index);
    if (removed != null) {
      removed.setMap(null);
    }

    this.mapObjectLayers = null;
  }

  @XmlTransient
  public void setHeight(int height) {
    this.height = height;
  }

  @XmlTransient
  public void setOrientation(MapOrientation orientation) {
    this.mapOrientation = orientation;
    this.orientation = this.mapOrientation.name().toLowerCase();
  }

  @XmlTransient
  public void setRenderorder(String renderorder) {
    this.renderorder = renderorder;
  }

  @XmlTransient
  public void setTiledVersion(String tiledversion) {
    this.tiledversion = tiledversion;
  }

  @XmlTransient
  public void setTileHeight(int tileheight) {
    this.tileheight = tileheight;
  }

  @XmlTransient
  public void setTileWidth(int tilewidth) {
    this.tilewidth = tilewidth;
  }

  @XmlTransient
  public void setHexSideLength(int hexSideLength) {
    this.hexsidelength = hexSideLength;
  }

  @XmlTransient
  public void setStaggerAxis(String staggerAxis) {
    this.staggeraxis = staggerAxis;
  }

  @XmlTransient
  public void setStaggerIndex(String staggerIndex) {
    this.staggerindex = staggerIndex;
  }

  @XmlTransient
  public void setVersion(double version) {
    this.version = version;
  }

  @XmlTransient
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public int compareTo(Map o) {
    if (this.name == null) {
      return o.name == null ? 0 : -1;
    }

    if (o.name == null) {
      return 1;
    }

    return this.name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (!(anObject instanceof Map)) {
      return false;
    }
    return Objects.equals(this.name, ((Map) anObject).name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }

  @Override
  public List<ILayer> getRenderLayers() {
    return this.allRenderLayers;
  }

  public List<Tileset> getExternalTilesets() {
    List<Tileset> externalTilesets = new ArrayList<>();
    for (Tileset set : this.getRawTilesets()) {
      if (set.sourceTileset != null) {
        externalTilesets.add(set.sourceTileset);
      }
    }

    return externalTilesets;
  }

  public List<Tileset> getRawTilesets() {
    if (this.rawTilesets == null) {
      this.rawTilesets = new ArrayList<>();
    }

    return this.rawTilesets;
  }

  @Override
  public Color getBackgroundColor() {
    if (this.backgroundcolor == null || this.backgroundcolor.isEmpty()) {
      return null;
    }

    if (this.decodedBackgroundColor != null) {
      return this.decodedBackgroundColor;
    }

    this.decodedBackgroundColor = ColorHelper.decode(this.backgroundcolor, true);
    return this.decodedBackgroundColor;
  }

  public List<TileLayer> getRawTileLayers() {
    if (this.rawTileLayers == null) {
      this.rawTileLayers = new ArrayList<>();
    }

    return this.rawTileLayers;
  }

  public boolean isInfinite() {
    return this.infinite == 1;
  }

  protected List<ImageLayer> getRawImageLayers() {
    if (this.rawImageLayers == null) {
      this.rawImageLayers = new ArrayList<>();
    }

    return this.rawImageLayers;
  }

  protected List<MapObjectLayer> getRawMapObjectLayers() {
    if (this.rawMapObjectLayers == null) {
      this.rawMapObjectLayers = new ArrayList<>();
    }

    return this.rawMapObjectLayers;
  }

  protected int getChunkOffsetX() {
    return this.chunkOffsetX;
  }

  protected int getChunkOffsetY() {
    return this.chunkOffsetY;
  }

  void afterUnmarshal(Unmarshaller u, Object parent) {
    this.checkVersion();

    ArrayList<ITileset> tmpSets = new ArrayList<>();
    if (this.rawTilesets != null) {
      tmpSets.addAll(this.rawTilesets);
    }

    ArrayList<ITileLayer> tmpTileLayers = new ArrayList<>();
    if (this.rawTileLayers != null) {
      tmpTileLayers.addAll(this.rawTileLayers);
    }

    ArrayList<IMapObjectLayer> tmpMapObjectLayers = new ArrayList<>();
    if (this.rawMapObjectLayers != null) {
      tmpMapObjectLayers.addAll(this.rawMapObjectLayers);
    }

    ArrayList<IImageLayer> tmpImageLayers = new ArrayList<>();
    if (this.rawImageLayers != null) {
      tmpImageLayers.addAll(this.rawImageLayers);
    }

    ArrayList<ILayer> tmprenderLayers = new ArrayList<>();
    tmprenderLayers.addAll(tmpTileLayers);
    tmprenderLayers.addAll(tmpImageLayers);
    tmprenderLayers.sort(Comparator.comparing(ILayer::getOrder));

    this.tilesets = Collections.unmodifiableList(tmpSets);
    this.tileLayers = Collections.unmodifiableList(tmpTileLayers);
    this.mapObjectLayers = Collections.unmodifiableList(tmpMapObjectLayers);
    this.imageLayers = Collections.unmodifiableList(tmpImageLayers);
    this.allRenderLayers = Collections.unmodifiableList(tmprenderLayers);

    if (this.isInfinite()) {
      this.updateDimensionsByTileLayers();
    }

    if (this.getOrientation() != MapOrientation.HEXAGONAL) {
      this.hexsidelength = null;
    }
  }

  private void checkVersion() {
    String supportedVersionString = ArrayUtilities.join(MAX_SUPPORTED_VERSION, ".");
    String[] ver = this.tiledversion.split("\\.");
    int[] vNumbers = new int[ver.length];
    try {
      for (int i = 0; i < ver.length; i++) {
        vNumbers[i] = Integer.parseInt(ver[i]);
      }
    } catch (NumberFormatException e) {
      log.log(Level.WARNING, "Unsupported Tiled version: {0} (Max. supported version is {1})", new Object[] { this.tiledversion, supportedVersionString });
    }

    for (int i = 0; i < Math.min(vNumbers.length, MAX_SUPPORTED_VERSION.length); i++) {
      if (vNumbers[i] > MAX_SUPPORTED_VERSION[i]) {
        log.log(Level.WARNING, "Unsupported Tiled version: {0} (Max. supported version is {1})", new Object[] { this.tiledversion, supportedVersionString });
        break;
      } else if (vNumbers[i] < MAX_SUPPORTED_VERSION[i]) {
        break;
      }
    }
  }

  /**
   * Update width and height by the max width and height of the tile layers in the infinite map.
   */
  private void updateDimensionsByTileLayers() {

    int minChunkOffsetX = 0;
    int minChunkOffsetY = 0;

    for (TileLayer tileLayer : this.rawTileLayers) {
      if (tileLayer.getRawTileData() != null && tileLayer.getRawTileData().getOffsetX() < minChunkOffsetX) {
        minChunkOffsetX = tileLayer.getRawTileData().getOffsetX();
      }

      if (tileLayer.getRawTileData() != null && tileLayer.getRawTileData().getOffsetY() < minChunkOffsetY) {
        minChunkOffsetY = tileLayer.getRawTileData().getOffsetY();
      }
    }

    // update all tile layer data with the information about the layer based on which they'll position themselves in the grid
    // they need this information because they have to create an appropriately sized grid before locating their chunks in it
    for (TileLayer tileLayer : this.rawTileLayers) {
      if (tileLayer.getRawTileData() != null) {
        tileLayer.getRawTileData().setMinChunkOffsets(minChunkOffsetX, minChunkOffsetY);
      }
    }

    this.chunkOffsetX = minChunkOffsetX;
    this.chunkOffsetY = minChunkOffsetY;

    int w = 0;
    int h = 0;

    for (TileLayer tileLayer : this.rawTileLayers) {
      if (tileLayer.getWidth() > w) {
        w = tileLayer.getWidth();
      }

      if (tileLayer.getHeight() > h) {
        h = tileLayer.getHeight();
      }
    }

    this.width = w;
    this.height = h;
  }
}
