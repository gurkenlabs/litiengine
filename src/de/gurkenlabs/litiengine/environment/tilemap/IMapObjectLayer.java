package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

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

  public String getColorHexString();

  public void setColor(String colorHexString);

  public Collection<IMapObject> getMapObjects(String... type);

  public Collection<IMapObject> getMapObjects(int... mapIDs);
}
