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

  public CustomEntityEmitter(final IEntity entity, final String emitterXml) {
    super(entity.getLocation().getX(), entity.getLocation().getY(), emitterXml);
    this.entity = entity;
    this.setSize(this.getEntity().getWidth(), this.getEntity().getHeight());
    this.getEmitterData().getParticleX().setMinValue(0);
    this.getEmitterData().getParticleX().setMaxValue(this.getWidth());
    this.getEmitterData().getParticleY().setMinValue(0);
    this.getEmitterData().getParticleY().setMaxValue(this.getHeight());
  }

  @Override
  public IEntity getEntity() {
    return this.entity;
  }

  @Override
  public Point2D getLocation() {
    if (this.getEntity() == null) {
      return null;
    }

    return new Point2D.Double(this.getEntity().getLocation().getX(), this.getEntity().getLocation().getY());
  }
}
