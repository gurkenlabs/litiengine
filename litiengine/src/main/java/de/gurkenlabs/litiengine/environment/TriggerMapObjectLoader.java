package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class TriggerMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(TriggerMapObjectLoader.class.getName());
  private static final Map<String, Class<? extends Trigger>> customTriggerTypes = new ConcurrentHashMap<>();

  protected TriggerMapObjectLoader() {
    super(MapObjectType.TRIGGER);
  }

  /**
   * <p>
   * Registers a custom {@link Trigger} implementation that can be automatically provided by this
   * {@link MapObjectLoader}.
   * </p>
   *
   * <p>
   * Whenever a {@link MapObjectType#TRIGGER TRIGGER} map object with a {@link IMapObject#getName() name} equal to the
   * specified {@code name} is loaded, an instance of the supplied {@code triggerType} is created instead of the default
   * {@link Trigger}.
   * </p>
   *
   * <p>
   * Make sure that the implementation has one of the following constructors present:
   * <ol>
   * <li>A constructor that matches the {@link Trigger} default constructor signature
   * ({@link TriggerActivation}, {@link String}, {@code boolean}, {@code int}).</li>
   * <li>An empty constructor.</li>
   * </ol>
   *
   * @param <T>         The type of the custom trigger implementation.
   * @param name        The map object name used to identify trigger instances that should be loaded as the specified
   *                    {@code triggerType}.
   * @param triggerType The class of the custom {@link Trigger} implementation.
   */
  public static <T extends Trigger> void registerCustomTriggerType(String name, Class<T> triggerType) {
    if (name == null || name.isEmpty() || triggerType == null) {
      return;
    }

    customTriggerTypes.put(name.toLowerCase(), triggerType);
  }

  /**
   * Removes all previously registered custom {@link Trigger} types.
   *
   * @see #registerCustomTriggerType(String, Class)
   */
  public static void clearCustomTriggerTypes() {
    customTriggerTypes.clear();
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final String message = mapObject.getStringValue(MapObjectProperty.TRIGGER_MESSAGE, null);
    final TriggerActivation act = mapObject.getEnumValue(MapObjectProperty.TRIGGER_ACTIVATION, TriggerActivation.class, TriggerActivation.COLLISION);
    final boolean oneTime = mapObject.getBoolValue(MapObjectProperty.TRIGGER_ONETIME, false);
    final int coolDown = mapObject.getIntValue(MapObjectProperty.TRIGGER_COOLDOWN, 0);

    final Trigger trigger = this.createTrigger(mapObject, act, message, oneTime, coolDown);
    loadDefaultProperties(trigger, mapObject);
    this.loadTargets(mapObject, trigger);
    this.loadActivators(mapObject, trigger);

    entities.add(trigger);
    return entities;
  }

  protected Trigger createTrigger(IMapObject mapObject, TriggerActivation act, String message, boolean oneTime, int coolDown) {
    final String name = mapObject.getName();
    if (name != null && !name.isEmpty()) {
      Class<? extends Trigger> customTriggerType = customTriggerTypes.get(name.toLowerCase());
      if (customTriggerType != null) {
        Trigger custom = createCustomTrigger(customTriggerType, act, message, oneTime, coolDown);
        if (custom != null) {
          return custom;
        }
      }
    }

    return new Trigger(act, message, oneTime, coolDown);
  }

  private static Trigger createCustomTrigger(Class<? extends Trigger> customTrigger, TriggerActivation act, String message, boolean oneTime,
      int coolDown) {
    try {
      return customTrigger.getConstructor(TriggerActivation.class, String.class, boolean.class, int.class)
          .newInstance(act, message, oneTime, coolDown);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      try {
        return customTrigger.getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
          | SecurityException ex) {
        log.log(Level.WARNING, "Could not automatically create trigger of type {0} because a matching constructor is missing.",
            new Object[] {customTrigger});
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    return null;
  }

  protected void loadTargets(IMapObject mapObject, Trigger trigger) {
    final String targets = mapObject.getStringValue(MapObjectProperty.TRIGGER_TARGETS, null);
    if (targets == null) {
      return;
    }

    for (final int target : ArrayUtilities.splitInt(targets)) {
      if (target != 0) {
        trigger.addTarget(target);
      }
    }
  }

  protected void loadActivators(IMapObject mapObject, Trigger trigger) {
    final String activators = mapObject.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS, null);
    if (activators == null) {
      return;
    }

    for (final int activator : ArrayUtilities.splitInt(activators)) {
      if (activator != 0) {
        trigger.addActivator(activator);
      }
    }
  }
}
