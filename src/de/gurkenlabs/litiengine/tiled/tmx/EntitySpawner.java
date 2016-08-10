package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.List;
import java.util.Random;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;

public abstract class EntitySpawner<T extends IEntity> implements IEntitySpawner<T> {
  private class SpawnThread extends Thread {
    private final MapLocation point;
    private final int amount;

    public SpawnThread(final MapLocation point, final int amount) {
      this.point = point;
      this.amount = amount;
    }

    @Override
    public void run() {
      for (int i = 0; i < this.amount; i++) {
        final T newEntity = EntitySpawner.this.createNew();
        newEntity.setLocation(this.point.getPoint());
        newEntity.setMapId(EntitySpawner.this.environment.getMapId());
        EntitySpawner.this.addToEnvironment(EntitySpawner.this.environment, newEntity);

        try {
          Thread.sleep(EntitySpawner.this.getSpawnDelay());
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private List<MapLocation> spawnpoints;
  private IEnvironment environment;
  private int interval;
  private int spawnDelay;
  private int amount;

  private SpawnMode spawnMode;

  private long lastSpawn;

  public EntitySpawner(final IEnvironment environment, final IGameLoop loop, final List<MapLocation> spawnpoints, final int interval, final int amount) {
    this(environment, interval, amount);
    this.spawnpoints = spawnpoints;
    loop.registerForUpdate(this);
  }

  private EntitySpawner(final IEnvironment environment, final int interval, final int amount) {
    this.environment = environment;
    this.interval = interval;
    this.spawnDelay = 1000;
    this.amount = amount;
  }

  protected abstract void addToEnvironment(final IEnvironment env, T newEntity);

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
  public List<MapLocation> getSpawnPoints() {
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

  private void spawn(final MapLocation spawnpoint, final int amount) {
    new SpawnThread(spawnpoint, amount).start();
  }

  protected void spawnNewEntities() {
    if (this.getSpawnPoints().size() == 0) {
      return;
    }

    switch (this.getSpawnMode()) {
    case ALLSPAWNPOINTS:
      for (final MapLocation spawn : this.getSpawnPoints()) {
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
    }
  }

  @Override
  public void update(final IGameLoop loop) {
    if (loop.getDeltaTime(this.lastSpawn) < this.getInterval()) {
      return;
    }

    this.spawnNewEntities();
    this.lastSpawn = loop.getTicks();
  }
}