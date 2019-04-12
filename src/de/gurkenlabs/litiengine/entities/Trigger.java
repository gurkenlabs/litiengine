package de.gurkenlabs.litiengine.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class Trigger extends CollisionEntity implements IUpdateable {
  public enum TriggerActivation {
    COLLISION, INTERACT
  }

  public static final String INTERACT_MESSAGE = "interact";
  private static final Logger log = Logger.getLogger(Trigger.class.getName());

  private List<IEntity> activated;
  private final Collection<TriggerActivatedListener> activatedListeners;
  private final Collection<TriggerDeactivatedListener> deactivatedListeners;
  private final Collection<TriggerActivatingCondition> activatingConditions;

  @TmxProperty(name = MapObjectProperty.TRIGGER_ACTIVATORS)
  private final List<Integer> activators;

  @TmxProperty(name = MapObjectProperty.TRIGGER_TARGETS)
  private final List<Integer> targets;

  @TmxProperty(name = MapObjectProperty.TRIGGER_ACTIVATION)
  private final TriggerActivation activationType;

  @TmxProperty(name = MapObjectProperty.TRIGGER_ONETIME)
  private final boolean isOneTimeTrigger;

  @TmxProperty(name = MapObjectProperty.TRIGGER_MESSAGE)
  private String message;

  @TmxProperty(name = MapObjectProperty.TRIGGER_COOLDOWN)
  private int cooldown;

  private long lastActivation;
  private boolean isActivated;

  public Trigger(final TriggerActivation activation, final String message) {
    this(activation, null, message);
  }

  public Trigger(final TriggerActivation activation, final String name, final String message) {
    this(activation, name, message, false);
  }

  public Trigger(final TriggerActivation activation, final String message, final boolean isOneTime) {
    super();
    this.activatingConditions = new CopyOnWriteArrayList<>();
    this.activatedListeners = new CopyOnWriteArrayList<>();
    this.deactivatedListeners = new CopyOnWriteArrayList<>();
    this.activators = new CopyOnWriteArrayList<>();
    this.targets = new CopyOnWriteArrayList<>();
    this.activated = new CopyOnWriteArrayList<>();
    this.message = message;
    this.isOneTimeTrigger = isOneTime;
    this.activationType = activation;
  }

  public Trigger(final TriggerActivation activation, final String name, final String message, final boolean isOneTime) {
    this(activation, message, isOneTime);
    this.setName(name);
  }

  public Trigger(final TriggerActivation activation, final String message, final boolean isOneTime, final int cooldown) {
    this(activation, message, isOneTime);
    this.setCooldown(cooldown);
  }

  public void addTriggerListener(TriggerListener listener) {
    this.activatedListeners.add(listener);
    this.activatingConditions.add(listener);
    this.deactivatedListeners.add(listener);
  }

  public void removeTriggerListener(TriggerListener listener) {
    this.activatedListeners.remove(listener);
    this.activatingConditions.remove(listener);
    this.deactivatedListeners.remove(listener);
  }

  public void addActivatedListener(TriggerActivatedListener listener) {
    this.activatedListeners.add(listener);
  }

  public void removeActivatedListener(TriggerActivatedListener listener) {
    this.activatedListeners.remove(listener);
  }

  public void addActivatingCondition(TriggerActivatingCondition condition) {
    this.activatingConditions.add(condition);
  }

  public void removeActivatingCondition(TriggerActivatingCondition condition) {
    this.activatingConditions.remove(condition);
  }

  public void addDeactivatedListener(TriggerDeactivatedListener listener) {
    this.deactivatedListeners.add(listener);
  }

  public void removeDeactivatedListener(TriggerDeactivatedListener listener) {
    this.deactivatedListeners.remove(listener);
  }

  public void addActivator(final int mapId) {
    this.activators.add(mapId);
  }

  public void addTarget(final int mapId) {
    this.targets.add(mapId);
  }

  public TriggerActivation getActivationType() {
    return this.activationType;
  }

  public List<Integer> getActivators() {
    return this.activators;
  }

  public String getMessage() {
    return this.message;
  }

  public List<Integer> getTargets() {
    return this.targets;
  }

  public int getCooldown() {
    return this.cooldown;
  }

  /**
   * Checks whether the specified entity can interact with this trigger.
   * 
   * @param entity
   *          The entity.
   * @return True if the entity can interact with the trigger; otherwise false.
   */
  public boolean canTrigger(ICollisionEntity entity) {
    return entity.canCollideWith(this) && GeometricUtilities.intersects(this.getCollisionBox(), entity.getCollisionBox());
  }

  public boolean isOneTimeTrigger() {
    return this.isOneTimeTrigger;
  }

  public boolean isActivated() {
    return this.isActivated;
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    if (this.activationType == TriggerActivation.COLLISION && sender != null && sender instanceof IEntity || message == null || message.isEmpty()) {
      return Boolean.toString(false);
    }

    if (!message.equals(INTERACT_MESSAGE)) {
      return Boolean.toString(false);
    }

    if (sender instanceof IEntity) {
      final IEntity ent = (IEntity) sender;

      if (this.activators.isEmpty() || this.activators.contains(ent.getMapId())) {
        this.activate(ent, ent.getMapId());
        return Boolean.toString(true);
      } else {
        log.log(Level.INFO, "[{1}] tried to activate trigger [{0}] but was not allowed so because it was not on the list of activators", new Object[] { this.getName(), ent.getMapId() });
        return Boolean.toString(false);
      }
    }

    return super.sendMessage(sender, message);
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public void setHeight(final double height) {
    this.setCollisionBoxHeight(height);
    super.setHeight(height);
  }

  @Override
  public void setWidth(final double width) {
    this.setCollisionBoxWidth(width);
    super.setWidth(width);
  }

  @Override
  public void setSize(final double width, final double height) {
    this.setCollisionBoxWidth(width);
    this.setCollisionBoxHeight(height);
    super.setSize(width, height);
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  @Override
  public void update() {
    if (Game.world().environment() == null || this.activationType != TriggerActivation.COLLISION) {
      return;
    }

    final List<IEntity> collEntities = this.getEntitiesInCollisionBox();
    for (final IEntity ent : collEntities) {
      if (this.activated.contains(ent)) {
        continue;
      }

      this.activate(ent, ent.getMapId());
    }

    // send deactivation event
    for (final IEntity ent : this.activated) {
      if (!collEntities.contains(ent)) {
        List<Integer> triggerTargets = this.getTargets();
        if (triggerTargets.isEmpty()) {
          triggerTargets = new ArrayList<>();
          triggerTargets.add(ent.getMapId());
        }

        final TriggerEvent event = new TriggerEvent(this, ent, triggerTargets);
        for (final TriggerDeactivatedListener listener : this.deactivatedListeners) {
          listener.deactivated(event);
        }
      }
    }

    this.activated = collEntities;
  }

  private boolean activate(final IEntity activator, final int tar) {
    if (this.isOneTimeTrigger && this.isActivated || this.getActivationType() == TriggerActivation.COLLISION && activator != null && this.activated.contains(activator)) {
      return false;
    }

    if (this.cooldown > 0 && Game.time().since(this.lastActivation) < this.cooldown) {
      return false;
    }

    List<Integer> triggerTargets = this.getTargets(tar);

    final TriggerEvent te = new TriggerEvent(this, activator, triggerTargets);

    if (!this.checkActivationPredicates(te)) {
      return false;
    }

    this.isActivated = true;

    // if we actually have a trigger target, we send the message to the target
    for (final int target : triggerTargets) {
      final IEntity entity = Game.world().environment().get(target);
      if (entity == null) {
        log.log(Level.WARNING, "trigger [{0}] was activated, but the trigger target [{1}] could not be found on the environment", new Object[] { this.getName(), target });
        continue;
      }

      entity.sendMessage(this, this.message);
      this.activated.add(activator);
    }

    // also send the trigger event to all registered consumers
    for (final TriggerActivatedListener listener : this.activatedListeners) {
      listener.activated(te);
    }

    if (this.isOneTimeTrigger && this.isActivated) {
      Game.world().environment().remove(this);
    }

    this.lastActivation = Game.time().now();
    return true;
  }

  private boolean checkActivationPredicates(TriggerEvent te) {
    // check if the trigger is allowed to be activated
    for (TriggerActivatingCondition condition : this.activatingConditions) {
      String result = condition.canActivate(te);
      if (result != null && !result.isEmpty()) {
        te.getEntity().sendMessage(this, result);
        return false;
      }
    }

    return true;
  }

  private List<IEntity> getEntitiesInCollisionBox() {
    final List<IEntity> collEntities = new CopyOnWriteArrayList<>();
    for (final ICollisionEntity coll : Game.physics().getCollisionEntities()) {
      if (coll == this || !this.activators.isEmpty() && !this.activators.contains(coll.getMapId())) {
        continue;
      }

      if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
        collEntities.add(coll);
      }
    }

    return collEntities;
  }

  private List<Integer> getTargets(int optionalTarget) {
    // always take local targets if there are any
    List<Integer> localTargets = this.getTargets();
    if (localTargets.isEmpty()) {

      // as a fall back send the message to the tar
      localTargets = new ArrayList<>();
      if (optionalTarget > 0) {
        localTargets.add(optionalTarget);
      }
    }

    return localTargets;
  }
}