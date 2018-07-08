package de.gurkenlabs.litiengine.environment;

public class MapObjectException extends RuntimeException {
  private static final long serialVersionUID = 8092966074895091875L;

  public MapObjectException() {}
  
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