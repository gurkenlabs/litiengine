package de.gurkenlabs.litiengine.entities;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class Trigger extends CollisionEntity implements IUpdateable {
  public static final String TOGGLE_MESSAGE = "toggle";
  private final Collection<Consumer<TriggerEvent>> activatedConsumer;
  private final Collection<Consumer<TriggerEvent>> deactivatedConsumer;
  private String message;
  private int target;
  private List<IEntity> activated;

  private final String name;

  public Trigger(String name, String message) {
    this.activatedConsumer = new CopyOnWriteArrayList<>();
    this.deactivatedConsumer = new CopyOnWriteArrayList<>();
    this.activated = new CopyOnWriteArrayList<>();
    this.name = name;
    this.message = message;
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void update(IGameLoop loop) {
    List<IEntity> collEntities = new CopyOnWriteArrayList<>();
    for (ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
      if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
        collEntities.add(coll);
      }
    }

    for(IEntity ent : collEntities){
      if(this.activated.contains(ent)){
        continue;
      }
      
      this.activate(ent, ent.getMapId());
    }
    
    // send deactivation event
    for(IEntity ent : this.activated){
      if(!collEntities.contains(ent)){
        for(Consumer<TriggerEvent> cons : this.deactivatedConsumer){
          cons.accept(new TriggerEvent(this.message, ent, this.target != 0 ? this.target : ent.getMapId()));
        }
      }
    }
    
    this.activated = collEntities;
  }

  @Override
  public String sendMessage(int sender, final String message) {
    if (message == null || message.isEmpty()) {
      return null;
    }

    if (message.equals(TOGGLE_MESSAGE)) {
      this.activate(Game.getEnvironment().get(sender), sender);
    }

    return null;
  }

  public void activate(IEntity activator, int tar) {
    if (this.activated.contains(activator) || this.getTarget() == 0 && tar == 0) {
      return;
    }

    // always take local target if it is set
    int t = this.getTarget() == 0 ? tar : this.getTarget();
    IEntity entity = Game.getEnvironment().get(t);
    if (entity == null) {
      return;
    }

    for (Consumer<TriggerEvent> cons : this.activatedConsumer) {
      cons.accept(new TriggerEvent(this.message, activator, tar));
    }

    entity.sendMessage(this.getMapId(), this.message);
    this.activated.add(activator);
  }

  public void activate() {
    this.activate(Game.getEnvironment().get(this.getMapId()), this.target);
  }

  public String getMessage() {
    return this.message;
  }

  public int getTarget() {
    return this.target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  public String getName() {
    return this.name;
  }

  public void setMessage(String message){
    this.message = message;
  }
  
  public void onActivated(Consumer<TriggerEvent> cons) {
    this.activatedConsumer.add(cons);
  }
  public void onDeactivated(Consumer<TriggerEvent> cons) {
    this.deactivatedConsumer.add(cons);
  }
}