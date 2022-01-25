package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serial;

public class MissingImageException extends MissingTmxResourceException {
  @Serial
  private static final long serialVersionUID = -2828113271068412134L;

  MissingImageException() {}

  MissingImageException(String message) {
    super(message);
  }

  MissingImageException(String message, Throwable cause) {
    super(message, cause);
  }

  MissingImageException(Throwable cause) {
    super(cause);
  }
}
