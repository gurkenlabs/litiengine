package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.IUpdateable;
import java.util.Collections;
import java.util.List;

/**
 * Represents a state machine that manages the states and transitions of an entity.
 */
public class StateMachine implements IUpdateable {

  private State currentState;

  /**
   * Gets the current state of the state machine.
   *
   * @return the current state
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Sets a new state for the state machine.
   *
   * @param newState the new state to be set
   */
  public void setState(State newState) {
    if (currentState != null) {
      currentState.exit();
    }

    currentState = newState;
    currentState.enter();
  }

  /**
   * Updates the state machine, performing the current state's actions and handling transitions.
   */
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
