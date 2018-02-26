package de.gurkenlabs.litiengine.states;

import java.util.List;

public interface IState {
  public void enter();

  public void executeBehaviour();

  public void exit();

  public String getName();

  public List<ITransition> getTransitions();
}
