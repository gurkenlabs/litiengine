/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The Class Serializer.
 */
public class Serializer {

  private Serializer() {
  }

  /**
   * Deserialize.
   *
   * @param bytes
   *          the bytes
   * @return the object
   */
  public static Object deserialize(final byte[] bytes) {
    final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
    ObjectInputStream o;
    try {
      o = new ObjectInputStream(b);
      return o.readObject();
    } catch (final IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Serialize.
   *
   * @param obj
   *          the obj
   * @return the byte[]
   */
  public static byte[] serialize(final Object obj) {
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream o;
    try {
      o = new ObjectOutputStream(b);
      o.writeObject(obj);

    } catch (final IOException e) {
      e.printStackTrace();
    }
    return b.toByteArray();
  }
}