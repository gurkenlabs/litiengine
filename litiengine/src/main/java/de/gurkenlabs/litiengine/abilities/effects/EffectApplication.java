package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Collection;

public class EffectApplication {
  private final Collection<ICombatEntity> affectedEntities;
  private final long applied;
  private final Shape impactArea;

  protected EffectApplication(final Collection<ICombatEntity> affectedEntities, final Shape impactArea) {
    this.applied = Game.time().now();
    this.affectedEntities = affectedEntities;
    this.impactArea = impactArea;
  }

  public Collection<ICombatEntity> getAffectedEntities() {
    return this.affectedEntities;
  }

  public long getAppliedTicks() {
    return this.applied;
  }

  public Shape getImpactArea() {
    return this.impactArea;
  }
}
