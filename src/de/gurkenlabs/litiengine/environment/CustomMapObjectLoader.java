package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public final class CustomMapObjectLoader<T extends IEntity> extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CustomMapObjectLoader.class.getName());

  private final Class<T> entityType;

  protected CustomMapObjectLoader(String mapObjectType, Class<T> entityType) {
    super(mapObjectType);
    this.entityType = entityType;
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    T entity = null;
    try {
      // check for constructor with Environment and IMapObject parameter
      Constructor<T> constructor = entityType.getConstructor(Environment.class, IMapObject.class);
      entity = constructor.newInstance(environment, mapObject);
    } catch (Exception e1) {
      try {
        // check for constructor with IMapObject parameter
        Constructor<T> constructor = entityType.getConstructor(IMapObject.class);
        entity = constructor.newInstance(mapObject);
      } catch (Exception e2) {
        try {
          // check for empty constructor
          Constructor<T> constructor = entityType.getConstructor();
          entity = constructor.newInstance();
        } catch (Exception e3) {
          log.log(Level.SEVERE, "Could not create an entity from a mapobject of type " + this.getMapObjectType() + ". Are you missing a matching constructor?", e3);
          return new ArrayList<>();
        }
      }
    }

    loadDefaultProperties(entity, mapObject);
    return Arrays.asList(entity);
  }
}
