package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.IUpdateable;
import java.util.Collections;
import java.util.List;

/**
 * A StateMachine manages states and transition to model conditional behaviour.
 */
public class StateMachine implements IUpdateable {

  private State currentState;

  /**
   * Get the current state of the StateMachine.
   *
   * @return The current state.
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Set the new state for the StateMachine.
   *
   * @param newState The new state to set.
   */
  public void setState(State newState) {
    if (currentState != null) {
      currentState.exit();
    }

    currentState = newState;
    currentState.enter();
  }

  @Override
  public void update() {
    if (currentState == null) {
      return;
    }

    currentState.perform();
    List<Transition> transitions = currentState.getTransitions();
    Collections.sort(transitions);

    for (Transition transition : transitions) {
      if (transition.conditionsFullfilled()) {
        currentState.exit();
        currentState = transition.getNextState();
        currentState.enter();
        return;
      }
    }
  }
}
