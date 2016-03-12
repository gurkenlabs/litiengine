package de.gurkenlabs.util.states;

import de.gurkenlabs.litiengine.IGameLoop;

public interface ITransition extends Comparable<ITransition> {
  public int getPriority();

  public boolean conditionsFullfilled(IGameLoop loop);

  public IState getNextState();
}
