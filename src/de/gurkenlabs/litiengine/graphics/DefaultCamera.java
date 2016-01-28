/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;

/**
 * The Class DefaultCamera.
 */
public class DefaultCamera extends Camera {

  private final IScreenManager screenManager;

  public DefaultCamera(final IScreenManager screenManager) {
    this.screenManager = screenManager;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCenterX()
   */
  @Override
  public double getCenterX() {
    return this.screenManager.getResolution().width / 2.0;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCenterY()
   */
  @Override
  public double getCenterY() {
    return this.screenManager.getResolution().height / 2.0;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    return new Rectangle2D.Double(this.getFocus().getX(), this.getFocus().getY(), this.screenManager.getResolution().width, this.screenManager.getResolution().height);
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(new Point2D.Double(this.getCenterX(), this.getCenterY())));
  }
}
