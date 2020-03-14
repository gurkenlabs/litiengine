package de.gurkenlabs.litiengine.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides serialization an deserialization mechanisms from Object to byte array and vice versa.
 */
public final class Serializer {
  private static final Logger log = Logger.getLogger(Serializer.class.getName());

  private Serializer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Deserializes an object from the specified byte array.
   *
   * @param bytes
   *          The byte array
   * 
   * @return The deserialized object.
   */
  public static Object deserialize(final byte[] bytes) {
    final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
    ObjectInputStream o;
    try {
      o = new ObjectInputStream(b);
      return o.readObject();
    } catch (final IOException | ClassNotFoundException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  /**
   * Serializes the specified object to a byte array.
   *
   * @param object
   *          The object to be serialized.
   * @return A serialized byte array representing the specified object.
   */
  public static byte[] serialize(final Object object) {
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream o;
    try {
      o = new ObjectOutputStream(b);
      o.writeObject(object);

    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return b.toByteArray();
  }
}