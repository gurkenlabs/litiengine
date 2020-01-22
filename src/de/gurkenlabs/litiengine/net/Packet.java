package de.gurkenlabs.litiengine.net;

public abstract class Packet {

  protected static final int TYPEBYTECOUNT = 1;

  private final byte packetId;
  
  private byte[] data;

  /**
   * Instantiates a new packet.
   *
   * @param packetId
   *          the packet id
   */
  protected Packet(final byte packetId) {
    this.packetId = packetId;
  }

  /**
   * Instantiates a new packet.
   *
   * @param content
   *          the content
   */
  protected Packet(final byte[] content) {
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
