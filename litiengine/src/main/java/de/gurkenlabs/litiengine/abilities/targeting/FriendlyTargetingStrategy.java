package de.gurkenlabs.litiengine.abilities.targeting;

public class FriendlyTargetingStrategy extends OtherEntityTargetingStrategy {

  public FriendlyTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean includeDead) {
    super(multiTarget, sortByDistance, true, false, includeDead);
  }
}
