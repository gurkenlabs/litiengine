package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;

public interface ICombatEntity extends ICollisionEntity {
  public void addCombatEntityListener(CombatEntityListener listener);

  public void removeCombatEntityListener(CombatEntityListener listener);

  public void addHitListener(CombatEntityHitListener listener);

  public void removeHitListener(CombatEntityHitListener listener);

  public void addDeathListener(CombatEntityDeathListener listener);

  public void removeDeathListener(CombatEntityDeathListener listener);

  public void die();

  public List<IEffect> getAppliedEffects();

  public RangeAttribute<Integer> getHitPoints();

  // TODO: This could be refactored to be a shape which would allow for the game
  // developer to decide what he wants to use.
  public Ellipse2D getHitBox();

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
