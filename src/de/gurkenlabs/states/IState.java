package de.gurkenlabs.states;

import java.util.List;

import de.gurkenlabs.litiengine.IGameLoop;

public interface IState {
  public void enter();

  public void executeBehaviour(IGameLoop loop);

  public void exit();

  public String getName();

  public List<ITransition> getTransitions();
}
