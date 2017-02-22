package de.gurkenlabs.util.states;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class State implements IState {
  private final String name;

  private final List<ITransition> transitions;

  protected State(final String name) {
    this.transitions = new CopyOnWriteArrayList<>();

    this.name = name;
  }

  @Override
  public void enter() {
  }

  @Override
  public void exit() {
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public List<ITransition> getTransitions() {
    return this.transitions;
  }
}
