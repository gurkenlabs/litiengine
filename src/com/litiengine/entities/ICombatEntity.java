package com.litiengine.entities;

import java.awt.Shape;
import java.util.List;

import com.litiengine.abilities.Ability;
import com.litiengine.abilities.effects.Effect;
import com.litiengine.attributes.RangeAttribute;

public interface ICombatEntity extends ICollisionEntity {
  void addCombatEntityListener(CombatEntityListener listener);

  void removeCombatEntityListener(CombatEntityListener listener);

  void onHit(CombatEntityHitListener listener);

  void removeListener(CombatEntityHitListener listener);

  void onDeath(CombatEntityDeathListener listener);

  void removeListener(CombatEntityDeathListener listener);

  void onResurrect(CombatEntityResurrectListener listener);

  void removeListener(CombatEntityResurrectListener listener);

  void die();

  List<Effect> getAppliedEffects();

  RangeAttribute<Integer> getHitPoints();

  Shape getHitBox();

  ICombatEntity getTarget();

  int getTeam();

  void hit(int damage);

  void hit(int damage, Ability ability);

  boolean isDead();

  boolean isFriendly(final ICombatEntity entity);

  boolean isIndestructible();

  boolean isNeutral();

  void resurrect();

  void setIndestructible(final boolean indestructible);

  void setTarget(final ICombatEntity target);

  void setTeam(int team);

  boolean wasHit(int timeSpan);
}
