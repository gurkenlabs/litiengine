package de.gurkenlabs.litiengine.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

/**
 * TODO: Triggers should be able to call entity actions (similar to the current message approach)
 */
@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
@TmxType(MapObjectType.TRIGGER)
public class Trigger extends CollisionEntity implements IUpdateable {
  public enum TriggerActivation {
    COLLISION, INTERACT
  }

  private static final Logger log = Logger.getLogger(Trigger.class.getName());

  private final Collection<TriggerActivatedListener> activatedListeners = ConcurrentHashMap.newKeySet();
  private final Collection<TriggerDeactivatedListener> deactivatedListeners = ConcurrentHashMap.newKeySet();
  private final Collection<TriggerActivatingCondition> activatingConditions = ConcurrentHashMap.newKeySet();

  private final Collection<IEntity> collisionActivated = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.TRIGGER_ACTIVATORS)
  private final List<Integer> activators = new CopyOnWriteArrayList<>();

  @TmxProperty(name = MapObjectProperty.TRIGGER_TARGETS)
  private final List<Integer> targets = new CopyOnWriteArrayList<>();

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

  /**
   * Instantiates a new <code>Trigger</code> entity.
   *
   * @param activation
   *          The activation method for this trigger.
   * @param message
   *          The message that gets sent by this trigger upon activation.
   */
  public Trigger(final TriggerActivation activation, final String message) {
    this(activation, null, message);
  }

  /**
   * Instantiates a new <code>Trigger</code> entity.
   *
   * @param activation
   *          The activation method for this trigger.
   * @param name
   *          The name of this trigger.
   * @param message
   *          The message that gets sent by this trigger upon activation.
   */
  public Trigger(final TriggerActivation activation, final String name, final String message) {
    this(activation, name, message, false);
  }

  /**
   * Instantiates a new <code>Trigger</code> entity.
   *
   * @param activation
   *          The activation method for this trigger.
   * @param message
   *          The message that gets sent by this trigger upon activation.
   * @param isOneTime
   *          A flag, indicating whether this instance can only be triggered once.
   */
  public Trigger(final TriggerActivation activation, final String message, final boolean isOneTime) {
    this.message = message;
    this.isOneTimeTrigger = isOneTime;
    this.activationType = activation;
  }

  /**
   * Instantiates a new <code>Trigger</code> entity.
   *
   * @param activation
   *          The activation method for this trigger.
   * @param name
   *          The name of this trigger.
   * @param message
   *          The message that gets sent by this trigger upon activation.
   * @param isOneTime
   *          A flag, indicating whether this instance can only be triggered once.
   */
  public Trigger(final TriggerActivation activation, final String name, final String message, final boolean isOneTime) {
    this(activation, message, isOneTime);
    this.setName(name);
  }

  /**
   * Initializes a new instance of the <code>Trigger</code> class.
   *
   * @param activation
   *          The activation method for this trigger.
   * @param message
   *          The message that gets sent by this trigger upon activation.
   * @param isOneTime
   *          A flag, indicating whether this instance can only be triggered once.
   * @param cooldown
   *          The cooldown that needs to be respected between two activation events.
   */
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

  public void addActivator(final IEntity activator) {
    this.activators.add(activator.getMapId());
  }

  public void addTarget(final int mapId) {
    this.targets.add(mapId);
  }

  public void addTarget(final IEntity target) {
    this.targets.add(target.getMapId());
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

  public boolean interact(final IEntity sender) {
    if (this.activationType == TriggerActivation.COLLISION || sender == null) {
      return false;
    }

    if (this.activators.isEmpty() || this.activators.contains(sender.getMapId())) {
      return this.activate(sender, sender.getMapId());
    } else {
      log.log(Level.FINE, "[{1}] tried to activate trigger [{0}] but was not allowed so because it was not on the list of activators", new Object[] { this, sender.getMapId() });
      return false;
    }
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
    if (Game.world().environment() == null || !this.isLoaded() || this.activationType != TriggerActivation.COLLISION) {
      return;
    }

    final List<IEntity> collEntities = this.getEntitiesInCollisionBox();
    for (final IEntity ent : collEntities) {
      if (this.collisionActivated.contains(ent)) {
        continue;
      }

      this.activate(ent, ent.getMapId());
    }

    // send deactivation event
    Iterator<IEntity> iter = this.collisionActivated.iterator();
    while (iter.hasNext()) {
      IEntity ent = iter.next();
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

        iter.remove();
      }
    }
  }

  private boolean activate(final IEntity activator, final int tar) {
    if (!this.isLoaded() || this.isOneTimeTrigger && this.isActivated || this.getActivationType() == TriggerActivation.COLLISION && activator != null && this.collisionActivated.contains(activator)) {
      return false;
    }

    if (this.getCooldown() > 0 && Game.time().since(this.lastActivation) < this.getCooldown()) {
      return false;
    }

    List<Integer> triggerTargets = this.getTargets(tar);

    final TriggerEvent te = new TriggerEvent(this, activator, triggerTargets);

    if (!this.checkActivationPredicates(te)) {
      return false;
    }

    this.isActivated = true;
    if (activator != null) {
      this.collisionActivated.add(activator);
    }

    // if we actually have a trigger target, we send the message to the target
    for (final int target : triggerTargets) {
      final IEntity entity = this.getEnvironment().get(target);
      if (entity == null) {
        log.log(Level.WARNING, "trigger [{0}] was activated, but the trigger target [{1}] could not be found on the environment", new Object[] { this, target });
        continue;
      }

      entity.sendMessage(this, this.message);
    }

    // also send the trigger event to all registered consumers
    for (final TriggerActivatedListener listener : this.activatedListeners) {
      listener.activated(te);
    }

    if (this.isOneTimeTrigger) {
      this.getEnvironment().remove(this);
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
        log.log(Level.FINE, "trigger [{0}] was not activated. Reason: [{1}])", new Object[] { this, result });
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