/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.net.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Class MessageType defines a static struct which contains all possible
 * message types for network communication. Some types are litiengine default.
 * If you need to create different messages you should use message ids in the
 * range between 20 and 127; Also see TODO(wiki) for more details.
 */
public class MessageType {
  private static List<MessageType> messageTypes = new ArrayList<>();
  public static MessageType INVALID = new MessageType("INVALID", (byte) -1);
  public static MessageType INVALIDVERSION = new MessageType("INVALIDVERSION", (byte) 18);
  public static MessageType LOGIN = new MessageType("LOGIN", (byte) 0);
  public static MessageType LOGINRESPONSE = new MessageType("LOGINRESPONSE", (byte) 5);
  public static MessageType LOGOUT = new MessageType("LOGOUT", (byte) 1);
  public static MessageType PING = new MessageType("PING", (byte) 4);

  private final String name;
  /** The packet id. */
  private final byte packetId;

  /**
   * Instantiates a new message type.
   *
   * @param messageId
   *          the packet id
   */
  public MessageType(final String name, final byte messageId) {
    if (messageTypes.stream().anyMatch(type -> type.getId() == messageId)) {
      throw new IllegalArgumentException(String.format("Cannot create a new message type with packetId '%d' because another message type has an equal id assigned.", messageId));
    }

    if (messageTypes.stream().anyMatch(type -> type.getName() == name)) {
      throw new IllegalArgumentException(String.format("Cannot create a new message type with name '%s' because another message type has an equal name.", name));
    }

    this.name = name;
    this.packetId = messageId;

    messageTypes.add(this);
  }

  /**
   * Gets the.
   *
   * @param id
   *          the id
   * @return the message type
   */
  public static MessageType get(final byte id) {
    final Optional<MessageType> optional = messageTypes.stream().filter(m -> m.getId() == id).findAny();
    if (!optional.isPresent()) {
      return MessageType.INVALID;
    }

    return optional.get();
  }

  public static MessageType get(final String name) {
    final Optional<MessageType> optional = messageTypes.stream().filter(m -> m.getName().equals(name)).findAny();
    if (!optional.isPresent()) {
      return MessageType.INVALID;
    }

    return optional.get();
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public byte getId() {
    return this.packetId;
  }

  public String getName() {
    return this.name;
  }
}
