package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.IOException;

/**
 * Thrown to indicate that something has gone wrong with the processing of a TMX file.
 */
public class TmxException extends IOException {

  private static final long serialVersionUID = -2404149074008693966L;

  public TmxException() {
  }

  public TmxException(String message) {
    super(message);
  }

  public TmxException(String message, Throwable cause) {
    super(message, cause);
  }

  public TmxException(Throwable cause) {
    super(cause);
  }
}
