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
  List<IMapObject> getMapObjects();

  void addMapObject(IMapObject mapObject);

  default void addMapObject(int index, IMapObject mapObject) {
    addMapObject(mapObject);
    List<IMapObject> mapObjects = getMapObjects();
    mapObjects.remove(mapObject);
    mapObjects.add(index, mapObject);
  }

  void removeMapObject(IMapObject mapObject);

  Color getColor();

  void setColor(Color color);

  Collection<IMapObject> getMapObjects(String... type);

  Collection<IMapObject> getMapObjects(int... mapIDs);
}
