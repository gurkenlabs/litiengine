package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class TriggerMapObjectLoader extends MapObjectLoader {

  protected TriggerMapObjectLoader() {
    super(MapObjectType.TRIGGER);
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
    return new Trigger(act, message, oneTime, coolDown);
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
