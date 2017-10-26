package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

public class PropMapObjectLoader extends MapObjectLoader {

  protected PropMapObjectLoader() {
    super(MapObjectType.PROP);
  }

  @Override
  public IEntity load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + PropMapObjectLoader.class);
    }

    // set map properties by map object
    final Material material = mapObject.getCustomProperty(MapObjectProperty.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperty.MATERIAL));
    final Prop prop = this.createNewProp(mapObject, mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME), material);
    prop.setMapId(mapObject.getId());

    if (mapObject.getCustomProperty(MapObjectProperty.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperty.INDESTRUCTIBLE).isEmpty()) {
      prop.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperty.INDESTRUCTIBLE)));
    }

    if (mapObject.getCustomProperty(MapObjectProperty.HEALTH) != null) {
      prop.getAttributes().getHealth().modifyMaxBaseValue(new AttributeModifier<>(Modification.SET, Integer.parseInt(mapObject.getCustomProperty(MapObjectProperty.HEALTH))));
    }

    if (mapObject.getCustomProperty(MapObjectProperty.COLLISION) != null) {
      prop.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperty.COLLISION)));
    }

    if (mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH) != null) {
      prop.setCollisionBoxWidth(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXWIDTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT) != null) {
      prop.setCollisionBoxHeight(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperty.COLLISIONBOXHEIGHT)));
    }

    if (mapObject.getCustomProperty(MapObjectProperty.PROP_ADDSHADOW) != null) {
      prop.setAddShadow(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_ADDSHADOW)));
    }

    prop.setCollisionBoxAlign(Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISIONALGIN)));
    prop.setCollisionBoxValign(Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISIONVALGIN)));

    if (mapObject.getDimension() != null) {
      prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    }

    if (mapObject.getCustomProperty(MapObjectProperty.TEAM) != null) {
      prop.setTeam(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperty.TEAM)));
    }

    prop.setName(mapObject.getName());
    return prop;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheetName, Material material) {
    Prop prop = new Prop(mapObject.getLocation(), spriteSheetName, material);
    final String obstacle = mapObject.getCustomProperty(MapObjectProperty.OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      prop.setObstacle(Boolean.valueOf(obstacle));
    }

    return prop;
  }
}
