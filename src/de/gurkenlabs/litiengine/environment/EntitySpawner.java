package de.gurkenlabs.litiengine.environment;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Spawnpoint;

public abstract class EntitySpawner<T extends IEntity> implements IEntitySpawner<T> {
  private static final Logger log = Logger.getLogger(EntitySpawner.class.getName());
  private int amount;
  private Environment environment;
  private int interval;
  private long lastSpawnWave;
  private int spawnDelay;
  private SpawnMode spawnMode;
  private List<Spawnpoint> spawnpoints;

  /**
   * Instantiates a new entity spawner.
   *
   * @param environment
   *          the environment
   * @param loop
   *          the loop
   * @param spawnpoints
   *          the spawnpoints
   * @param interval
   *          the interval
   * @param amount
   *          the amount
   */
  public EntitySpawner(final Environment environment, final IGameLoop loop, final List<Spawnpoint> spawnpoints, final int interval, final int amount) {
    this.environment = environment;
    Game.world().addUnloadedListener(e -> {
      if (e == this.environment) {
        loop.detach(this);
      }
    });
    Game.world().addLoadedListener(e -> {
      if (e == this.environment) {
        loop.attach(this);
      }
    });
    this.interval = interval;
    this.spawnDelay = 1000;
    this.amount = amount;
    this.spawnpoints = spawnpoints;
    this.spawnMode = SpawnMode.ALLSPAWNPOINTS;
    loop.attach(this);
  }

  @Override
  public int getSpawnAmount() {
    return this.amount;
  }

  @Override
  public int getSpawnInterval() {
    return this.interval;
  }

  @Override
  public int getSpawnDelay() {
    return this.spawnDelay;
  }

  @Override
  public SpawnMode getSpawnMode() {
    return this.spawnMode;
  }

  @Override
  public List<Spawnpoint> getSpawnPoints() {
    return this.spawnpoints;
  }

  @Override
  public void setSpawnAmount(final int amount) {
    this.amount = amount;
  }

  @Override
  public void setSpawnInterval(final int interval) {
    this.interval = interval;
  }

  @Override
  public void setSpawnDelay(final int delay) {
    this.spawnDelay = delay;
  }

  @Override
  public void setSpawnMode(final SpawnMode mode) {
    this.spawnMode = mode;
  }

  @Override
  public void update() {
    if (Game.loop().getDeltaTime(this.lastSpawnWave) < this.getSpawnInterval()) {
      return;
    }

    this.spawnNewEntities();
    this.lastSpawnWave = Game.loop().getTicks();
  }

  /**
   * Spawn new entities, depending on the <code>SpawnMode</code>, spawnAmount, spawnDelay, and spawnInterval of an <code>EntitySpawner</code>.
   * 
   * @see SpawnMode
   */
  protected void spawnNewEntities() {
    if (this.getSpawnPoints().isEmpty()) {
      return;
    }

    switch (this.getSpawnMode()) {
    case ALLSPAWNPOINTS:
      for (final Spawnpoint spawn : this.getSpawnPoints()) {
        this.spawn(spawn, this.getSpawnAmount());
      }
      break;
    case ONERANDOMSPAWNPOINT:
      final int rnd = new Random().nextInt(this.getSpawnPoints().size());
      this.spawn(this.getSpawnPoints().get(rnd), this.getSpawnAmount());
      break;
    case RANDOMSPAWNPOINTS:
      for (int i = 0; i < this.getSpawnAmount(); i++) {
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

    /** The amount of entities to spawn. */
    private final int remaining;

    /** The <code>Spawnpoint</code> where entities will be spawned. */
    private final Spawnpoint point;

    /**
     * Instantiates a new spawn thread.
     *
     * @param point
     *          the <code>Spawnpoint</code> where entities will be spawned.
     * @param amount
     *          the amount of entities to spawn.
     */
    public SpawnThread(final Spawnpoint point, final int amount) {
      this.point = point;
      this.remaining = amount;
    }

    @Override
    public void run() {
      for (int i = 0; i < this.remaining; i++) {
        final T newEntity = EntitySpawner.this.createNew();
        newEntity.setLocation(this.point.getLocation());
        newEntity.setMapId(EntitySpawner.this.environment.getNextMapId());
        EntitySpawner.this.environment.add(newEntity);

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