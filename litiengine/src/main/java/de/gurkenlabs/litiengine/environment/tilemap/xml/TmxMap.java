package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.RenderOrder;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public final class TmxMap extends CustomPropertyProvider implements IMap {

  public static final String FILE_EXTENSION = "tmx";
  public static final int MAX_MAJOR = 1;
  public static final int MAX_MINOR = 10;

  public static final int MIN_MAJOR = 1;

  public static final int MIN_MINOR = 5;

  private static final Logger log = Logger.getLogger(TmxMap.class.getName());
  private final transient List<ITileLayer> rawTileLayers = new CopyOnWriteArrayList<>();
  private final transient List<IMapObjectLayer> rawMapObjectLayers = new CopyOnWriteArrayList<>();
  private final transient List<IImageLayer> rawImageLayers = new CopyOnWriteArrayList<>();
  private final transient List<IGroupLayer> rawGroupLayers = new CopyOnWriteArrayList<>();
  private final transient List<ITileLayer> tileLayers = Collections.unmodifiableList(
      this.rawTileLayers);
  private final transient List<IMapObjectLayer> mapObjectLayers = Collections.unmodifiableList(
      this.rawMapObjectLayers);
  private final transient List<IImageLayer> imageLayers = Collections.unmodifiableList(
      this.rawImageLayers);
  private final transient List<IGroupLayer> groupLayers = Collections.unmodifiableList(
      this.rawGroupLayers);
  @XmlAttribute
  private double version;
  @XmlAttribute
  private String tiledversion;

  @XmlAttribute(name = "class")
  private String mapClass;

  @XmlAttribute
  private String orientation;
  @XmlTransient
  private IMapOrientation mapOrientation;
  @XmlAttribute
  private RenderOrder renderorder;
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
  @XmlAttribute
  private StaggerAxis staggeraxis;
  @XmlAttribute
  private StaggerIndex staggerindex;
  @XmlAttribute
  private double parallaxoriginx;
  @XmlAttribute
  private double parallaxoriginy;
  @XmlAttribute
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color backgroundcolor;
  @XmlAttribute(name = "nextlayerid")
  private Integer nextLayerId;
  @XmlAttribute(name = "nextobjectid")
  private Integer nextObjectId;
  @XmlAttribute
  private String name;
  @XmlElement(name = "tileset", type = Tileset.class)
  private List<ITileset> tilesets;
  @XmlElements({
      @XmlElement(name = "imagelayer", type = ImageLayer.class),
      @XmlElement(name = "layer", type = TileLayer.class),
      @XmlElement(name = "objectgroup", type = MapObjectLayer.class),
      @XmlElement(name = "group", type = GroupLayer.class)
  })
  private List<ILayer> layers;
  @XmlTransient
  private URL path;
  @XmlTransient
  private int chunkOffsetX;
  @XmlTransient
  private int chunkOffsetY;

  public TmxMap() {
    // keep for serialization
  }

  public TmxMap(IMapOrientation orientation) {
    this.mapOrientation = orientation;
    this.renderorder = RenderOrder.RIGHT_DOWN;
    this.setTiledVersion(MAX_MAJOR + "." + MAX_MINOR + ".0");
  }

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
  public IMapOrientation getOrientation() {
    if (this.mapOrientation == null) {
      this.mapOrientation = MapOrientations.forName(this.orientation);
    }
    return this.mapOrientation;
  }

  @XmlTransient
  public void setOrientation(IMapOrientation orientation) {
    this.mapOrientation = Objects.requireNonNull(orientation);
  }

  @Override
  @XmlTransient
  public URL getPath() {
    return this.path;
  }

  public void setPath(final URL path) {
    this.path = path;
  }

  @Override
  public RenderOrder getRenderOrder() {
    return this.renderorder;
  }

  @XmlTransient
  public void setRenderOrder(RenderOrder renderorder) {
    this.renderorder = renderorder;
  }

  @Override
  public List<IMapObjectLayer> getMapObjectLayers() {
    return this.mapObjectLayers;
  }

  @Override
  public Dimension getSizeInPixels() {
    return this.getOrientation().getSize(this);
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
    if (this.tilesets == null) {
      this.tilesets = new CopyOnWriteArrayList<>();
    }

    return this.tilesets;
  }

  @Override
  public ITilesetEntry getTilesetEntry(int gid) {
    for (ITileset tileset : this.getTilesets()) {
      if (tileset.containsTile(gid)) {
        return tileset.getTile(gid - tileset.getFirstGridId());
      }
    }
    return null;
  }

  @Override
  public Dimension getTileSize() {
    return new Dimension(this.tilewidth, this.tileheight);
  }

  @Override
  public Point2D getParallaxOrigin() {
    return new Point2D.Double(this.parallaxoriginx, this.parallaxoriginy);
  }

  @Override
  public int getTileWidth() {
    return this.tilewidth;
  }

  @XmlTransient
  public void setTileWidth(int tilewidth) {
    this.tilewidth = tilewidth;
  }

  @Override
  public int getTileHeight() {
    return this.tileheight;
  }

  @XmlTransient
  public void setTileHeight(int tileheight) {
    this.tileheight = tileheight;
  }

  @Override
  public double getVersion() {
    return this.version;
  }

  @XmlTransient
  public void setVersion(double version) {
    this.version = version;
  }

  @Override
  public String getTiledVersion() {
    return this.tiledversion;
  }

  @XmlTransient
  public void setTiledVersion(String tiledversion) {
    this.tiledversion = tiledversion;
  }

  @Override
  public List<IGroupLayer> getGroupLayers() {
    return this.groupLayers;
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
  public int getWidth() {
    return this.width;
  }

  @XmlTransient
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @XmlTransient
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public int getHexSideLength() {
    return this.hexsidelength;
  }

  @XmlTransient
  public void setHexSideLength(int hexSideLength) {
    this.hexsidelength = hexSideLength;
  }

  @Override
  public StaggerAxis getStaggerAxis() {
    return this.staggeraxis;
  }

  @XmlTransient
  public void setStaggerAxis(StaggerAxis staggerAxis) {
    this.staggeraxis = staggerAxis;
  }

  @Override
  public StaggerIndex getStaggerIndex() {
    return this.staggerindex;
  }

  @XmlTransient
  public void setStaggerIndex(StaggerIndex staggerIndex) {
    this.staggerindex = staggerIndex;
  }

  @Override
  public void finish(URL location) throws TmxException {
    super.finish(location);
    if (this.name == null) {
      this.name = FileUtilities.getFileName(location);
    }
    this.path = location;
    // tilesets must be post-processed before layers; otherwise external tilesets may not be loaded
    for (ITileset tileset : this.tilesets) {
      if (tileset instanceof Tileset tilesetImpl) {
        tilesetImpl.finish(location);
      }
    }
    for (ILayer layer : this.layers) {
      if (layer instanceof Layer layerImpl) {
        layerImpl.finish(location);
      }
    }
  }

  @Override
  public void addLayer(ILayer layer) {
    this.getRenderLayers().add(layer);
    this.layerAdded(layer);
    if (layer instanceof Layer layerImpl) {
      layerImpl.setMap(this);
    }
  }

  @Override
  public void addLayer(int index, ILayer layer) {
    this.getRenderLayers().add(index, layer);
    this.addRawLayer(index, layer);
    if (layer instanceof Layer layerImpl) {
      layerImpl.setMap(this);
    }
  }

  @Override
  public void removeLayer(ILayer layer) {
    this.layers.remove(layer);
    this.removeRawLayer(layer);
    if (layer instanceof Layer layerImpl) {
      layerImpl.setMap(null);
    }
  }

  @Override
  public void removeLayer(int index) {
    ILayer removed = this.layers.remove(index);
    this.removeRawLayer(removed);
    if (removed instanceof Layer layerImpl) {
      layerImpl.setMap(null);
    }
  }

  private void removeRawLayer(ILayer layer) {
    if (layer instanceof ITileLayer) {
      this.rawTileLayers.remove(layer);
    }
    if (layer instanceof IMapObjectLayer) {
      this.rawMapObjectLayers.remove(layer);
    }
    if (layer instanceof IImageLayer) {
      this.rawImageLayers.remove(layer);
    }
    if (layer instanceof IGroupLayer) {
      this.rawGroupLayers.remove(layer);
    }
  }

  @Override
  public String toString() {
    return (this.getName() == null || this.getName().isEmpty()) ? super.toString() : this.getName();
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (!(anObject instanceof TmxMap)) {
      return false;
    }
    return Objects.equals(this.name, ((TmxMap) anObject).name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }

  @Override
  public List<ILayer> getRenderLayers() {
    if (this.layers == null) {
      this.layers = new CopyOnWriteArrayList<>();
    }

    return this.layers;
  }

  public List<Tileset> getExternalTilesets() {
    List<Tileset> externalTilesets = new ArrayList<>();
    for (ITileset set : this.getTilesets()) {
      if (set instanceof Tileset tilesetImpl && tilesetImpl.sourceTileset != null) {
        externalTilesets.add(tilesetImpl.sourceTileset);
      }
    }

    return externalTilesets;
  }

  @Override
  public Color getBackgroundColor() {
    return this.backgroundcolor;
  }

  @Override
  public boolean isInfinite() {
    return this.infinite == 1;
  }

  int getChunkOffsetX() {
    return this.chunkOffsetX;
  }

  int getChunkOffsetY() {
    return this.chunkOffsetY;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) throws TmxException {
    this.checkVersion();

    if (this.orientation != null) {
      this.mapOrientation = MapOrientations.forName(this.orientation);
    }

    if (this.mapOrientation == null) {
      this.mapOrientation = MapOrientations.ORTHOGONAL;
    }

    if (this.tilesets == null) {
      this.tilesets = new CopyOnWriteArrayList<>();
    }

    if (this.layers == null) {
      this.layers = new CopyOnWriteArrayList<>();
    }

    for (ILayer layer : this.layers) {
      this.layerAdded(layer);
    }

    if (this.isInfinite()) {
      this.updateDimensionsByTileLayers();
    }
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
    this.orientation = this.mapOrientation.getName();
  }

  private void layerAdded(ILayer layer) {
    if (layer instanceof ITileLayer layerImpl) {
      this.rawTileLayers.add(layerImpl);
    }
    if (layer instanceof IMapObjectLayer layerImpl) {
      this.rawMapObjectLayers.add(layerImpl);
    }
    if (layer instanceof IImageLayer layerImpl) {
      this.rawImageLayers.add(layerImpl);
    }
    if (layer instanceof IGroupLayer layerImpl) {
      this.rawGroupLayers.add(layerImpl);
    }
  }

  private void addRawLayer(int index, ILayer layer) {
    if (layer instanceof ITileLayer layerImpl) {
      this.rawTileLayers.add(this.getRawIndex(index, ITileLayer.class), layerImpl);
    }
    if (layer instanceof IMapObjectLayer layerImpl) {
      this.rawMapObjectLayers.add(this.getRawIndex(index, IMapObjectLayer.class), layerImpl);
    }
    if (layer instanceof IImageLayer layerImpl) {
      this.rawImageLayers.add(this.getRawIndex(index, IImageLayer.class), layerImpl);
    }
    if (layer instanceof IGroupLayer layerImpl) {
      this.rawGroupLayers.add(this.getRawIndex(index, IGroupLayer.class), layerImpl);
    }
  }

  private <T extends ILayer> int getRawIndex(int index, Class<T> layerType) {
    int rawIndex = 0;
    for (int i = 0; i < this.layers.size(); i++) {
      if (i >= index) {
        break;
      }

      if (layerType.isAssignableFrom(this.layers.get(i).getClass())) {
        rawIndex++;
      }

    }

    return rawIndex;
  }

  private void checkVersion() throws UnsupportedMapVersionException {
    if (this.tiledversion == null || this.tiledversion.isEmpty()) {
      log.log(Level.WARNING,
          "Tiled version not defined for map \"{0}\". Could not evaluate whether the map format is supported by the engine.",
          new Object[] {this.getName()});
      return;
    }

    String[] ver = this.tiledversion.split("\\.", 3);

    int major;
    int minor;
    try {
      major = Integer.parseInt(ver[0]);
      minor = Integer.parseInt(ver[1]);
      // we don't need to care about the patch version
    } catch (NumberFormatException e) {
      throw new UnsupportedMapVersionException(this.tiledversion);
    }

    if (major < MIN_MAJOR || major > MAX_MAJOR) {
      // incompatible API changes
      throw new UnsupportedMapVersionException(this.tiledversion);
    }

    if (minor < MIN_MINOR) {
      log.log(Level.WARNING,
        "Tiled version {0} of map \"{1}\" is less than the supported version {2}.{3}.x. Some features may not work.",
        new Object[] {this.tiledversion, this.getName(), MIN_MAJOR, MIN_MINOR});
    }

    if (minor > MAX_MINOR) {
      log.log(Level.WARNING,
          "Tiled version {0} of map \"{1}\" is greater than the supported version {2}.{3}.x. Some features may not work.",
          new Object[] {this.tiledversion, this.getName(), MAX_MAJOR, MAX_MINOR});
    }
  }

  /**
   * Update width and height by the max width and height of the tile layers in the infinite map.
   */
  private void updateDimensionsByTileLayers() {

    int minChunkOffsetX = 0;
    int minChunkOffsetY = 0;

    int w = 0;
    int h = 0;

    for (ITileLayer tileLayer : this.tileLayers) {
      if (!(tileLayer instanceof TileLayer layer)) {
        continue;
      }

      if (layer.getRawTileData() != null && layer.getRawTileData().getOffsetX() < minChunkOffsetX) {
        minChunkOffsetX = layer.getRawTileData().getOffsetX();
      }

      if (layer.getRawTileData() != null && layer.getRawTileData().getOffsetY() < minChunkOffsetY) {
        minChunkOffsetY = layer.getRawTileData().getOffsetY();
      }
    }

    // update all tile layer data with the information about the layer based on which they'll position
    // themselves in the grid
    // they need this information because they have to create an appropriately sized grid before
    // locating their chunks in it
    for (ITileLayer tileLayer : this.tileLayers) {
      if (!(tileLayer instanceof TileLayer layer)) {
        continue;
      }

      if (layer.getRawTileData() != null) {
        layer.getRawTileData().setMinChunkOffsets(minChunkOffsetX, minChunkOffsetY);
      }

      if (tileLayer.getWidth() > w) {
        w = tileLayer.getWidth();
      }

      if (tileLayer.getHeight() > h) {
        h = tileLayer.getHeight();
      }
    }

    this.chunkOffsetX = minChunkOffsetX;
    this.chunkOffsetY = minChunkOffsetY;
    this.width = w;
    this.height = h;

  }
}
