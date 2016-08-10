package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntitySpawner<T extends IEntity> extends IUpdateable {
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

  public T createNew();

  public int getAmount();

  public int getInterval();

  public int getSpawnDelay();

  public SpawnMode getSpawnMode();

  public List<MapLocation> getSpawnPoints();

  public void setAmount(int amount);

  public void setInterval(int interval);

  public void setSpawnDelay(int delay);

  public void setSpawnMode(SpawnMode mode);
}
