/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    final Point2D focus = this.getFocus();
    return new Rectangle2D.Double(focus.getX() - this.getCenterX(), focus.getY() - this.getCenterY(), Game.getScreenManager().getResolution().getWidth() / Game.getInfo().renderScale(), Game.getScreenManager().getResolution().getHeight() / Game.getInfo().renderScale());
  }

  @Override
  public Point2D getViewPortLocation(final IEntity entity) {
    if (entity == null) {
      return null;
    }

    // always render the local player at the same location otherwise the
    // localplayer camera causes flickering and bouncing of the sprite
    if (entity.equals(this.getLockedEntity()) && entity.getAnimationController() != null && entity.getAnimationController().getCurrentAnimation() != null) {
      final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
      final Point2D location = new Point2D.Double(this.getFocus().getX() - (spriteSheet.getSpriteWidth() - entity.getWidth()) / 2.0, this.getFocus().getY() - (spriteSheet.getSpriteHeight() - entity.getHeight()) / 2.0);
      return this.getViewPortLocation(location);
    }

    return super.getViewPortLocation(entity);
  }

  @Override
  public void updateFocus() {
    final Point2D cameraLocation = this.getLockedEntity().getLocation();
    this.setFocus(this.applyShakeEffect(cameraLocation));
    this.setCenterX(Game.getScreenManager().getResolution().getWidth() / 2.0 / Game.getInfo().renderScale() - this.getLockedEntity().getWidth() / 2.0);
    this.setCenterY(Game.getScreenManager().getResolution().getHeight() / 2.0 / Game.getInfo().renderScale() - this.getLockedEntity().getHeight() / 2.0);
  }
}
