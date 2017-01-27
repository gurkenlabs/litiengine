package de.gurkenlabs.litiengine.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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
  private final Collection<Consumer<TriggerEvent>> activatedConsumer;
  private final Collection<Consumer<TriggerEvent>> deactivatedConsumer;
  private final Map<String, String> arguments;
  private final List<Integer> activators;
  private final List<Integer> targets;
  private String message;
  private List<IEntity> activated;
  private final TriggerActivation activationType;
  private final boolean isOneTimeTrigger;

  private boolean triggered;

  private final String name;

  public Trigger(String name, String message) {
    this(TriggerActivation.COLLISION, name, message, false, new ConcurrentHashMap<>());
  }

  public Trigger(TriggerActivation activation, String name, String message, boolean isOneTime, Map<String, String> arguments) {
    this.activatedConsumer = new CopyOnWriteArrayList<>();
    this.deactivatedConsumer = new CopyOnWriteArrayList<>();
    this.arguments = arguments;
    this.activators = new CopyOnWriteArrayList<>();
    this.targets = new CopyOnWriteArrayList<>();
    this.activated = new CopyOnWriteArrayList<>();
    this.name = name;
    this.message = message;
    this.isOneTimeTrigger = isOneTime;
    this.activationType = activation;
  }

  public void addActivator(int mapId) {
    this.activators.add(mapId);
  }

  public void addTarget(int mapId) {
    this.targets.add(mapId);
  }

  public List<Integer> getActivators() {
    return this.activators;
  }

  @Override
  public void update(IGameLoop loop) {
    if (Game.getEnvironment() == null || this.activationType != TriggerActivation.COLLISION) {
      return;
    }

    if (!Game.getPhysicsEngine().collides(this.getCollisionBox(), IPhysicsEngine.COLLTYPE_ENTITY)) {
      return;
    }

    List<IEntity> collEntities = new CopyOnWriteArrayList<>();
    for (ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
      if (this.activators.size() > 0 && !this.activators.contains(coll.getMapId())) {
        continue;
      }

      if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
        collEntities.add(coll);
      }
    }

    for (IEntity ent : collEntities) {
      if (this.activated.contains(ent)) {
        continue;
      }

      this.activate(ent, ent.getMapId());
    }

    // send deactivation event
    for (IEntity ent : this.activated) {
      if (!collEntities.contains(ent)) {
        for (Consumer<TriggerEvent> cons : this.deactivatedConsumer) {
          List<Integer> targets = this.getTargets();
          if (targets.size() == 0) {
            targets = new ArrayList<>();
            targets.add(ent.getMapId());
          }

          cons.accept(new TriggerEvent(this.message, ent, targets, this.arguments));
        }
      }
    }

    this.activated = collEntities;
  }

  @Override
  public String sendMessage(Object sender, final String message) {
    if (this.activationType == TriggerActivation.COLLISION && (sender != null && sender instanceof IEntity) || message == null || message.isEmpty()) {
      return null;
    }

    if (sender instanceof IEntity) {
      IEntity ent = (IEntity) sender;
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

  public void activate(IEntity activator, int tar) {
    if (this.isOneTimeTrigger && this.triggered || this.getActivationType() == TriggerActivation.COLLISION && activator != null && this.activated.contains(activator)) {
      return;
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

    // if we actually have a trigger target, we send the message to the target
    if (targets.size() > 0) {
      for (int target : targets) {
        IEntity entity = Game.getEnvironment().get(target);
        if (entity == null) {
          System.out.println("trigger '" + this.getName() + "' was activated, but the trigger target '" + target + "' could not be found on the environment");
          continue;
        }

        entity.sendMessage(this, this.message);
        this.activated.add(activator);
      }
    }

    final TriggerEvent te = new TriggerEvent(this.message, activator, targets, this.arguments);
    // also send the trigger event to all registered consumers
    for (Consumer<TriggerEvent> cons : this.activatedConsumer) {
      cons.accept(te);
    }

    if (this.isOneTimeTrigger && this.triggered) {
      Game.getEnvironment().remove(this);
    }
  }

  public String getMessage() {
    return this.message;
  }

  public List<Integer> getTargets() {
    return this.targets;
  }

  public String getName() {
    return this.name;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void onActivated(Consumer<TriggerEvent> cons) {
    if (this.activatedConsumer.contains(cons)) {
      return;
    }

    this.activatedConsumer.add(cons);
  }

  public void onDeactivated(Consumer<TriggerEvent> cons) {
    if (this.deactivatedConsumer.contains(cons)) {
      return;
    }

    this.deactivatedConsumer.add(cons);
  }

  public TriggerActivation getActivationType() {
    return activationType;
  }

  @Override
  public String toString() {
    return "trigger: " + this.getName() + "[" + this.getMapId() + "]";
  }
}