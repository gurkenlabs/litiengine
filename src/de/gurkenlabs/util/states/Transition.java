package de.gurkenlabs.util.states;

import de.gurkenlabs.litiengine.IGameLoop;

public abstract class Transition implements ITransition {
  private final int priority;

  protected Transition(final int priority) {
    this.priority = priority;
  }

  @Override
  public abstract boolean conditionsFullfilled(IGameLoop loop);

  @Override
  public abstract IState getNextState();

  @Override
  public int getPriority() {
    return this.priority;
  }

  @Override
  public int compareTo(ITransition other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }
}
