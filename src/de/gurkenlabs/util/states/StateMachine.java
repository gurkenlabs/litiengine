package de.gurkenlabs.util.states;

import java.util.Collections;
import java.util.List;

import de.gurkenlabs.litiengine.IGameLoop;

public class StateMachine implements IStateMachine {
  private IState currentState;

  protected StateMachine(final IGameLoop loop) {
    loop.registerForUpdate(this);
  }

  @Override
  public void update(IGameLoop loop) {
    if (this.currentState == null) {
      return;
    }

    this.currentState.executeBehaviour(loop);
    List<ITransition> transitions = this.currentState.getTransitions();
    Collections.sort(transitions);

    for (ITransition transition : transitions) {
      if (!transition.conditionsFullfilled(loop)) {
        continue;
      }

      this.currentState.exit();
      this.currentState = transition.getNextState();
      this.currentState.enter();
      return;
    }
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
  public IState getCurrentState() {
    return this.currentState;
  }
}
