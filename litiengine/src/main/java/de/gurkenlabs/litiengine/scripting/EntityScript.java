package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.entities.EntityMessageEvent;
import de.gurkenlabs.litiengine.entities.IEntity;

/** Base class for scripts attached to an entity. */
public abstract class EntityScript<T extends IEntity> extends AbstractScript<T> {
  @Override protected final void attached() throws Exception { this.onLoaded(); }
  @Override protected final void detached() throws Exception { this.onUnloaded(); }

  /** Called from the entity's loaded event after its environment is available. */
  protected void onLoaded() throws Exception { this.loaded(); }

  /** Called when the entity controller is detached or the entity is removed. */
  protected void onUnloaded() throws Exception { this.unloaded(); }

  /** Called for messages delivered to the attached entity. */
  protected void onMessage(EntityMessageEvent event) throws Exception { this.message(event); }

  /** @deprecated Override {@link #onLoaded()} in new scripts. */
  @Deprecated
  protected void loaded() throws Exception {}

  /** @deprecated Override {@link #onUnloaded()} in new scripts. */
  @Deprecated
  protected void unloaded() throws Exception {}

  /** @deprecated Override {@link #onMessage(EntityMessageEvent)} in new scripts. */
  @Deprecated
  protected void message(EntityMessageEvent event) throws Exception {}

  final void dispatchMessage(EntityMessageEvent event) throws Exception {
    this.onMessage(event);
  }
}
