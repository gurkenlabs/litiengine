package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * TODO: Implement spawn event/listener TODO: Implement additional constructors to enhance the API
 */
public abstract class EntitySpawner<T extends IEntity> implements IEntitySpawner<T> {
  private int amount;
  private int interval;
  private long lastSpawnWave;
  private int spawnDelay;
  private SpawnMode spawnMode;
  private List<Spawnpoint> spawnpoints;
  private Function<IEntitySpawner<T>, List<Spawnpoint>> customSpawnpoints;

  public EntitySpawner(final int interval, final int amount) {
    this.spawnDelay = 1000;
    this.interval = interval;
    this.amount = amount;
    this.spawnMode = SpawnMode.CUSTOMSPAWNPOINTS;
  }

  public EntitySpawner(final List<Spawnpoint> spawnpoints, final int interval, final int amount) {
    this(spawnpoints, interval, amount, SpawnMode.ALLSPAWNPOINTS);
  }

  public EntitySpawner(final List<Spawnpoint> spawnpoints, final int amount, SpawnMode spawnMode) {
    this.spawnDelay = 1000;
    this.amount = amount;
    this.spawnpoints = spawnpoints;
    this.spawnMode = spawnMode;
  }

  /**
   * Initializes a new instance of the {@code EntitySpawner} class.
   *
   * @param spawnpoints The spawnpoints from which this instance will choose from when spawning
   *     entities.
   * @param interval The interval in which entities will be spawned.
   * @param amount The amount of entities to spawn on every spawn event.
   * @param spawnMode the spawning behaviour
   */
  public EntitySpawner(
      final List<Spawnpoint> spawnpoints,
      final int interval,
      final int amount,
      SpawnMode spawnMode) {
    this.interval = interval;
    this.spawnDelay = 1000;
    this.amount = amount;
    this.spawnpoints = spawnpoints;
    this.spawnMode = spawnMode;
  }

  public EntitySpawner(
      final int amount, Function<IEntitySpawner<T>, List<Spawnpoint>> spawnpointCallback) {
    Objects.nonNull(spawnpointCallback);

    this.amount = amount;
    this.customSpawnpoints = spawnpointCallback;
    this.spawnMode = SpawnMode.CUSTOMSPAWNPOINTS;
  }

  public EntitySpawner(
      final int interval,
      final int amount,
      Function<IEntitySpawner<T>, List<Spawnpoint>> spawnpointCallback) {
    this(new ArrayList<Spawnpoint>(), interval, amount);
    Objects.nonNull(spawnpointCallback);

    this.customSpawnpoints = spawnpointCallback;
    this.spawnMode = SpawnMode.CUSTOMSPAWNPOINTS;
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
    if (!this.shouldSpawn()) {
      return;
    }

    this.spawnNewEntities();
    this.lastSpawnWave = Game.time().now();
  }

  protected boolean shouldSpawn() {
    return this.lastSpawnWave == 0
        || Game.time().since(this.lastSpawnWave) >= this.getSpawnInterval();
  }

  protected List<Spawnpoint> getCustomSpawnpoints() {
    return new ArrayList<>();
  }

  /**
   * Spawn new entities, depending on the {@code SpawnMode}, spawnAmount, spawnDelay, and
   * spawnInterval of an {@code EntitySpawner}.
   *
   * @see SpawnMode
   */
  protected void spawnNewEntities() {
    if (this.getSpawnMode() != SpawnMode.CUSTOMSPAWNPOINTS && this.getSpawnPoints().isEmpty()) {
      return;
    }

    switch (this.getSpawnMode()) {
      case ALLSPAWNPOINTS:
        for (int i = 0; i < this.getSpawnPoints().size(); i++) {
          final int index = i;
          Game.loop()
              .perform(
                  this.getSpawnDelay() + this.getSpawnDelay() * i,
                  () -> this.spawn(this.getSpawnPoints().get(index), this.getSpawnAmount()));
        }
        break;
      case ONERANDOMSPAWNPOINT:
        this.spawn(Game.random().choose(this.getSpawnPoints()), this.getSpawnAmount());
        break;
      case RANDOMSPAWNPOINTS:
        for (int i = 0; i < this.getSpawnAmount(); i++) {
          Game.loop()
              .perform(
                  this.getSpawnDelay() + this.getSpawnDelay() * i,
                  () -> this.spawn(Game.random().choose(this.getSpawnPoints()), 1));
        }
        break;
      case CUSTOMSPAWNPOINTS:
        List<Spawnpoint> spawnPoints =
            this.customSpawnpoints != null
                ? this.customSpawnpoints.apply(this)
                : this.getCustomSpawnpoints();

        int index = 0;
        for (Spawnpoint spawn : spawnPoints) {
          Game.loop()
              .perform(
                  this.getSpawnDelay() + this.getSpawnDelay() * index, () -> this.spawn(spawn, 1));
          index++;
        }
        break;
      default:
        break;
    }
  }

  private void spawn(final Spawnpoint spawnpoint, final int amount) {
    if (spawnpoint.getEnvironment() == null || !spawnpoint.getEnvironment().isLoaded()) {
      return;
    }
    for (int i = 0; i < amount; i++) {
      final T newEntity = this.createNew();
      spawnpoint.spawn(newEntity);
    }
  }
}
