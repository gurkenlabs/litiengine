package de.gurkenlabs.util.states;

import de.gurkenlabs.litiengine.IGameLoop;

public abstract class Transition implements ITransition {
  private final int priority;
  private IState state;

  protected Transition(final int priority) {
    this.priority = priority;
  }

  protected Transition(final int priority, final IState state) {
    this(priority);
    this.state = state;
  }

  @Override
  public int compareTo(final ITransition other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }

  @Override
  public abstract boolean conditionsFullfilled(IGameLoop loop);

  @Override
  public IState getNextState() {
    return this.state;
  }

  @Override
  public int getPriority() {
    return this.priority;
  }
}
