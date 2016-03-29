package de.gurkenlabs.util.states;

import java.util.List;

import de.gurkenlabs.litiengine.IGameLoop;

public interface IState {
  public String getName();

  public void enter();

  public void exit();

  public void executeBehaviour(IGameLoop loop);

  public List<ITransition> getTransitions();
}
