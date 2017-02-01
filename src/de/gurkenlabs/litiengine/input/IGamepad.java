package de.gurkenlabs.litiengine.input;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;

public interface IGamepad {
  public int getIndex();

  public String getName();

  public float getPollData(Identifier identifier);

  public void dispose();
}
