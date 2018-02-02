package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  /***
   * TODO 04.10.2017: refactor this implementation because the hard coded emitter
   * types are not a proper approach.
   */
  @Override
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + EmitterMapObjectLoader.class);
    }

    // TODO: provide EmitterData instance from the mapobject properties
    EmitterData data = new EmitterData();
    CustomEmitter emitter = new CustomEmitter(mapObject.getLocation().getX(), mapObject.getLocation().getY(), data);

    emitter.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    emitter.setLocation(mapObject.getLocation());
    emitter.setMapId(mapObject.getId());
    emitter.setName(mapObject.getName());

    Collection<IEntity> entities = super.load(mapObject);
    if (emitter != null) {
      entities.add(emitter);
    }
    return entities;
  }

}
