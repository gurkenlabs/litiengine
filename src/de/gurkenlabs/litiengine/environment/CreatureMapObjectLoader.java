package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class CreatureMapObjectLoader extends MapObjectLoader {

  protected CreatureMapObjectLoader() {
    super(MapObjectType.CREATURE);
  }

  @Override
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.CREATURE) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CreatureMapObjectLoader.class);
    }

    if (mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME) == null) {
      return super.load(mapObject);
    }

    Creature creature = this.createNewCreature(mapObject, mapObject.getCustomProperty(MapObjectProperty.SPAWN_TYPE));
    loadProperties(creature, mapObject);
    loadCollisionProperties(creature, mapObject);
    creature.setFacingDirection(mapObject.getCustomPropertyEnum(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.RIGHT));
    // TODO: load IMobileEntity and ICombatEntity properties

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(creature);
    return entities;
  }

  protected Creature createNewCreature(IMapObject mapObject, String spawnType) {
    return new Creature(mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME));
  }
}
