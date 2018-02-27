package de.gurkenlabs.litiengine.environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class TriggerMapObjectLoader extends MapObjectLoader {

  protected TriggerMapObjectLoader() {
    super(MapObjectType.TRIGGER);
  }

  @Override
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.TRIGGER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + TriggerMapObjectLoader.class);
    }

    final String message = mapObject.getCustomProperty(MapObjectProperty.TRIGGER_MESSAGE);

    final TriggerActivation act = mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATION) != null ? TriggerActivation.valueOf(mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATION)) : TriggerActivation.COLLISION;
    final String targets = mapObject.getCustomProperty(MapObjectProperty.TRIGGER_TARGETS);
    final String activators = mapObject.getCustomProperty(MapObjectProperty.TRIGGER_ACTIVATORS);

    final Map<String, String> triggerArguments = new HashMap<>();
    for (final Property prop : mapObject.getAllCustomProperties()) {
      if (MapObjectProperty.isCustom(prop.getName())) {
        triggerArguments.put(prop.getName(), prop.getValue());
      }
    }

    final Trigger trigger = new Trigger(act, message, mapObject.getCustomPropertyBool(MapObjectProperty.TRIGGER_ONETIME), triggerArguments);
    this.loadProperties(trigger, mapObject);
    
    for (final int target : ArrayUtilities.getIntegerArray(targets)) {
      if (target != 0) {
        trigger.addTarget(target);
      }
    }

    for (final int activator : ArrayUtilities.getIntegerArray(activators)) {
      if (activator != 0) {
        trigger.addActivator(activator);
      }
    }

    trigger.setCooldown(mapObject.getCustomPropertyInt(MapObjectProperty.TRIGGER_COOLDOWN));

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(trigger);
    return entities;
  }
}
