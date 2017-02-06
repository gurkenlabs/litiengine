package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.particles.IEntityEmitter;


@EmitterInfo(maxParticles = 0, spawnAmount = 0, activateOnInit = true)
@EntityInfo(renderType = RenderType.OVERLAY)
public class CustomEntityEmitter extends CustomEmitter implements IEntityEmitter {
  private final IEntity entity;

  public CustomEntityEmitter(IEntity entity, final String emitterXml) {
    super(entity.getLocation().getX(), entity.getLocation().getY(), emitterXml);
    this.entity = entity;
    this.setSize(this.getEntity().getWidth(), this.getEntity().getHeight());
    this.getEmitterData().getX().setMinValue(0);
    this.getEmitterData().getX().setMaxValue(this.getWidth());
    this.getEmitterData().getY().setMinValue(0);
    this.getEmitterData().getY().setMaxValue(this.getHeight());
  }

  @Override
  public Point2D getLocation() {
    if (this.getEntity() == null) {
      return null;
    }

    return new Point2D.Double(this.getEntity().getLocation().getX(), this.getEntity().getLocation().getY());
  }

  @Override
  public IEntity getEntity() {
    return this.entity;
  }
}
