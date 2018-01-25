package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

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
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + PropMapObjectLoader.class);
    }

    // TODO: make this accessible by child implementations that add create
    // custom Prop implementations
    // set map properties by map object
    final Material material = mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL));
    final Prop prop = this.createNewProp(mapObject, mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME), material);
    prop.setMapId(mapObject.getId());

    prop.setIndestructible(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE));
    prop.setCollision(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION));

    AttributeModifier<Short> mod = new AttributeModifier<>(Modification.SET, mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH));
    prop.getAttributes().getHealth().modifyMaxBaseValue(mod);
    prop.getAttributes().getHealth().modifyBaseValue(mod);

    prop.setCollisionBoxWidth(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH));
    prop.setCollisionBoxHeight(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT));
    prop.setAddShadow(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_ADDSHADOW));

    prop.setCollisionBoxAlign(Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALGIN)));
    prop.setCollisionBoxValign(Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALGIN)));

    if (mapObject.getDimension() != null) {
      prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    }

    prop.setTeam(mapObject.getCustomPropertyInt(MapObjectProperty.TEAM));
    prop.setName(mapObject.getName());

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(prop);
    return entities;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheetName, Material material) {
    Prop prop = new Prop(mapObject.getLocation(), spriteSheetName, material);
    prop.setName(mapObject.getName());
    final String obstacle = mapObject.getCustomProperty(MapObjectProperty.PROP_OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      prop.setObstacle(Boolean.valueOf(obstacle));
    }

    return prop;
  }
}
