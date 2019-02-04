package de.gurkenlabs.litiengine.entities;

import java.awt.Shape;
import java.util.List;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

public interface ICombatEntity extends ICollisionEntity {
  public void addCombatEntityListener(CombatEntityListener listener);

  public void removeCombatEntityListener(CombatEntityListener listener);

  public void addHitListener(CombatEntityHitListener listener);

  public void removeHitListener(CombatEntityHitListener listener);

  public void addDeathListener(CombatEntityDeathListener listener);

  public void removeDeathListener(CombatEntityDeathListener listener);

  public void die();

  public List<Effect> getAppliedEffects();

  public RangeAttribute<Integer> getHitPoints();

  public Shape getHitBox();

  public ICombatEntity getTarget();

  public int getTeam();

  public boolean hit(int damage);

  public boolean hit(int damage, Ability ability);

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
