package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class ExecutingEntityTargetTargetingStrategy extends TargetingStrategy {
  public ExecutingEntityTargetTargetingStrategy() {
    super(false, false);
  }

  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return List.of(executor.getTarget());
  }
}
