/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.List;

// TODO: Auto-generated Javadoc
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
}
