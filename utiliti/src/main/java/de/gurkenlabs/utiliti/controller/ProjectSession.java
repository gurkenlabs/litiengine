package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.function.Consumer;

/** A separately running game-project process controlled by utiLITI. */
public interface ProjectSession extends AutoCloseable {
  State state();

  default boolean isActive() {
    return switch (this.state()) {
      case STARTING, RUNNING, STOPPING -> true;
      case EXITED, FAILED -> false;
    };
  }

  OptionalInt exitCode();

  void writeInput(String input) throws IOException;

  void stop();

  void onOutput(Consumer<String> listener);

  void onStateChanged(Consumer<State> listener);

  @Override
  default void close() {
    this.stop();
  }

  enum State {
    STARTING,
    RUNNING,
    STOPPING,
    EXITED,
    FAILED
  }
}
