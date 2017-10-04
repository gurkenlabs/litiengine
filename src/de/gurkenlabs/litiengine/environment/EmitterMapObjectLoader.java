package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.FireEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.ShimmerEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;

public class EmitterMapObjectLoader extends MapObjectLoader {

  public EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  /***
   * TODO 04.10.2017: refactor this implementation because the hard coded
   * emitter types are not a proper approach.
   */
  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + EmitterMapObjectLoader.class);
    }

    Emitter emitter;
    final String emitterType = mapObject.getCustomProperty(MapObjectProperties.EMITTERTYPE);
    if (emitterType == null || emitterType.isEmpty()) {
      return null;
    }

    switch (emitterType) {
    case "fire":
      emitter = new FireEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      break;
    case "shimmer":
      emitter = new ShimmerEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      break;
    default:
      emitter = null;
      break;
    }

    // try to load custom emitter
    if (emitter == null && emitterType.endsWith(".xml")) {
      emitter = new CustomEmitter(mapObject.getLocation().x, mapObject.getLocation().y, emitterType);
    }

    if (emitter != null) {
      emitter.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
      emitter.setMapId(mapObject.getId());
      emitter.setName(mapObject.getName());

      return emitter;
    }

    return null;
  }

}
