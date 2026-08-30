package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.entities.EntityMessageEvent;
import de.gurkenlabs.litiengine.entities.EntityMessageListener;
import de.gurkenlabs.litiengine.entities.IEntity;
import java.util.Objects;

/// Base class for scripts attached to an entity.
public abstract class EntityScript<T extends IEntity> extends AbstractScript<T> {
  @Override protected final void attached() throws Exception { this.onLoaded(); }
  @Override protected final void detached() throws Exception { this.onUnloaded(); }

  /// Called from the entity's loaded event after its environment is available.
  protected void onLoaded() throws Exception { this.loaded(); }

  /// Called when the entity controller is detached or the entity is removed.
  protected void onUnloaded() throws Exception { this.unloaded(); }

  /// Called for messages delivered to the attached entity.
  protected void onMessage(EntityMessageEvent event) throws Exception {
    this.message(event);
    if (event != null) {
      this.onMessage(event.getMessage(), event.getSource());
    }
  }

  /// Called when a text message is received by the attached entity.
  protected void onMessage(String message, Object sender) throws Exception {}

  /// Called when the attached combat entity is hit.
  protected void onHit(de.gurkenlabs.litiengine.entities.EntityHitEvent event) throws Exception {
    this.onHit();
  }

  /// Called when the attached combat entity is hit.
  protected void onHit() throws Exception {}

  /// Called when the attached combat entity dies.
  protected void onDeath(de.gurkenlabs.litiengine.entities.ICombatEntity entity, de.gurkenlabs.litiengine.entities.EntityHitEvent hitEvent) throws Exception {
    this.onDeath();
  }

  /// Called when the attached combat entity dies.
  protected void onDeath() throws Exception {}

  /// Called when the attached collision entity collides with another collision entity.
  protected void onCollision(de.gurkenlabs.litiengine.physics.CollisionEvent event) throws Exception {
    this.onCollision();
  }

  /// Called when the attached collision entity collides with another collision entity.
  protected void onCollision() throws Exception {}

  /// Called when another entity interacts with the attached entity.
  protected void onInteract(IEntity source) throws Exception {}

  /// Called when an entity action is performed on the attached entity.
  protected void onAction(String action) throws Exception {}

  /// Sends a message from this entity to all of its listeners.
  public void sendMessage(String message) {

    if (this.host() != null) {
      this.host().sendMessage(this.host(), message);
    }
  }

  /// Sends a message from this entity to a target receiver entity.
  public void sendMessage(IEntity receiver, String message) {
    Objects.requireNonNull(receiver, "Receiver entity must not be null.");
    if (this.host() != null) {
      receiver.sendMessage(this.host(), message);
    }
  }

  /// Removes this entity from its current environment.
  public void remove() {
    if (this.host() != null && this.host().getEnvironment() != null) {
      this.host().getEnvironment().remove(this.host());
    }
  }

  /// Checks if the host entity is dead (if it is a combat entity).
  public boolean isDead() {
    return this.host() instanceof de.gurkenlabs.litiengine.entities.ICombatEntity combat && combat.isDead();
  }

  /// Marks the host combat entity as dead.
  public void die() {
    if (this.host() instanceof de.gurkenlabs.litiengine.entities.ICombatEntity combat) {
      combat.die();
    }
  }

  /// Resurrects the host combat entity.
  public void resurrect() {
    if (this.host() instanceof de.gurkenlabs.litiengine.entities.ICombatEntity combat) {
      combat.resurrect();
    }
  }

  /// Returns current health / hitpoints (or 0 if not a combat entity).
  public int getHealth() {
    return this.host() instanceof de.gurkenlabs.litiengine.entities.ICombatEntity combat ? combat.getHitPoints().getModifiedValue() : 0;
  }

  /// Returns maximum health / hitpoints (or 0 if not a combat entity).
  public int getMaxHealth() {
    return this.host() instanceof de.gurkenlabs.litiengine.entities.ICombatEntity combat ? combat.getHitPoints().getMax() : 0;
  }

  /// @deprecated Override [#onLoaded()] in new scripts.
  @Deprecated
  protected void loaded() throws Exception {}

  /// @deprecated Override [#onUnloaded()] in new scripts.
  @Deprecated
  protected void unloaded() throws Exception {}

  /// @deprecated Override [#onMessage(EntityMessageEvent)] or [Object)][#onMessage(String,] in new scripts.
  @Deprecated
  protected void message(EntityMessageEvent event) throws Exception {}

  final void dispatchMessage(EntityMessageEvent event) throws Exception {
    this.onMessage(event);
  }

  final void dispatchHit(de.gurkenlabs.litiengine.entities.EntityHitEvent event) throws Exception {
    this.onHit(event);
  }

  final void dispatchDeath(de.gurkenlabs.litiengine.entities.ICombatEntity entity, de.gurkenlabs.litiengine.entities.EntityHitEvent hitEvent) throws Exception {
    this.onDeath(entity, hitEvent);
  }

  final void dispatchCollision(de.gurkenlabs.litiengine.physics.CollisionEvent event) throws Exception {
    this.onCollision(event);
  }

  final void dispatchInteract(IEntity source) throws Exception {
    this.onInteract(source);
  }

  void dispatchAction(String action) throws Exception {
    this.onAction(action);
  }
}


