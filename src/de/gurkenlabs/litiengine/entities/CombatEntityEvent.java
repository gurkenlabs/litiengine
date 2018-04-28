package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

public class CombatEntityEvent extends EventObject {
  private static final long serialVersionUID = -7106624526186875471L;

  private final transient ICombatEntity entity;

  public CombatEntityEvent(Object source, ICombatEntity combatEntity) {
    super(source);
    this.entity = combatEntity;
  }

  public ICombatEntity getEntity() {
    return this.entity;
  }
}
