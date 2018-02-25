package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;

public interface ICombatEntity extends ICollisionEntity {
  public void die();

  public List<IEffect> getAppliedEffects();

  public CombatAttributes getAttributes();

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

  public void onDeath(Consumer<ICombatEntity> consumer);

  public void onHit(Consumer<CombatEntityHitArgument> consumer);

  public void onResurrect(Consumer<ICombatEntity> consumer);

  public void resurrect();

  public void setIndestructible(final boolean indestructible);

  public void setTarget(final ICombatEntity target);

  public void setTeam(int team);
}
