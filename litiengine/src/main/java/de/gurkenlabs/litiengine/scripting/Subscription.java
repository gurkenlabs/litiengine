package de.gurkenlabs.litiengine.scripting;

/** A removable event or runtime registration. */
@FunctionalInterface
public interface Subscription extends AutoCloseable {
  @Override
  void close();

  default void unsubscribe() {
    this.close();
  }
}

