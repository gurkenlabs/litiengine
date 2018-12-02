package de.gurkenlabs.litiengine.environment.tilemap.xml;

public class TmxError extends Error {

  private static final long serialVersionUID = 1352872701172365673L;

  public TmxError() {
  }

  public TmxError(String message) {
    super(message);
  }

  public TmxError(String message, Throwable cause) {
    super(message, cause);
  }

  public TmxError(Throwable cause) {
    super(cause);
  }
}
