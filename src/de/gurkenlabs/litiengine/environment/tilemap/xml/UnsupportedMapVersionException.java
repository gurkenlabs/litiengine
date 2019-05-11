package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class UnsupportedMapVersionException extends TmxException {
  private static final long serialVersionUID = 2987818719105510454L;

  public UnsupportedMapVersionException() {
  }

  public UnsupportedMapVersionException(String message) {
    super(message);
  }

  public UnsupportedMapVersionException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnsupportedMapVersionException(Throwable cause) {
    super(cause);
  }
}
