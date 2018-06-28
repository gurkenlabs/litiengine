package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public final class CustomMapObjectLoader extends MapObjectLoader {
  private final ConstructorInvocation invoke;

  @FunctionalInterface
  private interface ConstructorInvocation {
    IEntity invoke(IEnvironment environment, IMapObject mapObject) throws InvocationTargetException, IllegalAccessException, InstantiationException;
  }

  protected CustomMapObjectLoader(String mapObjectType, Class<? extends IEntity> entityType) {
    super(mapObjectType);
    if (entityType.isInterface())
      throw new IllegalArgumentException("Cannot create loader for interface or abstract class");
    ConstructorInvocation invoke;
    try {
      final Constructor<? extends IEntity> constructor = entityType.getConstructor(IEnvironment.class, IEnvironment.class);
      invoke = (e, o) -> constructor.newInstance(e, o);
    } catch (NoSuchMethodException e1) {
      try {
        final Constructor<? extends IEntity> constructor = entityType.getConstructor(IMapObject.class, IEnvironment.class);
        invoke = (e, o) -> constructor.newInstance(o, e);
      } catch (NoSuchMethodException e2) {
        try {
          final Constructor<? extends IEntity> constructor = entityType.getConstructor(IMapObject.class);
          invoke = (e, o) -> constructor.newInstance(o);
        } catch (NoSuchMethodException e3) {
          try {
            final Constructor<? extends IEntity> constructor = entityType.getConstructor(IEnvironment.class);
            invoke = (e, o) -> constructor.newInstance(e);
          } catch (NoSuchMethodException e4) {
            try {
              final Constructor<? extends IEntity> constructor = entityType.getConstructor();
              invoke = (e, o) -> constructor.newInstance();
            } catch (NoSuchMethodException e5) {
              throw new IllegalArgumentException("Entity class is missing a usable constructor");
            }
          }
        }
      }
    }
    this.invoke = invoke;
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    IEntity entity;
    try {
      entity = invoke.invoke(environment, mapObject);
    } catch (InvocationTargetException e) {
      // propagate the exception
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException)
        throw (RuntimeException)cause;
      if (cause instanceof Error)
        throw (Error)cause;
      
      throw new MapObjectException(cause);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new MapObjectException(e); // we shouldn't be getting these here
    }

    loadDefaultProperties(entity, mapObject);
    if (entity instanceof ICollisionEntity)
      loadCollisionProperties((ICollisionEntity)entity, mapObject);
    return Arrays.asList(entity);
  }
}
