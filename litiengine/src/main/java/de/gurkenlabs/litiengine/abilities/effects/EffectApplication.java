package de.gurkenlabs.litiengine.abilities.effects;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import java.awt.Shape;
import java.util.Collection;

/**
 * Represents the application of an effect, including the entities affected and the area of impact.
 */
public class EffectApplication {
  private final Collection<ICombatEntity> affectedEntities;
  private final long applied;
  private final Shape impactArea;

  /**
   * Constructs an EffectApplication with the specified affected entities and impact area.
   *
   * @param affectedEntities the entities affected by the effect
   * @param impactArea       the area where the effect is applied
   */
  protected EffectApplication(final Collection<ICombatEntity> affectedEntities, final Shape impactArea) {
    this.applied = Game.time().now();
    this.affectedEntities = affectedEntities;
    this.impactArea = impactArea;
  }

  /**
   * Gets the entities affected by the effect.
   *
   * @return the collection of affected entities
   */
  public Collection<ICombatEntity> getAffectedEntities() {
    return this.affectedEntities;
  }

  /**
   * Gets the time in ticks when the effect was applied.
   *
   * @return the applied time in ticks
   */
  public long getAppliedTicks() {
    return this.applied;
  }

  /**
   * Gets the shape of the area where the effect is applied.
   *
   * @return the impact area
   */
  public Shape getImpactArea() {
    return this.impactArea;
  }
}
