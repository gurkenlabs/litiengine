package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

/**
 * This interface provides methods that are required for loading an <code>IEntity</code> from an <code>IMapObject</code>.
 * It separates the actual entity implementation from the loading process and provide a place to implement all the logic
 * to load attributes and initialize logic based on static information from the <code>IMap</code>.
 * <br>
 * <br>
 * The engine provides default implementations for all predefined <code>Entity</code> types (e.g. <code>Prop or Creature</code>).
 * You can inherit/call the abstract <code>MapObjectLoader</code> implementation to make use of predefined loading logic.
 * 
 * @see Environment#registerMapObjectLoader(IMapObjectLoader)
 * @see MapObjectLoader#loadDefaultProperties(IEntity, IMapObject)
 */
public interface IMapObjectLoader {
  String getMapObjectType();

  Collection<IEntity> load(Environment environment, IMapObject mapObject) throws MapObjectException;
}
