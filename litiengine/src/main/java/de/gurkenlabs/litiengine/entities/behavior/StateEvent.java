package de.gurkenlabs.litiengine.entities.behavior;

import java.util.EventObject;

public class StateEvent extends EventObject {

  private final State state;

  public StateEvent(State state) {
    super(state);
    this.state = state;
  }

  public State getState() {
    return state;
  }
}
