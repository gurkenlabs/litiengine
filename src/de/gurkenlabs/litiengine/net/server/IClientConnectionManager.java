package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;
import java.util.List;


public interface IClientConnectionManager extends List<ClientConnection> {

  @Override
  public ClientConnection get(int clientId);

  public boolean isConnected(int connectionId, InetAddress address, int port);

  public void setSignOfLife(final int clientId);
}
