package de.gurkenlabs.states;

import java.util.Collections;
import java.util.List;

public class StateMachine implements IStateMachine {
  private IState currentState;

  protected StateMachine() {
  }

  @Override
  public IState getCurrentState() {
    return this.currentState;
  }

  @Override
  public void setState(final IState newState) {
    if (this.currentState != null) {
      this.currentState.exit();
    }

    this.currentState = newState;
    this.currentState.enter();
  }

  @Override
  public void update() {
    if (this.currentState == null) {
      return;
    }

    this.currentState.executeBehaviour();
    final List<ITransition> transitions = this.currentState.getTransitions();
    Collections.sort(transitions);

    for (final ITransition transition : transitions) {
      if (!transition.conditionsFullfilled()) {
        continue;
      }

      this.currentState.exit();
      this.currentState = transition.getNextState();
      this.currentState.enter();
      return;
    }
  }
}
