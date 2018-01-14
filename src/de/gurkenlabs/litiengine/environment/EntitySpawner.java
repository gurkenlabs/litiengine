package de.gurkenlabs.litiengine.environment;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;

public abstract class EntitySpawner<T extends IEntity> implements IEntitySpawner<T> {
  private static final Logger log = Logger.getLogger(EntitySpawner.class.getName());
  private int amount;
  private IEnvironment environment;
  private int interval;
  private long lastSpawn;
  private int spawnDelay;

  private SpawnMode spawnMode;

  private List<Spawnpoint> spawnpoints;

  public EntitySpawner(final IEnvironment environment, final IGameLoop loop, final List<Spawnpoint> spawnpoints, final int interval, final int amount) {
    this.environment = environment;
    this.interval = interval;
    this.spawnDelay = 1000;
    this.amount = amount;
    this.spawnpoints = spawnpoints;
    loop.attach(this);
  }

  @Override
  public int getAmount() {
    return this.amount;
  }

  @Override
  public int getInterval() {
    return this.interval;
  }

  @Override
  public int getSpawnDelay() {
    return this.spawnDelay;
  }

  @Override
  public IEntitySpawner.SpawnMode getSpawnMode() {
    return this.spawnMode;
  }

  @Override
  public List<Spawnpoint> getSpawnPoints() {
    return this.spawnpoints;
  }

  @Override
  public void setAmount(final int amount) {
    this.amount = amount;
  }

  @Override
  public void setInterval(final int interval) {
    this.interval = interval;
  }

  @Override
  public void setSpawnDelay(final int delay) {
    this.spawnDelay = delay;
  }

  @Override
  public void setSpawnMode(final IEntitySpawner.SpawnMode mode) {
    this.spawnMode = mode;
  }

  @Override
  public void update() {
    if (Game.getLoop().getDeltaTime(this.lastSpawn) < this.getInterval()) {
      return;
    }

    this.spawnNewEntities();
    this.lastSpawn = Game.getLoop().getTicks();
  }

  protected abstract void addToEnvironment(final IEnvironment env, T newEntity);

  protected void spawnNewEntities() {
    if (this.getSpawnPoints().isEmpty()) {
      return;
    }

    switch (this.getSpawnMode()) {
    case ALLSPAWNPOINTS:
      for (final Spawnpoint spawn : this.getSpawnPoints()) {
        this.spawn(spawn, this.getAmount());
      }
      break;
    case ONERANDOMSPAWNPOINT:
      final int rnd = new Random().nextInt(this.getSpawnPoints().size());
      this.spawn(this.getSpawnPoints().get(rnd), this.getAmount());
      break;
    case RANDOMSPAWNPOINTS:
      for (int i = 0; i < this.getAmount(); i++) {
        final int rnd2 = new Random().nextInt(this.getSpawnPoints().size());
        this.spawn(this.getSpawnPoints().get(rnd2), 1);
      }

      break;
    default:
      break;
    }
  }

  private void spawn(final Spawnpoint spawnpoint, final int amount) {
    new SpawnThread(spawnpoint, amount).start();
  }

  private class SpawnThread extends Thread {
    private final int amount;
    private final Spawnpoint point;

    public SpawnThread(final Spawnpoint point, final int amount) {
      this.point = point;
      this.amount = amount;
    }

    @Override
    public void run() {
      for (int i = 0; i < this.amount; i++) {
        final T newEntity = EntitySpawner.this.createNew();
        newEntity.setLocation(this.point.getLocation());
        newEntity.setMapId(EntitySpawner.this.environment.getNextMapId());
        EntitySpawner.this.addToEnvironment(EntitySpawner.this.environment, newEntity);

        try {
          Thread.sleep(EntitySpawner.this.getSpawnDelay());
        } catch (final InterruptedException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
          this.interrupt();
        }
      }
    }
  }

}