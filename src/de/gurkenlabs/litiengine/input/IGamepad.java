package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

import net.java.games.input.Component.Identifier;

public interface IGamepad {
  public int getIndex();

  public String getName();

  public float getPollData(Identifier identifier);

  public void onPoll(String identifier, Consumer<Float> consumer);
}
