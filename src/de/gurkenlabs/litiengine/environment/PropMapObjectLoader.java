package de.gurkenlabs.litiengine.environment;

import java.util.Collection;

import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
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

    final Material material = mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL));
    final Prop prop = this.createNewProp(mapObject, mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME), material);
    this.loadProperties(prop, mapObject);
    this.loadCollisionProperties(prop, mapObject);
    
    final Rotation rotation = mapObject.getCustomProperty(MapObjectProperty.PROP_ROTATION) == null ? Rotation.NONE : Rotation.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_ROTATION));
    prop.setSpriteRotation(rotation);

    prop.setIndestructible(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE));

    AttributeModifier<Short> mod = new AttributeModifier<>(Modification.SET, mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH));
    prop.getAttributes().getHealth().modifyMaxBaseValue(mod);
    prop.getAttributes().getHealth().modifyBaseValue(mod);

    prop.setAddShadow(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_ADDSHADOW));

    prop.setFlipHorizontally(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    prop.setFlipVertically(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPVERTICALLY));
    
    prop.setTeam(mapObject.getCustomPropertyInt(MapObjectProperty.TEAM));

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(prop);
    return entities;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheetName, Material material) {
    Prop prop = new Prop(mapObject.getLocation(), spriteSheetName, material);
    prop.setObstacle(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_OBSTACLE));
    return prop;
  }
}
