package de.gurkenlabs.litiengine.abilities.targeting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;

import java.awt.*;
import java.util.Collection;

public class OtherEntityTargetingStrategy extends TargetingStrategy {
  private final boolean friendly;
  private final boolean includeExecutor;
  private final boolean includeDead;

  public OtherEntityTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean friendly) {
    this(multiTarget, sortByDistance, friendly, false, false);
  }

  public OtherEntityTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean friendly, boolean includeExecutor, boolean includeDead) {
    super(multiTarget, sortByDistance);

    this.friendly = friendly;
    this.includeExecutor = includeExecutor;
    this.includeDead = includeDead;
  }

  @Override
  protected Collection<ICombatEntity> findTargetsInternal(Shape impactArea, ICombatEntity executor) {
    return Game.world().environment().findCombatEntities(impactArea, e ->
      (this.friendly && e.isFriendly(executor) || !this.friendly && !e.isFriendly(executor))
        && (this.includeDead || !e.isDead())
        && (this.includeExecutor || !e.equals(executor)));
  }
}
