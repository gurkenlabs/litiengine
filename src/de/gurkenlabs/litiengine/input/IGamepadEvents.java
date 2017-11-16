package de.gurkenlabs.litiengine.input;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IGamepadEvents {

  public void onPoll(String identifier, Consumer<Float> consumer);

  public void onPressed(String identifier, Consumer<Float> consumer);

  public void onPoll(BiConsumer<String, Float> consumer);

  public void onPressed(BiConsumer<String, Float> consumer);
}
