package de.gurkenlabs.litiengine.abilities.targeting;

public class EnemyTargetingStrategy extends OtherEntityTargetingStrategy {

  public EnemyTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean includeDead) {
    super(multiTarget, sortByDistance, false, false, includeDead);
  }
}
