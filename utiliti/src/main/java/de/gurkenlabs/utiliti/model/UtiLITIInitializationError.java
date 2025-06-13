package de.gurkenlabs.utiliti.model;

/**
 * Represents an initialization error specific to the UtiLITI application. This error is thrown when there is a problem during the initialization
 * process.
 */
public class UtiLITIInitializationError extends Error {

  /**
   * Constructs a new UtiLITIInitializationError with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause   the cause of the error.
   */
  public UtiLITIInitializationError(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new UtiLITIInitializationError with the specified cause.
   *
   * @param cause the cause of the error.
   */
  public UtiLITIInitializationError(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new UtiLITIInitializationError with the specified detail message.
   *
   * @param message the detail message.
   */
  public UtiLITIInitializationError(String message) {
    super(message);
  }
}
