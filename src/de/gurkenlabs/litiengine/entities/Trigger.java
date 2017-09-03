package de.gurkenlabs.litiengine.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;

@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class Trigger extends CollisionEntity implements IUpdateable {
  public enum TriggerActivation {
    COLLISION, INTERACT
  }

  public static final String USE_MESSAGE = "use";
  private List<IEntity> activated;
  private final Collection<Consumer<TriggerEvent>> activatedConsumer;
  private final Collection<Function<TriggerEvent, String>> activatingPredicates;
  private final TriggerActivation activationType;
  private final List<Integer> activators;
  private final Map<String, String> arguments;
  private final Collection<Consumer<TriggerEvent>> deactivatedConsumer;
  private final boolean isOneTimeTrigger;
  private String message;
  private final List<Integer> targets;

  private boolean triggered;

  public Trigger(final String name, final String message) {
    this(TriggerActivation.COLLISION, name, message, false, new ConcurrentHashMap<>());
  }

  public Trigger(final TriggerActivation activation, final String name, final String message, final boolean isOneTime, final Map<String, String> arguments) {
    super();
    this.activatingPredicates = new CopyOnWriteArrayList<>();
    this.activatedConsumer = new CopyOnWriteArrayList<>();
    this.deactivatedConsumer = new CopyOnWriteArrayList<>();
    this.arguments = arguments;
    this.activators = new CopyOnWriteArrayList<>();
    this.targets = new CopyOnWriteArrayList<>();
    this.activated = new CopyOnWriteArrayList<>();
    this.setName(name);
    this.message = message;
    this.isOneTimeTrigger = isOneTime;
    this.activationType = activation;
  }

  public boolean activate(final IEntity activator, final int tar) {
    if (this.isOneTimeTrigger && this.triggered || this.getActivationType() == TriggerActivation.COLLISION && activator != null && this.activated.contains(activator)) {
      return false;
    }

    this.triggered = true;
    // always take local targets if there are any
    List<Integer> targets = this.getTargets();
    if (targets.size() == 0) {
      // as a fallback send the message to the tar
      targets = new ArrayList<>();
      if (tar > 0) {
        targets.add(tar);
      }
    }

    final TriggerEvent te = new TriggerEvent(this, activator, targets);

    // check if the trigger is allowed to be activated
    for (Function<TriggerEvent, String> pred : this.activatingPredicates) {
      String result = pred.apply(te);
      if (result != null && !result.isEmpty()) {
        activator.sendMessage(this, result);
        return false;
      }
    }

    // if we actually have a trigger target, we send the message to the target
    if (targets.size() > 0) {
      for (final int target : targets) {
        final IEntity entity = Game.getEnvironment().get(target);
        if (entity == null) {
          System.out.println("trigger '" + this.getName() + "' was activated, but the trigger target '" + target + "' could not be found on the environment");
          continue;
        }

        entity.sendMessage(this, this.message);
        this.activated.add(activator);
      }
    }

    // also send the trigger event to all registered consumers
    for (final Consumer<TriggerEvent> cons : this.activatedConsumer) {
      cons.accept(te);
    }

    if (this.isOneTimeTrigger && this.triggered) {
      Game.getEnvironment().remove(this);
    }

    return true;
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

  public Map<String, String> getArguments() {
    return this.arguments;
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

  /**
   * Allows to register functions that contain additional checks for the trigger
   * activation. The return value of the function is considered the reason why
   * the trigger cannot be activated. If the function returns anything else than
   * null, the activation is cancelled and the result of the function is send to
   * the activator entity.
   * 
   * @param func
   */
  public void onActivating(final Function<TriggerEvent, String> func) {
    if (this.activatingPredicates.contains(func)) {
      return;
    }

    this.activatingPredicates.add(func);
  }

  public void onActivated(final Consumer<TriggerEvent> cons) {
    if (this.activatedConsumer.contains(cons)) {
      return;
    }

    this.activatedConsumer.add(cons);
  }

  public void onDeactivated(final Consumer<TriggerEvent> cons) {
    if (this.deactivatedConsumer.contains(cons)) {
      return;
    }

    this.deactivatedConsumer.add(cons);
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    if (this.activationType == TriggerActivation.COLLISION && sender != null && sender instanceof IEntity || message == null || message.isEmpty()) {
      return null;
    }

    if (sender instanceof IEntity) {
      final IEntity ent = (IEntity) sender;
      // already triggered by the entity
      if (this.activators.contains(ent.getMapId())) {
        return null;
      }

      if (message.equals(USE_MESSAGE)) {
        this.activate(ent, ent.getMapId());
      }
    }

    return null;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "trigger: " + this.getName() + "[" + this.getMapId() + "]";
  }

  @Override
  public void update(final IGameLoop loop) {
    if (Game.getEnvironment() == null || this.activationType != TriggerActivation.COLLISION) {
      return;
    }

    if (!Game.getPhysicsEngine().collides(this.getCollisionBox(), IPhysicsEngine.COLLTYPE_ENTITY)) {
      return;
    }

    final List<IEntity> collEntities = new CopyOnWriteArrayList<>();
    for (final ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
      if (this.activators.size() > 0 && !this.activators.contains(coll.getMapId())) {
        continue;
      }

      if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
        collEntities.add(coll);
      }
    }

    for (final IEntity ent : collEntities) {
      if (this.activated.contains(ent)) {
        continue;
      }

      this.activate(ent, ent.getMapId());
    }

    // send deactivation event
    for (final IEntity ent : this.activated) {
      if (!collEntities.contains(ent)) {
        for (final Consumer<TriggerEvent> cons : this.deactivatedConsumer) {
          List<Integer> targets = this.getTargets();
          if (targets.size() == 0) {
            targets = new ArrayList<>();
            targets.add(ent.getMapId());
          }

          cons.accept(new TriggerEvent(this, ent, targets));
        }
      }
    }

    this.activated = collEntities;
  }
}