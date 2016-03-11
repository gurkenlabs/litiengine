package de.gurkenlabs.util.states;

public abstract class Transition implements ITransition {
  private final IState state;
  private final int priority;

  protected Transition(final IState state, final int priority) {
    this.state = state;
    this.priority = priority;
  }

  @Override
  public abstract boolean conditionsFullfilled();

  @Override
  public abstract IState getNextState();

  @Override
  public IState getState() {
    return this.state;
  }

  @Override
  public int getPriority() {
    return this.priority;
  }

  @Override
  public int compareTo(ITransition other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }
}
