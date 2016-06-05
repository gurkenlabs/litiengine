package de.gurkenlabs.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

public class EffectAppliance {
  public final long applied;
  public final List<ICombatEntity> affectedEntities;
  public final Shape impactArea;

  protected EffectAppliance(final long applied, final List<ICombatEntity> affectedEntities, final Shape impactArea) {
    this.applied = applied;
    this.affectedEntities = affectedEntities;
    this.impactArea = impactArea;
  }

  public long getAppliedTicks() {
    return this.applied;
  }

  public List<ICombatEntity> getAffectedEntities() {
    return this.affectedEntities;
  }

  public Shape getImpactArea() {
    return this.impactArea;
  }
}
