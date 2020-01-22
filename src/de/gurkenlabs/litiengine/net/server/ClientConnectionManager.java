package de.gurkenlabs.litiengine.net.server;

import java.net.InetAddress;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientConnectionManager extends CopyOnWriteArrayList<ClientConnection> implements IClientConnectionManager {

  private static final long serialVersionUID = 3719486731770172645L;

  /**
   * Gets the connection.
   *
   * @param clientId
   *          the client id
   * @return the connection
   */
  @Override
  public ClientConnection get(final int clientId) {
    if (this.stream().noneMatch(connection -> connection.getId() == clientId)) {
      return null;
    }

    Optional<ClientConnection> opt = this.stream().filter(connection -> connection.getId() == clientId).findFirst();
    return opt.isPresent() ? opt.get() : null;
  }

  /**
   * Checks if the specified parameters identify a valid client.
   *
   * @param clientId
   *          the client id
   * @param address
   *          the address
   * @param port
   *          the port
   * @return true, if is valid connection
   */
  @Override
  public boolean isConnected(final int clientId, final InetAddress address, final int port) {
    return this.stream().anyMatch(connection -> connection.equals(clientId, address, port));
  }

  @Override
  public void setSignOfLife(final int clientId) {
    // update sign of life
    for (final ClientConnection connection : this) {
      if (connection.getId() == clientId) {
        connection.setSignOfLife(new Date());
      }
    }
  }
}
