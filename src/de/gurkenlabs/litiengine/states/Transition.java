package de.gurkenlabs.litiengine.states;

public abstract class Transition implements Comparable<Transition> {
  private final int priority;
  private State state;

  protected Transition(final int priority) {
    this.priority = priority;
  }

  protected Transition(final int priority, final State state) {
    this(priority);
    this.state = state;
  }

  @Override
  public int compareTo(final Transition other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }

  public State getNextState() {
    return this.state;
  }

  public int getPriority() {
    return this.priority;
  }

  protected abstract boolean conditionsFullfilled();
}
