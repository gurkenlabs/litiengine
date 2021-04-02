package de.gurkenlabs.utiliti;

@SuppressWarnings("serial")
class UtiLITIInitializationError extends Error {
  
  public UtiLITIInitializationError(String message, Throwable cause) {
    super(message, cause);
  }
  
  public UtiLITIInitializationError(Throwable cause) {
    super(cause);
  }
  
  public UtiLITIInitializationError(String message) {
    super(message);
  }
  
}
