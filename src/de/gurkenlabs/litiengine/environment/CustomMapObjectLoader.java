package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
    if (entityType.isInterface() || Modifier.isAbstract(entityType.getModifiers())) {
      throw new IllegalArgumentException("cannot create loader for interface or abstract class");
    }
    ConstructorInvocation invoke = null;
    Constructor<?>[] constructors = entityType.getConstructors();
    int priority = 0; // env+mo, mo+env, mo, env, nullary
    for (int i = 0; i < constructors.length; i++) {
      final Constructor<?> constructor = constructors[i];
      Class<?>[] classes = constructor.getParameterTypes();
      if (classes.length == 2) {
        if (classes[0] == IEnvironment.class && classes[1] == IMapObject.class) {
          invoke = (e, o) -> (IEntity) constructor.newInstance(e, o);
          break; // exit early because we've already found the highest priority constructor
        } else if (classes[0] == IMapObject.class && classes[1] == IEnvironment.class) {
          invoke = (e, o) -> (IEntity) constructor.newInstance(o, e);
          priority = 3;
        }
      } else if (classes.length == 1) {
        if (priority < 3) {
          if (classes[0] == IMapObject.class) {
            invoke = (e, o) -> (IEntity) constructor.newInstance(o);
            priority = 2;
          } else if (priority < 2 && classes[0] == IEnvironment.class) {
            invoke = (e, o) -> (IEntity) constructor.newInstance(e);
            priority = 1;
          }
        }
      } else if (classes.length == 0 && priority < 1) {
        invoke = (e, o) -> (IEntity) constructor.newInstance();
        // priority is already 0
      }
    }
    if (invoke == null) {
      throw new IllegalArgumentException("could not find suitable constructor");
    }
    this.invoke = invoke;
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) throws MapObjectException {
    IEntity entity;
    try {
      entity = invoke.invoke(environment, mapObject);
    } catch (ReflectiveOperationException e) {
      throw new MapObjectException(e);
    }

    loadDefaultProperties(entity, mapObject);
    if (entity instanceof ICollisionEntity)
      loadCollisionProperties((ICollisionEntity)entity, mapObject);
    return Arrays.asList(entity);
  }
}
