package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class UnsupportedMapVersionException extends TmxException {
  private static final long serialVersionUID = 2987818719105510454L;

  UnsupportedMapVersionException() {}

  UnsupportedMapVersionException(String message) {
    super(message);
  }

  UnsupportedMapVersionException(String message, Throwable cause) {
    super(message, cause);
  }

  UnsupportedMapVersionException(Throwable cause) {
    super(cause);
  }
}
