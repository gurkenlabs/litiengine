
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

/**
 * The Class Map.
 */
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class Map extends CustomPropertyProvider implements IMap, Serializable, Comparable<Map> {
  public static final String FILE_EXTENSION = "tmx";
  private static final long serialVersionUID = 402776584608365440L;

  /** The version. */
  @XmlAttribute
  private double version;

  @XmlAttribute
  private String tiledversion;

  /** The orientation. */
  @XmlAttribute
  private String orientation;

  /** The renderorder. */
  @XmlAttribute
  private String renderorder;

  /** The width. */
  @XmlAttribute
  private int width;

  /** The height. */
  @XmlAttribute
  private int height;

  /** The tilewidth. */
  @XmlAttribute
  private int tilewidth;

  /** The tileheight. */
  @XmlAttribute
  private int tileheight;

  /** The next object id. */
  @XmlAttribute
  private int nextObjectId;

  /** The tilesets. */
  @XmlElement(name = "tileset")
  private List<Tileset> tilesets;

  /** The imagelayers. */
  @XmlElement(name = "imagelayer")
  private List<ImageLayer> imagelayers;

  /** The layers. */
  @XmlElement(name = "layer")
  private List<TileLayer> layers;

  /** The name. */
  @XmlAttribute(required = false)
  private String name;

  /** The objectgroups. */
  @XmlElement(name = "objectgroup")
  private List<MapObjectLayer> objectgroups;

  @XmlTransient
  private String path;

  @Override
  public List<IImageLayer> getImageLayers() {
    final List<IImageLayer> imageLayers = new CopyOnWriteArrayList<>();
    if (this.imagelayers != null) {
      imageLayers.addAll(this.imagelayers);
    }
    return imageLayers;
  }

  @Override
  public String getFileName() {
    return this.name;
  }

  /**
   * Gets the next object id.
   *
   * @return the next object id
   */
  public int getNextObjectId() {
    return this.nextObjectId;
  }

  /**
   * Gets the objectgroups.
   *
   * @return the objectgroups
   */
  public List<MapObjectLayer> getObjectgroups() {
    if (this.objectgroups == null) {
      this.objectgroups = new ArrayList<>();
    }

    return this.objectgroups;
  }

  @Override
  public MapOrientation getOrientation() {
    return MapOrientation.valueOf(this.orientation.toUpperCase());
  }

  @Override
  @XmlTransient
  public String getPath() {
    return this.path;
  }

  @Override
  public String getRenderorder() {
    return this.renderorder;
  }

  @Override
  public List<IMapObjectLayer> getMapObjectLayers() {
    final List<IMapObjectLayer> shapeLayers = new CopyOnWriteArrayList<>();
    if (this.getObjectgroups() != null) {
      shapeLayers.addAll(this.getObjectgroups());
    }
    return shapeLayers;
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
    final List<ITileLayer> lay = new ArrayList<>();
    if (this.layers != null) {
      lay.addAll(this.layers);
    }
    return lay;
  }

  @Override
  public List<ITileset> getTilesets() {
    final List<ITileset> tileSets = new ArrayList<>();
    if (this.tilesets != null) {
      tileSets.addAll(this.tilesets);
    }
    return tileSets;
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
  public String getName() {
    return this.name;
  }

  @Override
  public void setFileName(final String name) {
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
    if (this.imagelayers != null && !this.imagelayers.isEmpty()) {
      for (final ImageLayer imgLayer : this.imagelayers) {
        if (imgLayer == null) {
          continue;
        }

        imgLayer.setMapPath(path);
      }
    }

    if (this.tilesets != null && !this.tilesets.isEmpty()) {
      for (final Tileset tileSet : this.tilesets) {
        if (tileSet == null) {
          continue;
        }

        tileSet.setMapPath(FileUtilities.getParentDirPath(path));
      }
    }
  }

  public void updateTileTerrain() {
    for (TileLayer layer : this.layers) {
      for (Tile tile : layer.getData()) {
        tile.setTerrains(MapUtilities.getTerrain(this, tile.getGridId()));
      }
    }
  }

  @Override
  public void addMapObjectLayer(IMapObjectLayer layer) {
    this.getObjectgroups().add((MapObjectLayer) layer);
  }

  @Override
  public void removeMapObjectLayer(IMapObjectLayer layer) {
    this.getObjectgroups().remove(layer);
  }

  @Override
  public void removeMapObjectLayer(int index) {
    this.getObjectgroups().remove(index);
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
  public void setTileheight(int tileheight) {
    this.tileheight = tileheight;
  }

  @XmlTransient
  public void setTilewidth(int tilewidth) {
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

  public List<Tileset> getRawTileSets() {
    return this.tilesets;
  }

  public List<Tileset> getExternalTilesets() {
    List<Tileset> externalTilesets = new ArrayList<>();
    for (Tileset set : this.getRawTileSets()) {
      if (set.sourceTileset != null) {
        externalTilesets.add(set.sourceTileset);
      }
    }

    return externalTilesets;
  }
}