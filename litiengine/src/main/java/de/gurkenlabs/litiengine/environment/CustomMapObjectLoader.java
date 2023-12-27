package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CustomMapObjectLoader.class.getName());
  private final ConstructorInvocation invoke;

  @FunctionalInterface
  interface ConstructorInvocation {
    IEntity invoke(Environment environment, IMapObject mapObject) throws InvocationTargetException, IllegalAccessException, InstantiationException;
  }

  CustomMapObjectLoader(String mapObjectType, ConstructorInvocation invocation) {
    super(mapObjectType);
    this.invoke = invocation;
  }

  static ConstructorInvocation findConstructor(Class<? extends IEntity> entityType) {
    ConstructorInvocation inv = null;

    int priority = 0; // env+mo, mo+env, mo, env, nullary
    for (final Constructor<?> constructor : entityType.getConstructors()) {
      Class<?>[] classes = constructor.getParameterTypes();
      if (classes.length == 2) {
        if (classes[0] == Environment.class && classes[1] == IMapObject.class) {
          return (e, o) -> (IEntity) constructor.newInstance(e, o); // exit early because we've already found the highest priority constructor
        } else if (classes[0] == IMapObject.class && classes[1] == Environment.class) {
          inv = (e, o) -> (IEntity) constructor.newInstance(o, e);
          priority = 3;
        }
      } else if (classes.length == 1) {
        if (priority < 3) {
          if (classes[0] == IMapObject.class) {
            inv = (e, o) -> (IEntity) constructor.newInstance(o);
            priority = 2;
          } else if (priority < 2 && classes[0] == Environment.class) {
            inv = (e, o) -> (IEntity) constructor.newInstance(e);
            priority = 1;
          }
        }
      } else if (classes.length == 0 && priority < 1) {
        inv = (e, o) -> (IEntity) constructor.newInstance();
        // priority is already 0
      }
    }

    return inv;
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    IEntity entity;
    try {
      entity = invoke.invoke(environment, mapObject);
    } catch (ReflectiveOperationException e) {
      log.log(Level.SEVERE, "map object {} failed to load", mapObject.getId());
      return entities;
    }

    loadDefaultProperties(entity, mapObject);

    entities.add(entity);
    return entities;
  }
}
