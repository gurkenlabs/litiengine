package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.Force;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ForceEffect extends Effect {

  private final float strength;
  private final Map<IMobileEntity, Force> appliedForces;

  protected ForceEffect(
    final Ability ability, final float strength, final EffectTarget... targets) {
    super(ability, targets);
    this.strength = strength;
    this.appliedForces = new ConcurrentHashMap<>();
  }

  @Override
  public void apply(final ICombatEntity affectedEntity) {
    super.apply(affectedEntity);
    if (affectedEntity instanceof IMobileEntity iMobileEntity) {
      Force force = createForce(iMobileEntity);
      appliedForces.put(iMobileEntity, force);
      iMobileEntity.movement().apply(force);
    }
  }

  public float getStrength() {
    return this.strength;
  }

  protected abstract Force createForce(final IMobileEntity affectedEntity);

  @Override
  public void cease(ICombatEntity entity) {
    super.cease(entity);
    if (entity instanceof IMobileEntity iMobileEntity && appliedForces.containsKey(iMobileEntity)) {
      appliedForces.get(iMobileEntity).end();
      appliedForces.remove(iMobileEntity);
    }
  }

  @Override
  protected boolean hasEnded(final EffectApplication appliance) {
    return super.hasEnded(appliance) || appliedForces.isEmpty() || appliedForces.values().stream()
      .allMatch(Force::hasEnded);
  }
}
