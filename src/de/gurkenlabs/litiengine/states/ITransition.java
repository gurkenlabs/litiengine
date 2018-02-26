package de.gurkenlabs.litiengine.states;

public interface ITransition extends Comparable<ITransition> {
  public boolean conditionsFullfilled();

  public IState getNextState();

  public int getPriority();
}
