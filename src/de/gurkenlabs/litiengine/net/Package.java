/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net;

/**
 * The Class Package.
 */
public abstract class Package {

  /** The data. */
  private byte[] data;

  /** The packet id. */
  private final byte packetId;

  /** The Type byte count. */
  protected final int TypeByteCount = 1;

  /**
   * Instantiates a new package.
   *
   * @param packetId
   *          the packet id
   */
  protected Package(final byte packetId) {
    this.packetId = packetId;
  }

  /**
   * Instantiates a new package.
   *
   * @param content
   *          the content
   */
  protected Package(final byte[] content) {
    this(content[0]);
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public byte[] getData() {
    return this.data;
  }

  /**
   * Gets the packet id.
   *
   * @return the packet id
   */
  public byte getPacketId() {
    return this.packetId;
  }

  /**
   * Read data.
   *
   * @param data
   *          the data
   * @return the string
   */
  public String readData(final byte[] data) {
    final String message = new String(data).trim();
    return message.substring(2);
  }

  /**
   * Sets the data.
   *
   * @param data
   *          the new data
   */
  protected void setData(final byte[] data) {
    this.data = data;
  }
}
