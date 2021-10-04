package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

/**
 * This interface provides methods that are required for loading an {@code IEntity} from an {@code IMapObject}.
 * It separates the actual entity implementation from the loading process and provide a place to implement all the logic
 * to load attributes and initialize logic based on static information from the {@code IMap}.
 * <br>
 * <br>
 * The engine provides default implementations for all predefined {@code Entity} types (e.g. {@code Prop or Creature}).
 * You can inherit/call the abstract {@code MapObjectLoader} implementation to make use of predefined loading logic.
 *
 * @see Environment#registerMapObjectLoader(IMapObjectLoader)
 * @see MapObjectLoader#loadDefaultProperties(IEntity, IMapObject)
 */
public interface IMapObjectLoader {
  String getMapObjectType();

  Collection<IEntity> load(Environment environment, IMapObject mapObject);

  /**
   * This method is called externally on the loader instance after the entities have been loaded.
   *
   * @param entities  The loaded entities.
   * @param mapObject The map object by which the entities have been loaded.
   */
  void afterLoad(Collection<IEntity> entities, IMapObject mapObject);
}
