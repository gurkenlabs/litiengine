package de.gurkenlabs.litiengine.resources;

import java.io.Serial;

/**
 * Exception thrown when a resource fails to load.
 */
public class ResourceLoadException extends RuntimeException {
  @Serial private static final long serialVersionUID = 2690585643366673974L;

  /**
   * Constructs a new ResourceLoadException with {@code null} as its detail message.
   */
  public ResourceLoadException() {
  }

  /**
   * Constructs a new ResourceLoadException with the specified detail message.
   *
   * @param message The detail message.
   */
  public ResourceLoadException(String message) {
    super(message);
  }

  /**
   * Constructs a new ResourceLoadException with the specified cause.
   *
   * @param cause The cause of the exception.
   */
  public ResourceLoadException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new ResourceLoadException with the specified detail message and cause.
   *
   * @param message The detail message.
   * @param cause   The cause of the exception.
   */
  public ResourceLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
