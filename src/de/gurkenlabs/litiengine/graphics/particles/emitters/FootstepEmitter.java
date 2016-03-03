package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;

@EmitterInfo(maxParticles = 1, spawnAmount = 1, emitterTTL = 5000, particleMinTTL = 5000, particleMaxTTL = 5000)
public class FootstepEmitter extends Emitter {
  private final float stepWidth;
  private final float stepHeight;
  private final Direction dir;
  private final boolean left;

  public FootstepEmitter(final IMovableCombatEntity entity, final boolean left) {
    super(entity.getCollisionBox().getX() + entity.getCollisionBox().getWidth() / 2, entity.getCollisionBox().getY() + entity.getCollisionBox().getHeight() / 2);
    this.stepWidth = entity.getWidth() / 8;
    this.stepHeight = entity.getHeight() / 6;
    this.dir = entity.getFacingDirection();
    this.left = left;
  }

  @Override
  protected Particle createNewParticle() {
    float width;
    float height;
    int x;
    int y;
    if (this.dir == Direction.DOWN || this.dir == Direction.UP) {
      width = this.stepWidth;
      height = this.stepHeight;

      x = (int) (this.left ? -this.stepWidth * 1.25 : this.stepWidth * 1.25);
      y = 0;
    } else {
      width = this.stepHeight;
      height = this.stepWidth;

      x = 0;
      y = (int) (this.left ? -this.stepWidth * 1.25 : this.stepWidth * 1.25);
    }

    final int life = this.getRandomParticleTTL();

    final Particle p = new RectangleFillParticle(x, y, 0, 0, 0, 0, width, height, life, new Color(0, 0, 0, 80));
    p.setColorAlpha(50);

    return p;
  }
}
