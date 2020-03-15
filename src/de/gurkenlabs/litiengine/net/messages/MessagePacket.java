package de.gurkenlabs.litiengine.net.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.net.Packet;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.io.CompressionUtilities;
import de.gurkenlabs.litiengine.util.io.Serializer;

/**
 * The Class ObjectPacket.
 *
 * @param <T>
 *          the generic type
 */
public class MessagePacket<T> extends Packet {

  private static final Logger log = Logger.getLogger(MessagePacket.class.getName());
  /**
   * The Content length byte count. UDP does only support up to 64k.
   */
  private static final int CONTENTLENGTHBYTECOUNT = 7;

  /** The object. */
  private T object;

  /** The size. */
  private int size;

  /**
   * Instantiates a new object packet.
   *
   * @param packetId
   *          the packet id
   */
  public MessagePacket(final byte packetId) {
    super(packetId);
  }

  /**
   * Instantiates a new object packet.
   *
   * @param content
   *          the content
   */
  @SuppressWarnings("unchecked")
  public MessagePacket(final byte[] content) {
    super(content);
    final int headerOffset = TYPEBYTECOUNT;
    final int dataOffset = headerOffset + CONTENTLENGTHBYTECOUNT;

    // message size
    final ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(content, headerOffset, headerOffset + CONTENTLENGTHBYTECOUNT));

    // header + size info + actual message size
    this.size = CONTENTLENGTHBYTECOUNT + wrapped.getInt();

    // actual message
    final byte[] objectBytes = Arrays.copyOfRange(content, dataOffset, this.getSize() + dataOffset);

    try {
      final Object dataObject = Serializer.deserialize(objectBytes);
      this.object = (T) dataObject;
    } catch (final Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      this.object = null;
    }
  }

  /**
   * Instantiates a new object packet.
   *
   * @param type
   *          the type
   * @param object
   *          the object
   */
  public MessagePacket(final MessageType type, final T object) {
    super(type.getId());
    this.object = object;
  }

  @Override
  public byte[] getData() {
    if (super.getData() != null && super.getData().length > 0) {
      return super.getData();
    }

    final byte[] header = new byte[] { this.getPacketId() };
    final byte[] serializedObject = Serializer.serialize(this.object);
    final byte[] objectSize = ByteBuffer.allocate(CONTENTLENGTHBYTECOUNT).putInt(serializedObject.length).array();
    byte[] data = ArrayUtilities.concat(header, ArrayUtilities.concat(objectSize, serializedObject));
    data = CompressionUtilities.compress(data);
    this.setData(data);
    return data;
  }

  /**
   * Gets the object.
   *
   * @return the object
   */
  public T getObject() {
    return this.object;
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  public int getSize() {
    return this.size;
  }
}
