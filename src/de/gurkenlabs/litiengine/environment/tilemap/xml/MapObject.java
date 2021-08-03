package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectText;
import de.gurkenlabs.litiengine.environment.tilemap.IPolyShape;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
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

public class MapObject extends CustomPropertyProvider implements IMapObject {
  @XmlAttribute private int id;

  @XmlAttribute private Integer gid;

  @XmlAttribute private String name;

  @XmlAttribute private String type;

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

  @XmlTransient private ITilesetEntry tile;

  @XmlElement(name = "polyline")
  private PolyShape polyline;

  @XmlElement(name = "polygon")
  private PolyShape polygon;

  @XmlElement private String point;

  @XmlElement private String ellipse;

  @XmlElement private Text text;

  private transient MapObjectLayer layer;

  /** Instantiates a new {@code MapObject} instance. */
  public MapObject() {}

  /**
   * Instantiates a new {@code MapObject} instance.
   *
   * @param type The type of this map object.
   */
  public MapObject(String type) {
    this.type = type;
  }

  /**
   * Instantiates a new {@code MapObject} instance by copying the specified original instance.
   *
   * <p>This variant of the constructor will assign an entirely new ID to the newly created
   * MapObject.
   *
   * @param original the MapObject we want to copy
   */
  public MapObject(MapObject original) {
    super(original);
    this.setName(original.getName());
    this.setId(Game.world().environment().getNextMapId());
    this.polyline =
        (original.getPolyline() != null && !original.getPolyline().getPoints().isEmpty())
            ? new PolyShape(original.getPolyline())
            : null;
    this.polygon =
        (original.getPolygon() != null && !original.getPolygon().getPoints().isEmpty())
            ? new PolyShape(original.getPolygon())
            : null;
    this.setType(original.getType());
    this.setX(original.getX());
    this.setY(original.getY());
    this.setWidth(original.getWidth());
    this.setHeight(original.getHeight());
    this.setLayer(original.layer);
    this.text = original.text;
    this.ellipse = original.ellipse;
    this.point = original.point;
  }

  /**
   * Instantiates a new {@code MapObject} instance by copying the specified original instance.
   *
   * <p>This variant of the constructor lets you decide if the copy instance will get the same ID as
   * the old MapObject or get a new ID.
   *
   * @param original the MapObject we want to copy
   * @param keepID decide if the new instance will adopt the old MapObject's ID or get a new, unique
   *     one.
   */
  public MapObject(MapObject original, boolean keepID) {
    this(original);
    if (keepID) {
      this.setId(original.getId());
    }
  }

  /**
   * Instantiates a new {@code MapObject} instance by copying the specified original instance.
   *
   * @param original the MapObject we want to copy
   * @param id The id of this instance.
   */
  public MapObject(MapObject original, int id) {
    this(original);
    this.setId(id);
  }

  public static Rectangle2D getBounds(IMapObject... objects) {
    return getBounds(Arrays.asList(objects));
  }

  public static Rectangle2D getBounds(Iterable<IMapObject> objects) {
    double x = Double.MAX_VALUE;
    double y = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (IMapObject object : objects) {
      final Rectangle2D bounds = object.getBoundingBox();
      x = Math.min(bounds.getX(), x);
      y = Math.min(bounds.getY(), y);
      maxX = Math.max(bounds.getMaxX(), maxX);
      maxY = Math.max(bounds.getMaxY(), maxY);
    }

    return new Rectangle2D.Double(x, y, maxX - x, maxY - y);
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
  public IPolyShape getPolyline() {
    return this.polyline;
  }

  @Override
  public IPolyShape getPolygon() {
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
    this.height = Math.max(height, 0);
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
    this.width = Math.max(width, 0);
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

    return this.x == null ? 0 : this.x;
  }

  @Override
  public float getY() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getLayer().getMap();
      return this.y - map.getChunkOffsetY() * map.getTileHeight();
    }

    return this.y == null ? 0 : this.y;
  }

  @Override
  @XmlTransient
  public void setPolyline(IPolyShape polyline) {
    this.polyline = (PolyShape) polyline;
  }

  @Override
  @XmlTransient
  public void setPolygon(IPolyShape polygon) {
    this.polygon = (PolyShape) polygon;
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
    // MapObjects don't necessarily have to be children of a layer. E.g. they can also be children
    // of a Blueprint.
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
    return this.getLayer() != null
        && this.getLayer().getMap() != null
        && this.getLayer().getMap().isInfinite()
        && this.getLayer().getMap() instanceof TmxMap;
  }
}
