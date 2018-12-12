package de.gurkenlabs.litiengine.environment;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Spawnpoint;

// TODO: Auto-generated Javadoc
/**
 * The Class EntitySpawner.
 *
 * @param <T>
 *          the generic type
 */
public abstract class EntitySpawner<T extends IEntity> implements IEntitySpawner<T> {

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(EntitySpawner.class.getName());

  /** The amount. */
  private int amount;

  /** The environment. */
  private IEnvironment environment;

  /** The interval between spawn waves. */
  private int interval;

  /** The time of the last spawn wave. */
  private long lastSpawnWave;

  /** The spawn delay between individual entity spawns of a spawn wave. */
  private int spawnDelay;

  /** The spawn mode. */
  private SpawnMode spawnMode;

  /** The spawnpoints. */
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
  public EntitySpawner(final IEnvironment environment, final IGameLoop loop, final List<Spawnpoint> spawnpoints, final int interval, final int amount) {
    this.environment = environment;
    Game.addEnvironmentUnloadedListener(e -> {
      if (e == this.environment) {
        loop.detach(this);
      }
    });
    Game.addEnvironmentLoadedListener(e -> {
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

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#getSpawnAmount()
   */
  @Override
  public int getSpawnAmount() {
    return this.amount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#getSpawnInterval()
   */
  @Override
  public int getSpawnInterval() {
    return this.interval;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#getSpawnDelay()
   */
  @Override
  public int getSpawnDelay() {
    return this.spawnDelay;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#getSpawnMode()
   */
  @Override
  public SpawnMode getSpawnMode() {
    return this.spawnMode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#getSpawnPoints()
   */
  @Override
  public List<Spawnpoint> getSpawnPoints() {
    return this.spawnpoints;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#setSpawnAmount(int)
   */
  @Override
  public void setSpawnAmount(final int amount) {
    this.amount = amount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#setSpawnInterval(int)
   */
  @Override
  public void setSpawnInterval(final int interval) {
    this.interval = interval;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#setSpawnDelay(int)
   */
  @Override
  public void setSpawnDelay(final int delay) {
    this.spawnDelay = delay;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.environment.IEntitySpawner#setSpawnMode(de.gurkenlabs.litiengine.environment.IEntitySpawner.SpawnMode)
   */
  @Override
  public void setSpawnMode(final SpawnMode mode) {
    this.spawnMode = mode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.gurkenlabs.litiengine.IUpdateable#update()
   */
  @Override
  public void update() {
    if (Game.getLoop().getDeltaTime(this.lastSpawnWave) < this.getSpawnInterval()) {
      return;
    }

    this.spawnNewEntities();
    this.lastSpawnWave = Game.getLoop().getTicks();
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

  /**
   * Spawn.
   *
   * @param spawnpoint
   *          the spawnpoint
   * @param amount
   *          the amount
   */
  private void spawn(final Spawnpoint spawnpoint, final int amount) {
    new SpawnThread(spawnpoint, amount).start();
  }

  /**
   * The Class SpawnThread.
   */
  private class SpawnThread extends Thread {

    /** The amount of entities to spawn. */
    private final int amount;

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
      this.amount = amount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      for (int i = 0; i < this.amount; i++) {
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