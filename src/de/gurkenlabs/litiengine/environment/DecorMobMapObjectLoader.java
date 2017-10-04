package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehavior;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class DecorMobMapObjectLoader extends MapObjectLoader {

  public DecorMobMapObjectLoader() {
    super(MapObjectType.DECORMOB);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.DECORMOB) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + DecorMobMapObjectLoader.class);
    }

    if (mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME) == null) {
      throw new IllegalArgumentException("A DecorMob requires a " + MapObjectProperties.SPRITESHEETNAME + " to be set.");
    }

    // TODO: this is a very weird default value
    short velocity = (short) (100 / Game.getInfo().getDefaultRenderScale());
    if (mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY) != null) {
      velocity = Short.parseShort(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY));
    }

    final DecorMob mob = new DecorMob(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), MovementBehavior.get(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_BEHAVIOUR)), velocity);

    if (mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      mob.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }

    mob.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH) != null) {
      mob.setCollisionBoxWidth(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT) != null) {
      mob.setCollisionBoxHeight(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)));
    }

    mob.setCollisionBoxAlign(Align.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)));
    mob.setCollisionBoxValign(Valign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)));
    mob.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    mob.setMapId(mapObject.getId());
    mob.setName(mapObject.getName());

    return mob;
  }
}
