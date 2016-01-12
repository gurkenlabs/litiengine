package de.gurkenlabs.litiengine.net.messages;

import java.io.Serializable;

public class InvalidVersionMessage implements Serializable {

  private static final long serialVersionUID = -7372352605792869407L;

  private final float serverVersion;

  public InvalidVersionMessage(final float serverVersion) {
    this.serverVersion = serverVersion;
  }

  public float getServerVersion() {
    return this.serverVersion;
  }
}
