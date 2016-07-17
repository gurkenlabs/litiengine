package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;

public interface ICombatEntity extends ICollisionEntity {
  public CombatAttributes getAttributes();

  public Ellipse2D getHitBox();

  public int getTeam();

  public ICombatEntity getTarget();

  public void setTarget(final ICombatEntity target);

  /**
   *
   * @param damage
   * @return Returns if the entity died by the hit.
   */
  public boolean hit(int damage, Ability ability);

  public void die();

  public void resurrect();

  public boolean isDead();

  public boolean isFriendly(final ICombatEntity entity);

  public boolean isNeutral();

  public boolean isIndestructible();

  public void onDeath(Consumer<ICombatEntity> consumer);

  public void onResurrect(Consumer<ICombatEntity> consumer);

  public void onHit(Consumer<CombatEntityHitArgument> consumer);

  public void setTeam(int team);
  
  public List<IEffect> getAppliedEffects();
}
