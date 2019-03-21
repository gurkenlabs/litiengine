package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public final class CustomMapObjectLoader extends MapObjectLoader {
  private final ConstructorInvocation invoke;

  @FunctionalInterface
  private interface ConstructorInvocation {
    IEntity invoke(Environment environment, IMapObject mapObject) throws InvocationTargetException, IllegalAccessException, InstantiationException;
  }

  protected CustomMapObjectLoader(String mapObjectType, Class<? extends IEntity> entityType) {
    super(mapObjectType);
    if (entityType.isInterface() || Modifier.isAbstract(entityType.getModifiers())) {
      throw new IllegalArgumentException("cannot create loader for interface or abstract class");
    }
    ConstructorInvocation inv = null;
    Constructor<?>[] constructors = entityType.getConstructors();
    int priority = 0; // env+mo, mo+env, mo, env, nullary
    for (int i = 0; i < constructors.length; i++) {
      final Constructor<?> constructor = constructors[i];
      Class<?>[] classes = constructor.getParameterTypes();
      if (classes.length == 2) {
        if (classes[0] == Environment.class && classes[1] == IMapObject.class) {
          inv = (e, o) -> (IEntity) constructor.newInstance(e, o);
          break; // exit early because we've already found the highest priority constructor
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

    if (inv == null) {
      throw new IllegalArgumentException("could not find suitable constructor");
    }

    this.invoke = inv;
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) throws MapObjectException {
    IEntity entity;
    try {
      if (environment != null) {
        mapObject.setId(Game.world().environment().getNextMapId());
      }
      entity = invoke.invoke(environment, mapObject);

    } catch (ReflectiveOperationException e) {
      throw new MapObjectException(e);
    }

    loadDefaultProperties(entity, mapObject);

    return Arrays.asList(entity);
  }
}
