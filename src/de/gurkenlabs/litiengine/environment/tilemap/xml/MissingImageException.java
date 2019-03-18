package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class MissingImageException extends MissingTmxResourceException {
  private static final long serialVersionUID = -2828113271068412134L;

  public MissingImageException() {
  }

  public MissingImageException(String message) {
    super(message);
  }

  public MissingImageException(String message, Throwable cause) {
    super(message, cause);
  }

  public MissingImageException(Throwable cause) {
    super(cause);
  }
}
