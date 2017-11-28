package de.gurkenlabs.litiengine.net.server;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

/**
 * The Class ClientConnection.
 */
public class ClientConnection implements Serializable {
  /** The id sequence. */
  private static int idSequence = 0;

  private static final long serialVersionUID = 6311152055968351408L;

  /** The id. */
  private final int id;

  /** The ip address. */
  private final InetAddress ipAddress;

  /** The last sign of life. */
  private Date lastSignOfLife;

  /** The port. */
  private final int port;

  /** The account. */
  private final String userName;

  public ClientConnection(final ClientConnection connection) {
    super();
    this.ipAddress = connection.getIpAddress();
    this.port = connection.getPort();
    this.userName = connection.getUserName();
    this.lastSignOfLife = connection.getLastSignOfLife();
    this.id = connection.getId();
  }

  /**
   * Instantiates a new client connection.
   *
   * @param ipAddress
   *          the ip address
   * @param port
   *          the port
   * @param userName
   *          the userName
   */
  public ClientConnection(final InetAddress ipAddress, final int port, final String userName) {
    super();
    this.ipAddress = ipAddress;
    this.port = port;
    this.userName = userName;
    this.lastSignOfLife = new Date();
    this.id = ++idSequence;
  }

  /**
   * Equals.
   *
   * @param id
   *          the id
   * @param address
   *          the address
   * @param port
   *          the port
   * @return true, if successful
   */
  public boolean equals(final long id, final InetAddress address, final int port) {
    return this.getId() == id && this.getIpAddress().equals(address) && this.getPort() == port;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getId() {
    return this.id;
  }

  /**
   * Gets the ip address.
   *
   * @return the ip address
   */
  public InetAddress getIpAddress() {
    return this.ipAddress;
  }

  /**
   * Gets the last sign of life.
   *
   * @return the last sign of life
   */
  public Date getLastSignOfLife() {
    return this.lastSignOfLife;
  }

  public long getLastSignOfLifeInMs() {
    return new Date().getTime() - this.getLastSignOfLife().getTime();
  }

  /**
   * Gets the port.
   *
   * @return the port
   */
  public int getPort() {
    return this.port;
  }

  /**
   * Gets the account.
   *
   * @return the account
   */
  public String getUserName() {
    return this.userName;
  }

  /**
   * Sets the last sign of life.
   *
   * @param lastSignOfLife
   *          the new last sign of life
   */
  public void setSignOfLife(final Date lastSignOfLife) {
    this.lastSignOfLife = lastSignOfLife;
  }

  @Override
  public String toString() {
    return "Client(" + this.getId() + "): " + this.getIpAddress().getHostAddress() + ":" + this.getPort() + "; last sign of life: " + this.getLastSignOfLifeInMs() + "ms ago";
  }
}
