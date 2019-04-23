package de.gurkenlabs.litiengine.resources;

public class ResourceLoadException extends RuntimeException {
  private static final long serialVersionUID = 2690585643366673974L;

  public ResourceLoadException() {
  }

  public ResourceLoadException(String message) {
    super(message);
  }

  public ResourceLoadException(Throwable cause) {
    super(cause);
  }

  public ResourceLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
