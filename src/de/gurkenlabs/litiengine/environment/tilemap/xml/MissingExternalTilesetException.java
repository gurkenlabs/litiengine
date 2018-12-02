package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class MissingExternalTilesetException extends MissingTmxResourceException {

  private static final long serialVersionUID = 3758242882248677213L;

  public MissingExternalTilesetException() {
  }

  public MissingExternalTilesetException(String message) {
    super(message);
  }
  
  public MissingExternalTilesetException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public MissingExternalTilesetException(Throwable cause) {
    super(cause);
  }
}
