package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>Entity</code> was moved or changed its size.
 */
public interface EntityTransformListener extends EventListener {

  /**
   * This method is called whenever the location of an <code>IEntity</code> was changed.
   * 
   * @param entity
   *          The entity that changed its location.
   * 
   * @see IEntity#setLocation(java.awt.geom.Point2D)
   * @see IEntity#setLocation(double, double)
   * @see IEntity#setX(double)
   * @see IEntity#setY(double)
   */
  public void locationChanged(IEntity entity);

  /**
   * This method is called whenever the size of an <code>IEntity</code> was changed.
   * 
   * @param entity
   *          The entity that changed its size.
   *          
   * @see IEntity#setSize(double, double)
   * @see IEntity#setHeight(double)
   * @see IEntity#setWidth(double)
   */
  public void sizeChanged(IEntity entity);
}
