package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

public class FixedTargetingStrategy extends TargetingStrategy {
  private Collection<ICombatEntity> fixedTargets;

  /**
   * Constructor for the `FixedTargetingStrategy` class.
   */
  protected FixedTargetingStrategy(ICombatEntity... fixedTargets) {
    super(fixedTargets.length > 1, false);
    this.fixedTargets = Arrays.asList(fixedTargets);
  }

  @Override
  protected Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return this.fixedTargets;
  }
}
