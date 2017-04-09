package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public class EffectApplication {
  private final List<ICombatEntity> affectedEntities;
  private final long applied;
  private final Shape impactArea;
  private final IEnvironment environment;

  protected EffectApplication(final IEnvironment environment, final long applied, final List<ICombatEntity> affectedEntities, final Shape impactArea) {
    this.applied = applied;
    this.affectedEntities = affectedEntities;
    this.impactArea = impactArea;
    this.environment = environment;
  }

  public List<ICombatEntity> getAffectedEntities() {
    return this.affectedEntities;
  }

  public long getAppliedTicks() {
    return this.applied;
  }

  public Shape getImpactArea() {
    return this.impactArea;
  }

  public IEnvironment getEnvironment() {
    return this.environment;
  }
}
