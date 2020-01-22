package de.gurkenlabs.litiengine.net.messages;

import java.io.Serializable;
import java.net.InetAddress;

import de.gurkenlabs.litiengine.Game;

public abstract class ClientMessageHandler<T extends Serializable> extends MessageHandler<T> {

  @Override
  public void handle(final byte[] data, final InetAddress address, final int port) {
    super.handle(data, address, port);
    Game.metrics().packageReceived(new MessagePacket<T>(data).getSize());
  }
}
