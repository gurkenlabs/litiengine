package de.gurkenlabs.litiengine.entities.behavior;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class State {
  private final String name;

  private final List<Transition> transitions;

  protected State(final String name) {
    this.transitions = new CopyOnWriteArrayList<>();
    this.name = name;
  }

  public void enter() {
    // this method is just implemented for convenient purposes
  }

  public void exit() {
    // this method is just implemented for convenient purposes
  }

  public String getName() {
    return this.name;
  }

  public List<Transition> getTransitions() {
    return this.transitions;
  }

  protected abstract void perform();
}
