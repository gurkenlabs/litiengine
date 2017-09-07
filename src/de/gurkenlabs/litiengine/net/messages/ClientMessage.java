/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.io.Serializable;

/**
 * The Class ClientMessage.
 */
public class ClientMessage implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3674946213323521377L;

  /** The client id. */
  private int clientId;

  /**
   * Instantiates a new client message.
   *
   * @param clientId
   *          the client id
   */
  public ClientMessage(final int clientId) {
    this.clientId = clientId;
  }

  /**
   * Gets the client id.
   *
   * @return the client id
   */
  public int getClientId() {
    return this.clientId;
  }

  /**
   * Sets the client id.
   *
   * @param clientId
   *          the new client id
   */
  public void setClientId(final int clientId) {
    this.clientId = clientId;
  }
}
