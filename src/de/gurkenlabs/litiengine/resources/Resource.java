package de.gurkenlabs.litiengine.resources;

import java.util.Comparator;

public interface Resource {
  public static final Comparator<Resource> BY_NAME = Comparator.nullsFirst(Comparator.comparing(Resource::getName, Comparator.nullsFirst(Comparator.naturalOrder())));

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

  public void setName(String name);
}
