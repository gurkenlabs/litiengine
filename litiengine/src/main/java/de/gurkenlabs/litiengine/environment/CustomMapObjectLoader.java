package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code CustomMapObjectLoader} class extends the {@link MapObjectLoader} class to provide custom functionality for loading map objects into
 * entities.
 *
 * <p>This class uses a functional interface {@code ConstructorInvocation} to dynamically
 * invoke constructors of entity classes based on the provided {@link IMapObject} and {@link Environment}. It supports various constructor signatures
 * and prioritizes them based on their parameter types.
 *
 * <p>It also overrides the {@code load} method to handle the creation and initialization
 * of entities from map objects.
 */
public final class CustomMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CustomMapObjectLoader.class.getName());
  private final ConstructorInvocation invoke;

  /**
   * A functional interface for invoking constructors of entity classes.
   *
   * <p>This interface defines a single method {@code invoke}, which dynamically
   * creates an instance of an {@link IEntity} using the provided {@link Environment} and {@link IMapObject}.
   */
  @FunctionalInterface
  interface ConstructorInvocation {
    IEntity invoke(Environment environment, IMapObject mapObject) throws InvocationTargetException, IllegalAccessException, InstantiationException;
  }

  /**
   * Constructs a new {@code CustomMapObjectLoader} instance with the specified map object type and constructor invocation logic.
   *
   * <p>This constructor initializes the loader with a specific map object type and a functional
   * interface for dynamically invoking constructors of entity classes.
   *
   * @param mapObjectType the type of the map object to be handled by this loader
   * @param invocation    the {@link ConstructorInvocation} functional interface used to create entities
   */
  CustomMapObjectLoader(String mapObjectType, ConstructorInvocation invocation) {
    super(mapObjectType);
    this.invoke = invocation;
  }

  /**
   * Finds the most suitable constructor for the specified entity type.
   *
   * <p>This method iterates through all constructors of the given entity class and determines
   * the best match based on the parameter types. It prioritizes constructors in the following order:
   * <ol>
   *   <li>Constructor with parameters {@link Environment} and {@link IMapObject}</li>
   *   <li>Constructor with parameters {@link IMapObject} and {@link Environment}</li>
   *   <li>Constructor with a single {@link IMapObject} parameter</li>
   *   <li>Constructor with a single {@link Environment} parameter</li>
   *   <li>Default (no-argument) constructor</li>
   * </ol>
   *
   * <p>If multiple constructors match, the one with the highest priority is selected.
   *
   * @param entityType the class of the entity for which a constructor is to be found
   * @return a {@link ConstructorInvocation} functional interface for invoking the selected constructor, or {@code null} if no suitable constructor is
   * uctor is found
   */
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
    } catch (ReflectiveOperationException _) {
      log.log(Level.SEVERE, "map object {} failed to load", mapObject.getId());
      return entities;
    }

    loadDefaultProperties(entity, mapObject);

    entities.add(entity);
    return entities;
  }
}
