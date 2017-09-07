/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.gurkenlabs.litiengine.net.Package;
import de.gurkenlabs.util.io.CompressionUtilities;
import de.gurkenlabs.util.io.Serializer;

/**
 * The Class ObjectPacket.
 *
 * @param <T>
 *          the generic type
 */
public class MessagePackage<T> extends Package {

  /**
   * Arrayconcat.
   *
   * @param A
   *          the a
   * @param B
   *          the b
   * @return the byte[]
   */
  private static byte[] arrayconcat(final byte[] A, final byte[] B) {
    final int aLen = A.length;
    final int bLen = B.length;
    final byte[] C = new byte[aLen + bLen];
    System.arraycopy(A, 0, C, 0, aLen);
    System.arraycopy(B, 0, C, aLen, bLen);
    return C;
  }

  /**
   * The Content length byte count. UDP does only support up to 64k.
   */
  private final int ContentLengthByteCount = 7;

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
  public MessagePackage(final byte packetId) {
    super(packetId);
  }

  /**
   * Instantiates a new object packet.
   *
   * @param content
   *          the content
   */
  @SuppressWarnings("unchecked")
  public MessagePackage(final byte[] content) {
    super(content);
    final int headerOffset = this.TypeByteCount;
    final int dataOffset = headerOffset + this.ContentLengthByteCount;

    // message size
    final ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(content, headerOffset, headerOffset + this.ContentLengthByteCount));

    // header + size info + actual message size
    this.size = this.ContentLengthByteCount + wrapped.getInt();

    // actual message
    final byte[] objectBytes = Arrays.copyOfRange(content, dataOffset, this.getSize() + dataOffset);

    try {
      final Object dataObject = Serializer.deserialize(objectBytes);
      this.object = (T) dataObject;
    } catch (final Exception e) {
      e.printStackTrace();
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
  public MessagePackage(final MessageType type, final T object) {
    super(type.getId());
    this.object = object;
  }

  /*
   * (non-Javadoc)
   *
   * @see liti.net.packages.Packet#getData()
   */
  @Override
  public byte[] getData() {
    if (super.getData() != null && super.getData().length > 0) {
      return super.getData();
    }

    final byte[] header = new byte[] { this.getPacketId() };
    final byte[] serializedObject = Serializer.serialize(this.object);
    final byte[] objectSize = ByteBuffer.allocate(this.ContentLengthByteCount).putInt(serializedObject.length).array();
    byte[] data = arrayconcat(header, arrayconcat(objectSize, serializedObject));
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
