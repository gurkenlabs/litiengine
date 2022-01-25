package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serial;

public class UnsupportedMapVersionException extends TmxException {
  @Serial
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
