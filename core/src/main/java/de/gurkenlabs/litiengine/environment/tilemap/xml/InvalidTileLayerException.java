package de.gurkenlabs.litiengine.environment.tilemap.xml;

/**
 * Thrown when an exception occurs while parsing tile data.
 */
public class InvalidTileLayerException extends TmxException {

  private static final long serialVersionUID = -863575375538927793L;

  public InvalidTileLayerException() {}

  public InvalidTileLayerException(String message) {
    super(message);
  }

  public InvalidTileLayerException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidTileLayerException(Throwable cause) {
    super(cause);
  }
}
