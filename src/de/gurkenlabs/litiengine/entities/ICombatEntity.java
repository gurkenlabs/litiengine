package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.function.Consumer;

public interface ICombatEntity extends ICollisionEntity {
  public CombatAttributes getAttributes();

  public Ellipse2D getHitBox();

  public int getTeam();

  public void hit(int damage);

  public boolean isDead();

  public boolean isFriendly(final ICombatEntity entity);

  public boolean isIndestructible();

  public void onDeath(Consumer<ICombatEntity> consumer);

  public void onHit(Consumer<CombatEntityHitArgument> consumer);

  public void setTeam(int team);

  public class CombatEntityHitArgument {
    private final ICombatEntity entity;
    private final float damage;

    public CombatEntityHitArgument(final ICombatEntity entity, final float damage) {
      this.entity = entity;
      this.damage = damage;
    }

    public float getDamage() {
      return this.damage;
    }

    public ICombatEntity getEntity() {
      return this.entity;
    }
  }
}
