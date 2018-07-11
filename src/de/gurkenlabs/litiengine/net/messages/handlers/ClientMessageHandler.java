package de.gurkenlabs.litiengine.net.messages.handlers;

import java.io.Serializable;
import java.net.InetAddress;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.net.messages.MessagePacket;

public abstract class ClientMessageHandler<T extends Serializable> extends MessageHandler<T> {

  @Override
  public void handle(final byte[] data, final InetAddress address, final int port) {
    super.handle(data, address, port);
    Game.getMetrics().packageReceived(new MessagePacket<T>(data).getSize());
  }
}
