package de.gurkenlabs.litiengine.environment.tilemap.xml;

/**
 * Thrown to indicate that an external resource for a TMX file could not be found or loaded.
 */
public class MissingTmxResourceException extends TmxException {

  private static final long serialVersionUID = 2649018991304386841L;

  protected MissingTmxResourceException() {
  }

  protected MissingTmxResourceException(String message) {
    super(message);
  }

  protected MissingTmxResourceException(String message, Throwable cause) {
    super(message, cause);
  }

  protected MissingTmxResourceException(Throwable cause) {
    super(cause);
  }
}
