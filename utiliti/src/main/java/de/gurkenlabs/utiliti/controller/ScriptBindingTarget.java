package de.gurkenlabs.utiliti.controller;

/** Identifies one persistent scope to which scripts can be attached in utiLITI. */
public sealed interface ScriptBindingTarget {
  record Game() implements ScriptBindingTarget {}

  record Environment(String mapName) implements ScriptBindingTarget {
    public Environment {
      if (mapName == null || mapName.isBlank()) throw new IllegalArgumentException("Map name must not be blank.");
    }
  }

  record EntityType(String type) implements ScriptBindingTarget {
    public EntityType {
      if (type == null || type.isBlank()) throw new IllegalArgumentException("Entity type must not be blank.");
    }
  }

  record EntityInstance(String mapName, int entityId) implements ScriptBindingTarget {
    public EntityInstance {
      if (mapName == null || mapName.isBlank()) throw new IllegalArgumentException("Map name must not be blank.");
    }
  }
}
