package de.gurkenlabs.litiengine.abilities.targeting;

/// A targeting strategy that selects friendly entities as ability targets.
///
/// This strategy extends [OtherEntityTargetingStrategy] and is preconfigured to include
/// only allied entities while excluding the executing entity itself. It is typically used for support abilities such as heals, buffs, or shields that
/// should affect friends but not foes or the caster.
///
/// @see OtherEntityTargetingStrategy
public class FriendlyTargetingStrategy extends OtherEntityTargetingStrategy {

  /// Creates a new `FriendlyTargetingStrategy`.
  ///
  /// @param multiTarget     if `true`, the strategy may return multiple matching targets;
  /// otherwise only a single target is selected.
  /// @param sortByDistance  if `true`, the resulting targets are ordered by their distance
  /// to the executing entity (closest first).
  /// @param includeDead     if `true`, dead entities are considered valid targets (useful
  /// for resurrection-like abilities); otherwise only living entities are
  /// selected.
  public FriendlyTargetingStrategy(boolean multiTarget, boolean sortByDistance, boolean includeDead) {
    super(multiTarget, sortByDistance, true, false, includeDead);
  }
}
