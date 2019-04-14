package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Arrays;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IPolygon;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectText;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;

/**
 * The Class MapObject.
 */
public class MapObject extends CustomPropertyProvider implements IMapObject {
  @XmlAttribute
  private int id;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private String type;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float x;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float y;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float width = 0f;

  @XmlAttribute
  @XmlJavaTypeAdapter(value = DecimalFloatAdapter.class)
  private Float height = 0f;

  @XmlAttribute
  private Integer gid;

  @XmlTransient
  private ITilesetEntry tile;

  @XmlElement
  private Polyline polyline;

  @XmlElement
  private Polygon polygon;

  @XmlElement
  private String point;

  @XmlElement
  private String ellipse;

  @XmlElement
  private Text text;

  private transient MapObjectLayer layer;

  public MapObject() {
  }

  /**
   * Copy Constructor for copying instances of MapObjects.
   * This variant of the constructor will assign an entirely new ID to the newly created MapObject.
   *
   * @param mapObjectToBeCopied
   *          the MapObject we want to copy
   */
  public MapObject(MapObject mapObjectToBeCopied) {
    super(mapObjectToBeCopied);
    this.setName(mapObjectToBeCopied.getName());
    this.setId(Game.world().environment().getNextMapId());
    this.polyline = (mapObjectToBeCopied.getPolyline() != null && !mapObjectToBeCopied.getPolyline().getPoints().isEmpty()) ? new Polyline(mapObjectToBeCopied.getPolyline()) : null;
    this.setType(mapObjectToBeCopied.getType());
    this.setX(mapObjectToBeCopied.getX());
    this.setY(mapObjectToBeCopied.getY());
    this.setWidth(mapObjectToBeCopied.getWidth());
    this.setHeight(mapObjectToBeCopied.getHeight());
  }

  /**
   * Copy Constructor for copying instances of MapObjects.
   * This variant of the constructor lets you decide if the copy instance will get the same ID as the old MapObject or get a new ID.
   * 
   * @param mapObjectToBeCopied
   *          the MapObject we want to copy
   * @param keepID
   *          decide if the new instance will adopt the old MapObject's ID or get a new, unique one.
   */
  public MapObject(MapObject mapObjectToBeCopied, boolean keepID) {
    this(mapObjectToBeCopied);
    if (keepID) {
      this.setId(mapObjectToBeCopied.getId());
    }
  }

  public static Rectangle2D getBounds2D(IMapObject... objects) {
    return getBounds(objects);
  }

  public static Rectangle2D getBounds2D(Iterable<IMapObject> objects) {
    return getBounds(objects);
  }

  public static Rectangle2D getBounds(IMapObject... objects) {
    return getBounds(Arrays.asList(objects));
  }

  public static Rectangle2D getBounds(Iterable<IMapObject> objects) {
    float minX = -1;
    float minY = -1;
    float maxX = -1;
    float maxY = -1;
    for (IMapObject item : objects) {
      if (minX == -1 || item.getX() < minX) {
        minX = item.getX();
      }

      if (minY == -1 || item.getY() < minY) {
        minY = item.getY();
      }

      if (maxX == -1 || item.getBoundingBox().getMaxX() > maxX) {
        maxX = (int) item.getBoundingBox().getMaxX();
      }

      if (maxY == -1 || item.getBoundingBox().getMaxY() > maxY) {
        maxY = (int) item.getBoundingBox().getMaxY();
      }
    }

    return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
  }

  @Override
  public int getGridId() {
    if (this.gid == null) {
      return 0;
    }

    return this.gid;
  }

  @Override
  public ITilesetEntry getTile() {
    return this.tile;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.getX(), this.getY(), this.width, this.height);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public Point2D getLocation() {
    return new Point2D.Double(this.getX(), this.getY());
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public Polyline getPolyline() {
    return this.polyline;
  }

  @Override
  public IPolygon getPolygon() {
    return this.polygon;
  }

  @Override
  public Ellipse2D getEllipse() {
    if (!this.isEllipse()) {
      return null;
    }

    return new Ellipse2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
  }

  @Override
  public IMapObjectText getText() {
    return this.text;
  }

  @Override
  @XmlTransient
  public void setGridId(int gid) {
    this.gid = gid;
  }

  @Override
  @XmlTransient
  public void setHeight(float height) {
    this.height = height;
  }

  @Override
  @XmlTransient
  public void setId(int id) {
    this.id = id;
  }

  @Override
  @XmlTransient
  public void setName(String name) {
    if (name != null && name.isEmpty()) {
      this.name = null;
      return;
    }

    this.name = name;
  }

  @Override
  @XmlTransient
  public void setType(String type) {
    this.type = type;
  }

  @Override
  @XmlTransient
  public void setWidth(float width) {
    this.width = width;
  }

  @Override
  @XmlTransient
  public void setX(float x) {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getLayer().getMap();
      this.x = x + map.getChunkOffsetX() * map.getTileWidth();
      return;
    }

    this.x = x;
  }

  @Override
  @XmlTransient
  public void setY(float y) {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getLayer().getMap();
      this.y = y + map.getChunkOffsetY() * map.getTileHeight();
      return;
    }

    this.y = y;
  }

  @Override
  public void setLocation(Point2D location) {
    if (location == null) {
      return;
    }

    this.setLocation((float) location.getX(), (float) location.getY());
  }

  @Override
  public void setLocation(float x, float y) {
    this.setX(x);
    this.setY(y);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("#" + this.getId() + ": ");
    sb.append(this.getName() == null ? "" : "\'" + this.getName() + "\' ");
    sb.append("" + this.getType());
    sb.append("; x: " + this.getX());
    sb.append("; y: " + this.getY());
    sb.append("; width: " + this.getWidth());
    sb.append("; height: " + this.getHeight());
    return sb.toString();
  }

  @Override
  public float getX() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getLayer().getMap();
      return this.x - map.getChunkOffsetX() * map.getTileWidth();
    }

    return this.x;
  }

  @Override
  public float getY() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getLayer().getMap();
      return this.y - map.getChunkOffsetY() * map.getTileHeight();
    }

    return this.y;
  }

  @XmlTransient
  public void setPolyline(Polyline polyline) {
    this.polyline = polyline;
  }

  @Override
  public float getWidth() {
    return this.width;
  }

  @Override
  public float getHeight() {
    return this.height;
  }

  @Override
  public IMapObjectLayer getLayer() {
    return this.layer;
  }

  @Override
  public boolean isPolyline() {
    return this.polyline != null;
  }

  @Override
  public boolean isPolygon() {
    return this.polygon != null;
  }

  @Override
  public boolean isPoint() {
    return this.point != null;
  }

  @Override
  public boolean isEllipse() {
    return this.ellipse != null;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    // MapObjects don't necessarily have to be children of a layer. E.g. they can also be children of a Blueprint.
    if (parent instanceof MapObjectLayer) {
      this.setLayer((MapObjectLayer) parent);
    }

    if (this.gid != null && this.gid == 0) {
      this.gid = null;
    }

    if (this.name != null && this.name.isEmpty()) {
      this.name = null;
    }

    if (this.polyline != null && this.polyline.getPoints().isEmpty()) {
      this.polyline = null;
    }
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    if (this.gid != null) {
      this.tile = this.getLayer().getMap().getTilesetEntry(this.gid);
    }
  }

  protected void setLayer(MapObjectLayer layer) {
    this.layer = layer;
  }

  private boolean isInfiniteMap() {
    return this.getLayer() != null && this.getLayer().getMap() != null && this.getLayer().getMap().isInfinite() && this.getLayer().getMap() instanceof TmxMap;
  }
}
