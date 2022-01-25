package de.gurkenlabs.litiengine.entities;

import java.io.Serial;
import java.util.EventObject;
import java.util.List;

public class TriggerEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 3624707673365488289L;

  private final transient IEntity entity;
  private final transient String message;
  private final transient List<Integer> targets;
  private final transient Trigger trigger;

  TriggerEvent(final Trigger trigger, final IEntity entity, final List<Integer> targets) {
    super(trigger);
    this.trigger = trigger;
    this.message = trigger.getMessage();
    this.targets = targets;
    this.entity = entity;
  }

  /**
   * Get the entity that activated the Trigger.
   *
   * @return The entity that activated the trigger.
   */
  public IEntity getEntity() {
    return this.entity;
  }

  /**
   * Get this Trigger's message.
   *
   * @return The trigger's message.
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Get the entities that are affected by the Trigger.
   *
   * @return Target entities of the trigger.
   */
  public List<Integer> getTargets() {
    return this.targets;
  }

  /**
   * Get the Trigger affected by this event.
   *
   * @return The trigger.
   */
  public Trigger getTrigger() {
    return trigger;
  }
}
