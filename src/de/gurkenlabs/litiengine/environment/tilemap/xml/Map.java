/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
import de.gurkenlabs.util.io.XmlUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class Map.
 */
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class Map extends CustomPropertyProvider implements IMap, Comparable<Map> {
  public static final String FILE_EXTENSION = "tmx";

  /** The version. */
  @XmlAttribute
  private double version;

  @XmlAttribute
  private double tiledversion;

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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getImageLayers()
   */
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
      return this.objectgroups = new ArrayList<>();
    }
    return this.objectgroups;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getOrientation()
   */
  @Override
  public MapOrientation getOrientation() {
    return MapOrientation.valueOf(this.orientation);
  }

  @Override
  @XmlTransient
  public String getPath() {
    return this.path;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getRenderorder()
   */
  @Override
  public String getRenderorder() {
    return this.renderorder;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getShapeLayers()
   */
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getSizeInPixles()
   */
  @Override
  public Dimension getSizeInPixels() {
    return new Dimension(this.width * this.tilewidth, this.height * this.tileheight);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getSizeinTiles()
   */
  @Override
  public Dimension getSizeinTiles() {
    return new Dimension(this.width, this.height);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getTileLayers()
   */
  @Override
  public List<ITileLayer> getTileLayers() {
    final List<ITileLayer> lay = new ArrayList<>();
    if (this.layers != null) {
      lay.addAll(this.layers);
    }
    return lay;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getTilesets()
   */
  @Override
  public List<ITileset> getTilesets() {
    final List<ITileset> tileSets = new ArrayList<>();
    if (this.tilesets != null) {
      tileSets.addAll(this.tilesets);
    }
    return tileSets;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getTileSize()
   */
  @Override
  public Dimension getTileSize() {
    return new Dimension(this.tilewidth, this.tileheight);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMap#getVersion()
   */
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
  public List<IMapObject> getMapObjects(String... types) {
    List<IMapObject> mapObjects = new ArrayList<>();
    if (this.getMapObjectLayers() == null || this.getMapObjectLayers().size() == 0) {
      return mapObjects;
    }

    for (IMapObjectLayer layer : this.getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null && mapObject.getType() != null && !mapObject.getType().isEmpty()) {

          if (types.length == 0) {
            mapObjects.add(mapObject);
            continue;
          }

          for (String type : types) {
            if (mapObject.getType().equals(type)) {
              mapObjects.add(mapObject);
            }
          }
        }
      }
    }

    return mapObjects;
  }

  public void setPath(final String path) {
    this.path = path;
    if (this.imagelayers != null && this.imagelayers.size() > 0) {
      for (final ImageLayer imgLayer : this.imagelayers) {
        if (imgLayer == null) {
          continue;
        }

        imgLayer.setMapPath(path);
      }
    }

    if (this.tilesets != null && this.tilesets.size() > 0) {
      for (final Tileset tileSet : this.tilesets) {
        if (tileSet == null) {
          continue;
        }

        tileSet.setMapPath(path);
      }
    }
  }

  public void updateTileTerrain() {
    for (TileLayer layers : this.layers) {
      for (Tile tile : layers.getData()) {
        tile.setTerrains(MapUtilities.getTerrain(this, tile.getGridId()));
      }
    }
  }

  public String save(String fileName) {
    if (!fileName.endsWith("." + FILE_EXTENSION)) {
      fileName += "." + FILE_EXTENSION;
    }

    File newFile = new File(fileName);

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Map.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
      FileOutputStream fileOut = new FileOutputStream(newFile);
      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // first: marshal to byte array
        jaxbMarshaller.marshal(this, out);
        out.flush();

        // second: postprocess xml and then write it to the file
        XmlUtilities.saveWithCustomIndetation(new ByteArrayInputStream(out.toByteArray()), fileOut, 1);
        out.close();

        jaxbMarshaller.marshal(this, out);
      } finally {
        fileOut.flush();
        fileOut.close();
      }
    } catch (JAXBException ex) {
      ex.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return newFile.toString();
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
}