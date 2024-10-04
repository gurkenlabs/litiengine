package de.gurkenlabs.litiengine.entities.behavior;

/**
 * Represents a transition with a priority and a target state.
 */
public abstract class Transition implements Comparable<Transition> {
  private final int priority;
  private State targetState;

  /**
   * Initializes a new instance of the Transition class with the specified priority.
   *
   * @param priority the priority of the transition
   */
  protected Transition(final int priority) {
    this.priority = priority;
  }

  /**
   * Initializes a new instance of the Transition class with the specified priority and target state.
   *
   * @param priority    the priority of the transition
   * @param targetState the target state of the transition
   */
  protected Transition(final int priority, final State targetState) {
    this(priority);
    this.targetState = targetState;
  }

  /**
   * Compares this transition with another transition based on their priorities.
   *
   * @param other the other transition to compare with
   * @return a negative integer, zero, or a positive integer as this transition's priority is less than, equal to, or greater than the specified
   * transition's priority
   */
  @Override
  public int compareTo(final Transition other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }

  /**
   * Gets the next state of the transition.
   *
   * @return the next state of the transition
   */
  public State getNextState() {
    return this.targetState;
  }

  /**
   * Gets the priority of the transition.
   *
   * @return the priority of the transition
   */
  public int getPriority() {
    return this.priority;
  }

  /**
   * Checks if the conditions for the transition are fulfilled.
   *
   * @return true if the conditions are fulfilled, false otherwise
   */
  protected abstract boolean conditionsFullfilled();
}
