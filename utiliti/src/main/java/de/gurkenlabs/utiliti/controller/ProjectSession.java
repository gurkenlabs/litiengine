package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

/** A separately running game-project process controlled by utiLITI. */
public interface ProjectSession extends AutoCloseable {
  State state();

  default boolean isActive() {
    return switch (this.state()) {
      case STARTING, BUILDING, STARTING_GAME, RUNNING, STOPPING -> true;
      case EXITED, FAILED -> false;
    };
  }

  OptionalInt exitCode();

  /** A concise, actionable explanation when the external launch process could not start. */
  default Optional<String> failureMessage() {
    return Optional.empty();
  }

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
    BUILDING,
    STARTING_GAME,
    RUNNING,
    STOPPING,
    EXITED,
    FAILED
  }
}
