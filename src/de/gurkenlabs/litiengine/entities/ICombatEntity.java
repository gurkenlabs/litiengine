package de.gurkenlabs.litiengine.entities;

import java.awt.Shape;
import java.util.List;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

public interface ICombatEntity extends ICollisionEntity {
  public void addCombatEntityListener(CombatEntityListener listener);

  public void removeCombatEntityListener(CombatEntityListener listener);

  public void onHit(CombatEntityHitListener listener);

  public void removeListener(CombatEntityHitListener listener);

  public void onDeath(CombatEntityDeathListener listener);

  public void removeListener(CombatEntityDeathListener listener);

  public void onResurrect(CombatEntityResurrectListener listener);

  public void removeListener(CombatEntityResurrectListener listener);

  public void die();

  public List<Effect> getAppliedEffects();

  public RangeAttribute<Integer> getHitPoints();

  public Shape getHitBox();

  public ICombatEntity getTarget();

  public int getTeam();

  public void hit(int damage);

  public void hit(int damage, Ability ability);

  public boolean isDead();

  public boolean isFriendly(final ICombatEntity entity);

  public boolean isIndestructible();

  public boolean isNeutral();

  public void resurrect();

  public void setIndestructible(final boolean indestructible);

  public void setTarget(final ICombatEntity target);

  public void setTeam(int team);

  public boolean wasHit(int timeSpan);
}
