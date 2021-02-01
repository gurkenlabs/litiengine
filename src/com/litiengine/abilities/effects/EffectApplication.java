package com.litiengine.abilities.effects;

import java.awt.Shape;
import java.util.List;

import com.litiengine.Game;
import com.litiengine.entities.ICombatEntity;

public class EffectApplication {
  private final List<ICombatEntity> affectedEntities;
  private final long applied;
  private final Shape impactArea;

  protected EffectApplication(final List<ICombatEntity> affectedEntities, final Shape impactArea) {
    this.applied = Game.time().now();
    this.affectedEntities = affectedEntities;
    this.impactArea = impactArea;
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
}
