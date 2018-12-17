package de.gurkenlabs.litiengine.environment.tilemap.xml;

/**
 * Thrown to indicate that an external resource for a TMX file could not be found or loaded.
 */
public class MissingTmxResourceException extends TmxException {

  private static final long serialVersionUID = 2649018991304386841L;

  public MissingTmxResourceException() {
  }

  public MissingTmxResourceException(String message) {
    super(message);
  }

  public MissingTmxResourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public MissingTmxResourceException(Throwable cause) {
    super(cause);
  }
}
