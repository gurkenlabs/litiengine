package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxException;

/**
 * Indicates that a map object has failed to load.
 */
public class MapObjectException extends TmxException {

  private static final long serialVersionUID = 8092966074895091875L;

  public MapObjectException() {
  }

  public MapObjectException(String message) {
    super(message);
  }

  public MapObjectException(Throwable cause) {
    super(cause);
  }

  public MapObjectException(String message, Throwable cause) {
    super(message, cause);
  }
}