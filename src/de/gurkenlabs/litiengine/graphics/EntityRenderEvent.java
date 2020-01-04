package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.util.EventObject;

import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityRenderEvent extends EventObject {
  private static final long serialVersionUID = 6397005859146712222L;
  
  private final transient Graphics2D graphics;
  private final transient IEntity entity;

  public EntityRenderEvent(final Graphics2D graphics, final IEntity entity) {
    super(entity);
    
    this.graphics = graphics;
    this.entity = entity;
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  public IEntity getEntity() {
    return this.entity;
  }
}
