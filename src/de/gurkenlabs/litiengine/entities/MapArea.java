package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.NONE)
@TmxType(MapObjectType.AREA)
public class MapArea extends Entity {

  /**
   * Instantiates a new <code>MapArea</code> entity.
   */
  public MapArea() {
  }

  /**
   * Instantiates a new <code>MapArea</code> entity.
   *
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   */
  public MapArea(final double x, final double y, final double width, final double height) {
    this(0, null, x, y, width, height);
  }

  /**
   * Instantiates a new <code>MapArea</code> entity.
   *
   * @param id
   *          The id of this instance.
   * @param name
   *          The name of this instance.
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   */
  public MapArea(final int id, final String name, final double x, final double y, final double width, final double height) {
    super(id, name);
    this.setLocation(x, y);
    this.setWidth(width);
    this.setHeight(height);
  }
}
