package de.gurkenlabs.litiengine.environment;

import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Spawnpoint;

public interface IEntitySpawner<T extends IEntity> extends IUpdateable {

  /**
   * SpawnMode specifies the behaviour of the EntitySpawner:
   * <ul>
   * <li><b>ALLSPAWNPOINTS</b>: the specified spawnAmount is spawned at each of the SpawnPoints individually</li>
   * <li><b>ONERANDOMSPAWNPOINT</b>: the specified spawnAmount is spawned at one random SpawnPoint</li>
   * <li><b>RANDOMSPAWNPOINTS</b>: the specified spawnAmount is distributed equally to all of the SpawnPoints</li>
   * </ul>
   */
  enum SpawnMode {

    /**
     * Spawns the amount of mobs for all the spawnpoints available.
     */
    ALLSPAWNPOINTS,

    /**
     * Spawns the amount of mobs on one random spawnpoint.
     */
    ONERANDOMSPAWNPOINT,

    /**
     * Spawns the amount of mobs, distributed to random spawnpoints.
     */
    RANDOMSPAWNPOINTS,

    CUSTOMSPAWNPOINTS,
  }

  /**
   * Creates a new instance of the provided Entity type.
   *
   * @return the Entity instance which will be spawned
   */
  T createNew();

  /**
   * Gets the amount of Entities that are spawned in each wave.
   *
   * @return the spawn amount
   */
  int getSpawnAmount();

  /**
   * Gets the interval between spawn waves.
   *
   * @return the spawn interval
   */
  int getSpawnInterval();

  /**
   * Gets the delay between spawning individual Entities of one wave.
   *
   * @return the spawn delay
   */
  int getSpawnDelay();

  /**
   * Gets the spawn mode for an EntitySpawner.
   *
   * @see SpawnMode
   * @return the spawn mode
   */
  SpawnMode getSpawnMode();

  /**
   * Gets the list of SpawnPoints that a EntitySpawner uses.
   *
   * @return the spawn points
   */
  List<Spawnpoint> getSpawnPoints();

  /**
   * Sets the amount of Entities that spawn in each wave.
   *
   * @param amount
   *          the new amount
   */
  void setSpawnAmount(int amount);

  /**
   * Sets the interval in milliseconds between each spawn wave.
   *
   * @param interval
   *          the new interval
   */
  void setSpawnInterval(int interval);

  /**
   * Gets the delay in milliseconds between spawning individual Entities of one wave.
   *
   * @param delay
   *          the new spawn delay
   */
  void setSpawnDelay(int delay);

  /**
   * Sets the spawn mode.
   * 
   * @param mode
   *          the new spawn mode
   * @see SpawnMode
   */
  void setSpawnMode(SpawnMode mode);
}
