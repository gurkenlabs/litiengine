package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public abstract class SoundEngine implements ISoundEngine, IUpdateable {
  private static final int UPDATE_RATE = 200;

  protected static String getIdentifier(final IEntity entity, final Sound sound) {
    return "entity-" + entity.getMapId() + "-" + sound.getName();
  }

  protected static String getIdentifier(final Point2D location, final Sound sound) {
    return "location-" + location.getX() + ", " + location.getY() + "-" + sound.getName();
  }

  private final List<Playback> playbacks;
  private final List<Predicate<IEntity>> entityPlayConditions;
  private final List<Predicate<Point2D>> playConditions;

  private float gain;
  private Point2D listenerPosition;

  private float maxListenerRadius;

  private long lastUpdate;

  public SoundEngine() {
    this.playbacks = new CopyOnWriteArrayList<>();
    this.entityPlayConditions = new CopyOnWriteArrayList<>();
    this.playConditions = new CopyOnWriteArrayList<>();
  }

  protected void add(final Playback playBack) {
    if (this.playbacks.contains(playBack)) {
      return;
    }

    this.playbacks.add(playBack);
  }

  @Override
  public void addEntityPlayCondition(final Predicate<IEntity> predicate) {
    this.entityPlayConditions.add(predicate);
  }

  @Override
  public void addPlayCondition(final Predicate<Point2D> predicate) {
    this.playConditions.add(predicate);
  }

  protected boolean canPlay(final IEntity entity) {
    for (final Predicate<IEntity> condition : this.entityPlayConditions) {
      if (!condition.test(entity)) {
        return false;
      }
    }

    return true;
  }

  protected boolean canPlay(final Point2D location) {
    for (final Predicate<Point2D> condition : this.playConditions) {
      if (!condition.test(location)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public float getGain() {
    return this.gain;
  }

  public Point2D getListenerPosition() {
    return this.listenerPosition;
  }

  public float getMaxListenerRadius() {
    return this.maxListenerRadius;
  }

  @Override
  public void init(final float gain) {
    this.listenerPosition = Game.getScreenManager().getCamera().getFocus();
    this.setGain(gain);
  }

  @Override
  public void setGain(final float volume) {
    this.gain = volume;
  }

  @Override
  public void setMaxRadius(final float radius) {
    this.maxListenerRadius = radius;
  }

  @Override
  public void start() {
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void terminate() {
    Game.getLoop().unregisterFromUpdate(this);
  }

  @Override
  public void update(final IGameLoop gameLoop) {
    if (gameLoop.getDeltaTime(this.lastUpdate) < UPDATE_RATE) {
      return;
    }

    SoundController.callIgnoreTimeout(engine -> {
      // update listener
      this.listenerPosition = Game.getScreenManager().getCamera().getFocus();
      this.updateListenerPosition(this.getListenerPosition());
      // update playing sounds position
      final List<Playback> finished = new ArrayList<>();
      for (final Playback playback : this.playbacks) {
        if (this.isPlaying(playback.getName())) {
          if (this.maxListenerRadius != 0 && playback.getEntity() != null && playback.getEntity().getLocation().distance(this.getListenerPosition()) > this.maxListenerRadius) {
            this.updatePosition(playback.getName(), new Point2D.Double(10000, 10000));
          } else if (playback.getEntity() != null && playback.getEntity().getLocation().distance(this.getListenerPosition()) > 5) {
            this.updatePosition(playback.getName(), playback.getEntity().getDimensionCenter());
          } else {
            this.updatePosition(playback.getName(), this.getListenerPosition());
          }
        } else {
          finished.add(playback);
        }
      }

      // clean up finished playbacks
      finished.forEach(x -> this.playbacks.remove(x));
    }, true);

    this.lastUpdate = gameLoop.getTicks();
  }
}
