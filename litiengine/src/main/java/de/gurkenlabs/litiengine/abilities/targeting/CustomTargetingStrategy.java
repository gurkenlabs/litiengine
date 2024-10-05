package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

public class CustomTargetingStrategy extends TargetingStrategy {

  private final BiPredicate<ICombatEntity, ICombatEntity> targetingCondition;

  public CustomTargetingStrategy(BiPredicate<ICombatEntity, ICombatEntity> customPredicate, boolean multiTarget, boolean sortByDistance) {
    super(multiTarget, sortByDistance);
    this.targetingCondition = customPredicate;
  }

  @Override
  public Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    if (targetingCondition == null || Game.world() == null || Game.world().environment() == null) {
      return List.of();
    }

    return Game.world().environment().findCombatEntities(impactArea, e -> this.targetingCondition.test(executor, e));
  }
}
