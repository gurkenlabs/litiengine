package de.gurkenlabs.litiengine.environment;

import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;

public interface IEntitySpawner<T extends IEntity> extends IUpdateable {
  public enum SpawnMode {

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
    RANDOMSPAWNPOINTS
  }

  public T createNew();

  public int getAmount();

  public int getInterval();

  public int getSpawnDelay();

  public SpawnMode getSpawnMode();

  public List<Spawnpoint> getSpawnPoints();

  public void setAmount(int amount);

  public void setInterval(int interval);

  public void setSpawnDelay(int delay);

  public void setSpawnMode(SpawnMode mode);
}
