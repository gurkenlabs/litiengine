package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class PropMapObjectLoader extends MapObjectLoader {

  public PropMapObjectLoader() {
    super(MapObjectType.PROP);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + PropMapObjectLoader.class);
    }

    // set map properties by map object
    final Material material = mapObject.getCustomProperty(MapObjectProperties.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL));
    final Prop prop = this.createNewProp(mapObject, mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), material);
    prop.setMapId(mapObject.getId());

    if (mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      prop.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.HEALTH) != null) {
      prop.getAttributes().getHealth().modifyMaxBaseValue(new AttributeModifier<>(Modification.SET, Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.HEALTH))));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISION) != null) {
      prop.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH) != null) {
      prop.setCollisionBoxWidth(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT) != null) {
      prop.setCollisionBoxHeight(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)));
    }

    prop.setCollisionBoxAlign(Align.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)));
    prop.setCollisionBoxValign(Valign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)));

    if (mapObject.getDimension() != null) {
      prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    }

    if (mapObject.getCustomProperty(MapObjectProperties.TEAM) != null) {
      prop.setTeam(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.TEAM)));
    }

    prop.setName(mapObject.getName());
    return prop;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheetName, Material material) {
    Prop prop = new Prop(mapObject.getLocation(), spriteSheetName, material);
    final String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      prop.setObstacle(Boolean.valueOf(obstacle));
    }

    return prop;
  }
}
