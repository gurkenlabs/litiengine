package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntitySpawner<T extends IEntity> extends IUpdateable {
  public int getInterval();

  public void setInterval(int interval);

  public int getAmount();

  public void setAmount(int amount);

  public int getSpawnDelay();

  public void setSpawnDelay(int delay);

  public List<MapLocation> getSpawnPoints();

  public T createNew();

  public SpawnMode getSpawnMode();

  public void setSpawnMode(SpawnMode mode);

  public enum SpawnMode {

    /**
     * Spawns the amount of mobs on one random spawnpoint.
     */
    ONERANDOMSPAWNPOINT,

    /**
     * Spawns the amount of mobs, distributed to random spawnpoints.
     */
    RANDOMSPAWNPOINTS,

    /**
     * Spawns the amount of mobs for all the spawnpoints available.
     */
    ALLSPAWNPOINTS
  }
}
