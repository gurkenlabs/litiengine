/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

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

    // always render the local player at the same location otherwise the
    // localplayer camera causes flickering and bouncing of the sprite
    final IAnimationController animationController = Game.getEntityControllerManager().getAnimationController(entity);
    if (entity.equals(this.getLockedEntity()) && animationController != null && animationController.getCurrentAnimation() != null && animationController.getCurrentAnimation().getSpritesheet() != null) {
      final Spritesheet spriteSheet = animationController.getCurrentAnimation().getSpritesheet();
      final Point2D location = new Point2D.Double(this.getFocus().getX() - entity.getWidth() / 2 - (spriteSheet.getSpriteWidth() - entity.getWidth()) * 0.5, this.getFocus().getY() - entity.getHeight() / 2 - (spriteSheet.getSpriteHeight() - entity.getHeight()) * 0.5);
      return this.getViewPortLocation(location);
    }

    return super.getViewPortLocation(entity);
  }

  @Override
  public void updateFocus() {
    final Point2D cameraLocation = this.getLockedCameraLocation();

    this.setFocus(new Point2D.Double(cameraLocation.getX(), cameraLocation.getY()));
    super.updateFocus();
  }

  protected Point2D getLockedCameraLocation() {
    return this.getLockedEntity().getDimensionCenter();
  }
}
