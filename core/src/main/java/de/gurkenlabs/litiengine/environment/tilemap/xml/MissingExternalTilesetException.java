package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serial;

public class MissingExternalTilesetException extends MissingTmxResourceException {

  @Serial
  private static final long serialVersionUID = 3758242882248677213L;

  MissingExternalTilesetException() {}

  MissingExternalTilesetException(String message) {
    super(message);
  }

  MissingExternalTilesetException(String message, Throwable cause) {
    super(message, cause);
  }

  MissingExternalTilesetException(Throwable cause) {
    super(cause);
  }
}
