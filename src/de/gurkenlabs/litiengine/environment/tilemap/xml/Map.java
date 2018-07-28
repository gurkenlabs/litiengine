
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Map extends CustomPropertyProvider implements IMap, Serializable, Comparable<Map> {
  public static final String FILE_EXTENSION = "tmx";
  private static final long serialVersionUID = 402776584608365440L;
  private static final int[] MAX_SUPPORTED_VERSION = {1, 1, 5}; // 1.1.5

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
  private String backgroundcolor;

  @XmlAttribute(name = "nextobjectid")
  private int nextObjectId;

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
    return new Dimension(this.width * this.tilewidth, this.height * this.tileheight);
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
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().isEmpty()) {
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
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().isEmpty()) {
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

  public void setPath(final String path) {
    this.path = path;
    if (this.rawImageLayers != null && !this.rawImageLayers.isEmpty()) {
      for (final ImageLayer imgLayer : this.rawImageLayers) {
        if (imgLayer == null) {
          continue;
        }

        imgLayer.setMapPath(path);
      }
    }

    if (this.rawTilesets != null && !this.rawTilesets.isEmpty()) {
      for (final Tileset tileSet : this.rawTilesets) {
        if (tileSet == null) {
          continue;
        }

        tileSet.setMapPath(FileUtilities.getParentDirPath(path));
      }
    }
  }

  public void updateTileTerrain() {
    for (TileLayer layer : this.rawTileLayers) {
      for (Tile tile : layer.getData()) {
        tile.setTerrains(MapUtilities.getTerrain(this, tile.getGridId()));
      }
    }
  }

  @Override
  public void addMapObjectLayer(IMapObjectLayer layer) {
    this.getRawMapObjectLayers().add((MapObjectLayer) layer);
    this.mapObjectLayers = null;
  }

  @Override
  public void removeMapObjectLayer(IMapObjectLayer layer) {
    this.getRawMapObjectLayers().remove(layer);
    this.mapObjectLayers = null;
  }

  @Override
  public void removeMapObjectLayer(int index) {
    this.getRawMapObjectLayers().remove(index);
    this.mapObjectLayers = null;
  }

  @XmlTransient
  public void setHeight(int height) {
    this.height = height;
  }

  @XmlTransient
  public void setOrientation(String orientation) {
    this.orientation = orientation;
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
  public void setVersion(double version) {
    this.version = version;
  }

  @XmlTransient
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public int compareTo(Map o) {
    return this.name.compareTo(o.name);
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

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    String[] ver = this.tiledversion.split("\\.");
    int[] vNumbers = new int[ver.length];
    try {
      for (int i = 0; i < ver.length; i++) {
        vNumbers[i] = Integer.parseInt(ver[i]);
      }
    } catch (NumberFormatException e) {
      throw new UnsupportedOperationException("unsupported Tiled version: " + tiledversion, e);
    }
    for (int i = 0; i < Math.min(vNumbers.length, MAX_SUPPORTED_VERSION.length); i++) {
      if (vNumbers[i] > MAX_SUPPORTED_VERSION[i]) {
        throw new UnsupportedOperationException("unsupported Tiled version: " + tiledversion);
      } else if (vNumbers[i] < MAX_SUPPORTED_VERSION[i]) {
        break;
      }
    }
    
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
      this.rawMapObjectLayers.forEach(layer -> {
        layer.setWidth(this.width);
        layer.setHeight(this.height);
      });
      
      tmpMapObjectLayers.addAll(this.rawMapObjectLayers);
    }

    ArrayList<IImageLayer> tmpImageLayers = new ArrayList<>();
    if (this.rawImageLayers != null) {
      this.rawImageLayers.forEach(layer -> {
        layer.setWidth(this.width);
        layer.setHeight(this.height);
      });
      
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

  }
}