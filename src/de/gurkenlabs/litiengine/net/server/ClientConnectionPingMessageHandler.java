package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;

import de.gurkenlabs.litiengine.net.messages.ClientMessage;
import de.gurkenlabs.litiengine.net.messages.MessageHandler;

public class ClientConnectionPingMessageHandler extends MessageHandler<ClientMessage> {

  private final IClientConnectionManager manager;

  public ClientConnectionPingMessageHandler(final IClientConnectionManager manager) {
    this.manager = manager;
  }

  @Override
  protected void handle(final ClientMessage message, final InetAddress address, final int port) {
    this.manager.setSignOfLife(message.getClientId());
  }
}