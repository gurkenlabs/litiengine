/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Class LocalPlayerCamera.
 */
public class PositionLockCamera extends Camera {
  private final IEntity entity;

  public PositionLockCamera(final IEntity entity) {
    super();
    this.entity = entity;
    this.updateFocus();
  }

  public IEntity getLockedEntity() {
    return this.entity;
  }

  @Override
  public Point2D getViewPortLocation(final IEntity entity) {
    if (entity == null) {
      return null;
    }



    return super.getViewPortLocation(entity);
  }

  @Override
  public void updateFocus() {
    final Point2D cameraLocation = this.getLockedEntity().getDimensionCenter();

    // TODO: clamp camera so that it doesn't display black space on map edges
    this.setFocus(new Point2D.Double(cameraLocation.getX(), cameraLocation.getY()));
    super.updateFocus();
  }
}
