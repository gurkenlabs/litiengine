package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

public class CombatEntityEvent extends EventObject {
  private static final long serialVersionUID = -7106624526186875471L;

  private final transient ICombatEntity entity;

  public CombatEntityEvent(ICombatEntity combatEntity) {
    super(combatEntity);
    this.entity = combatEntity;
  }

  public ICombatEntity getEntity() {
    return this.entity;
  }
}
