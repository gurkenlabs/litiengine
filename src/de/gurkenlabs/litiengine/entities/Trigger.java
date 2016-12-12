package de.gurkenlabs.litiengine.entities;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderType;

@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class Trigger extends CollisionEntity implements IUpdateable {
  private final Collection<Consumer<String>> triggeredConsumer;
  private final String message;
  private int target;
  private final TriggerActivation activation;
  private boolean oneTime;
  private boolean activated;

  public Trigger(String message, TriggerActivation activation) {
    this.triggeredConsumer = new CopyOnWriteArrayList<>();
    this.message = message;
    this.activation = activation;
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void update(IGameLoop loop) {
    switch (this.activation) {
    case COLLISION:
      ICollisionEntity collEntity = null;
      for (ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
        if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
          collEntity = coll;
          break;
        }
      }

      if (collEntity != null) {
        if (this.getTarget() == 0) {
          this.target = collEntity.getMapId();
        }

        this.activate();
      } else {
        this.activated = false;
      }

      break;
    default:
      break;
    }
  }

  public void activate() {
    if (this.activated || this.target == 0) {
      return;
    }

    IEntity entity = Game.getEnvironment().get(this.target);
    if (entity == null) {
      return;
    }

    for (Consumer<String> cons : this.triggeredConsumer) {
      cons.accept(this.message);
    }

    entity.sendMessage(this.getMapId(), this.message);
    this.activated = true;
  }

  public String getMessage() {
    return this.message;
  }

  public int getTarget() {
    return this.target;
  }

  public TriggerActivation getActivation() {
    return this.activation;
  }

  public boolean isOneTime() {
    return oneTime;
  }

  public void setOneTime(boolean oneTime) {
    this.oneTime = oneTime;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  public void onTriggered(Consumer<String> cons) {
    this.triggeredConsumer.add(cons);
  }
}
