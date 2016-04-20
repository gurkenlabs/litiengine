/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
  protected static final Color FogOfWarColor = new Color(0, 0, 0, 127);

  private final IEnvironment environment;

  /** The liti. */
  private final ICombatEntity combatEntity;

  /** The vision diameter. */
  private int visionDiameter;

  /** The vision radius. */
  private int visionRadius;

  private Shape renderVisionShape;

  private Shape fogOfWar;

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
  public void renderFogOfWar(final Graphics2D g) {
    if (this.fogOfWar == null) {
      return;
    }

    final AffineTransform oldTransform = g.getTransform();
    final AffineTransform at = new AffineTransform();
    at.scale(Game.getInfo().renderScale(), Game.getInfo().renderScale());
    at.translate(Game.getScreenManager().getCamera().getPixelOffsetX(), Game.getScreenManager().getCamera().getPixelOffsetY());

    g.setTransform(at);
    g.setColor(FogOfWarColor);
    g.fill(this.fogOfWar);
    g.setTransform(oldTransform);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IVision#renderMinimapFogOfWar(java.awt.
   * Graphics, float, int, int)
   */
  @Override
  public void renderMinimapFogOfWar(final Graphics2D g, final float minimapScale, final int x, final int y) {
    final AffineTransform oldTransform = g.getTransform();

    final AffineTransform at = new AffineTransform();
    at.translate(x, y);
    at.scale(minimapScale, minimapScale);

    g.setTransform(at);
    g.setColor(FogOfWarColor);
    g.fill(this.fogOfWar);
    g.setTransform(oldTransform);
  }

  @Override
  public void updateVisionShape() {
    final Path2D path = new Path2D.Float();
    final Path2D renderPath = new Path2D.Float();
    path.append(this.getMapVisionCircle(this.combatEntity), false);
    renderPath.append(this.getRenderVisionArc(this.combatEntity), false);

    for (final ICombatEntity entity : this.environment.getCombatEntities()) {
      if (entity.isFriendly(this.combatEntity) && !entity.equals(this.combatEntity)) {
        path.append(this.getMapVisionCircle(entity), false);
        renderPath.append(this.getRenderVisionArc(entity), false);
      }
    }

    this.renderVisionShape = renderPath;

    final float width = (float) this.environment.getMap().getSizeInPixles().getWidth();
    final float height = (float) this.environment.getMap().getSizeInPixles().getHeight();
    final Rectangle2D rect = new Rectangle2D.Float(0, 0, width, height);
    final Area rectangleArea = new Area(rect);
    rectangleArea.subtract(new Area(path));

    this.fogOfWar = rectangleArea;
  }
  
  public void setVisionRadius(int radius){
    this.visionRadius = radius;
    this.visionDiameter = radius* 2;
  }

  protected ICombatEntity getCombatEntity() {
    return this.combatEntity;
  }

  protected Shape getFogOfWar() {
    return this.fogOfWar;
  }

  protected void setFogOfWar(final Shape fogOfWar) {
    this.fogOfWar = fogOfWar;
  }

  protected IEnvironment getEnvironment() {
    return this.environment;
  }

  protected void setRenderVisionShape(final Shape renderVisionShape) {
    this.renderVisionShape = renderVisionShape;
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

  /**
   * Gets the render vision circle.
   *
   * @param entity
   *          the mob
   * @return the render vision circle
   */
  private Ellipse2D getRenderVisionArc(final IEntity entity) {
    final Point2D renderDimensionCenter = Game.getScreenManager().getCamera().getViewPortDimensionCenter(entity);
    return new Ellipse2D.Double(renderDimensionCenter.getX() - this.visionRadius, renderDimensionCenter.getY() - this.visionRadius, this.visionDiameter, this.visionDiameter);
  }

}