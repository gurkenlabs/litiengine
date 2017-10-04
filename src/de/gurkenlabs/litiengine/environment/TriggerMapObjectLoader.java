package de.gurkenlabs.litiengine.environment;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;
import de.gurkenlabs.util.ArrayUtilities;

public class TriggerMapObjectLoader extends MapObjectLoader {

  protected TriggerMapObjectLoader() {
    super(MapObjectType.TRIGGER);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.TRIGGER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + TriggerMapObjectLoader.class);
    }

    final String message = mapObject.getCustomProperty(MapObjectProperties.TRIGGERMESSAGE);

    final TriggerActivation act = mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATION) != null ? TriggerActivation.valueOf(mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATION)) : TriggerActivation.COLLISION;
    final String targets = mapObject.getCustomProperty(MapObjectProperties.TRIGGERTARGETS);
    final String activators = mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATORS);
    final String oneTime = mapObject.getCustomProperty(MapObjectProperties.TRIGGERONETIME);
    final boolean oneTimeBool = oneTime != null && !oneTime.isEmpty() ? Boolean.valueOf(oneTime) : false;

    final Map<String, String> triggerArguments = new HashMap<>();
    for (final Property prop : mapObject.getAllCustomProperties()) {
      if (MapObjectProperties.isCustom(prop.getName())) {
        triggerArguments.put(prop.getName(), prop.getValue());
      }
    }

    final Trigger trigger = new Trigger(act, mapObject.getName(), message, oneTimeBool, triggerArguments);

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

    trigger.setMapId(mapObject.getId());
    trigger.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    trigger.setCollisionBoxHeight(trigger.getHeight());
    trigger.setCollisionBoxWidth(trigger.getWidth());
    trigger.setLocation(new Point2D.Double(mapObject.getLocation().x, mapObject.getLocation().y));

    return trigger;
  }
}
