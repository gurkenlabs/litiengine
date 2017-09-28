package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

/**
 * The Interface IMapObjectLayer.
 */
public interface IMapObjectLayer extends ILayer {

  /**
   * Gets the shapes.
   *
   * @return the shapes
   */
  public List<IMapObject> getMapObjects();

  public void addMapObject(IMapObject mapObject);

  public void removeMapObject(IMapObject mapObject);

  public Color getColor();

  public void setColor(String color);

  public Collection<IMapObject> getMapObjects(String... type);
}
