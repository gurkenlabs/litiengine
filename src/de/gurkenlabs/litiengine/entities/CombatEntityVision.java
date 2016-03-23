/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.IVision;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;

// TODO: Auto-generated Javadoc
/**
 * The Class LitiVision.
 */
public class CombatEntityVision implements IVision {

  /** The Constant DefaultLitiVision. */
  public static final int VISION_RADIUS = 200;

  /** The Constant FogOfWarColor. */
  private static final Color FogOfWarColor = new Color(0, 0, 0, 127);

  private final IEnvironment environment;

  /** The liti. */
  private final ICombatEntity combatEntity;

  /** The vision diameter. */
  private final int visionDiameter;

  /** The vision radius. */
  private final int visionRadius;

  private Shape renderVisionShape;

  /**
   * Instantiates a new liti vision.
   *
   * @param combatEntity
   *          the liti
   */
  public CombatEntityVision(final IEnvironment environment, final ICombatEntity combatEntity) {
    this.environment = environment;
    this.combatEntity = combatEntity;
    this.visionRadius = VISION_RADIUS;
    this.visionDiameter = VISION_RADIUS * 2;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#contains(int, int)
   */
  @Override
  public boolean contains(final int x, final int y) {
    return this.contains(new Point(x, y));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#contains(java.awt.geom.Point2D)
   */
  @Override
  public boolean contains(final Point2D point) {
    for (final ICombatEntity entity : this.environment.getCombatEntities()) {
      if (entity.isFriendly(this.combatEntity) && this.getMapVisionCircle(entity).contains(point)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Gets the render vision circle.
   *
   * @param entity
   *          the mob
   * @return the render vision circle
   */
  public Ellipse2D getRenderVisionCircle(final IEntity entity) {
    final Point2D renderDimensionCenter = Game.getScreenManager().getCamera().getViewPortDimensionCenter(entity);
    return new Ellipse2D.Double(renderDimensionCenter.getX() - this.visionRadius, renderDimensionCenter.getY() - this.visionRadius, this.visionDiameter, this.visionDiameter);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#getRenderVisionShape()
   */
  @Override
  public Shape getRenderVisionShape() {
    if (this.renderVisionShape == null) {
      this.updateVisionShape();
    }

    return this.renderVisionShape;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#intersects(java.awt.Shape)
   */
  @Override
  public boolean intersects(final Rectangle2D shape) {
    for (final ICombatEntity entity : this.environment.getCombatEntities()) {
      if (entity.isFriendly(this.combatEntity) && this.getMapVisionCircle(entity).intersects(shape)) {
        return true;
      }
    }

    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#renderFogOfWar(java.awt.Graphics)
   */
  @Override
  public void renderFogOfWar(final Graphics g) {
    // if we create a relative vision mask, we only need the screen size
    // otherwise we create a mask for the whole map
    final float width = Game.getScreenManager().getResolution().width / Game.getInfo().renderScale();
    final float height = Game.getScreenManager().getResolution().height / Game.getInfo().renderScale();
    final Rectangle2D rect = new Rectangle2D.Float(0, 0, width, height);

    final Area rectangleArea = new Area(rect);
    rectangleArea.subtract(new Area(this.getRenderVisionShape()));
    
    /*
     * Maybe we will add a more sophisticated vision algorithm in the future
     * that takes obstructed vision into consideration for(Destructable dest :
     * Game.instance().getScreenManager().getIngameScreen().getData().getMatch()
     * .getMapContainer().getDestructables()){ rectangleArea
     * .add(dest.getObstructedVisionArea(this.liti.getRenderDimensionCenter()));
     * }
     */

    g.setColor(FogOfWarColor);
    ((Graphics2D) g).fill(rectangleArea);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#renderMinimapFogOfWar(java.awt.
   * Graphics, float, int, int)
   */
  @Override
  public void renderMinimapFogOfWar(final Graphics g, final float minimapScale, final int x, final int y) {

    // if we create a relative vision mask, we only need the screen size
    // otherwise we create a mask for the whole map
    final float width = (float) (minimapScale * this.environment.getMap().getSizeInPixles().getWidth());
    final float height = (float) (minimapScale * this.environment.getMap().getSizeInPixles().getHeight());

    final Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
    final Area rectangleArea = new Area(rect);

    for (final ICombatEntity entity : this.environment.getCombatEntities()) {
      if (entity.isFriendly(this.combatEntity)) {
        final Ellipse2D visionEllipse = this.getMapVisionCircle(entity);
        final Ellipse2D scaledEllipse = new Ellipse2D.Double(x + visionEllipse.getX() * minimapScale, y + visionEllipse.getY() * minimapScale, visionEllipse.getWidth() * minimapScale, visionEllipse.getHeight() * minimapScale);
        rectangleArea.subtract(new Area(scaledEllipse));
      }
    }

    g.setColor(FogOfWarColor);
    ((Graphics2D) g).fill(rectangleArea);
  }

  /**
   * Gets the map vision circle.
   *
   * @param mob
   *          the mob
   * @return the map vision circle
   */
  private Ellipse2D getMapVisionCircle(final ICombatEntity entity) {
    return new Ellipse2D.Double(entity.getDimensionCenter().getX() - this.visionRadius, entity.getDimensionCenter().getY() - this.visionRadius, this.visionDiameter, this.visionDiameter);
  }

  @Override
  public void updateVisionShape() {
    Path2D path = new Path2D.Float();
    path.append(this.getRenderVisionCircle(this.combatEntity), false);

    for (final ICombatEntity entity : this.environment.getCombatEntities()) {
      if (entity.isFriendly(this.combatEntity) && !entity.equals(this.combatEntity)) {
        path.append(this.getRenderVisionCircle(entity), false);
      }
    }

    this.renderVisionShape = path;
  }
}
