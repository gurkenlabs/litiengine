package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class MissingExternalTilesetException extends MissingTmxResourceException {

  private static final long serialVersionUID = 3758242882248677213L;

  MissingExternalTilesetException() {
  }

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
