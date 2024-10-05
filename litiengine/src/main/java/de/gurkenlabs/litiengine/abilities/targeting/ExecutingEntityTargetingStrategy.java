package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class ExecutingEntityTargetingStrategy extends TargetingStrategy {
  public ExecutingEntityTargetingStrategy() {
    super(false, false);
  }

  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return List.of(executor);
  }
}
