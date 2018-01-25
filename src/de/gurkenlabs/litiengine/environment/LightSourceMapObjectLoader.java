package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.LightSource;

public class LightSourceMapObjectLoader extends MapObjectLoader {

  protected LightSourceMapObjectLoader() {
    super(MapObjectType.LIGHTSOURCE);
  }

  @Override
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.LIGHTSOURCE) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + LightSourceMapObjectLoader.class);
    }

    final int alpha = mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_ALPHA);
    final int intensity = mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_INTENSITY);
    final String mapObjectColor = mapObject.getCustomProperty(MapObjectProperty.LIGHT_COLOR);
    final String mapObjectLightOn = mapObject.getCustomProperty(MapObjectProperty.LIGHT_ACTIVE);
    final String lightShape = mapObject.getCustomProperty(MapObjectProperty.LIGHT_SHAPE);
    if (mapObjectColor == null || mapObjectColor.isEmpty() || lightShape == null) {
      return null;
    }

    final Color color = Color.decode(mapObjectColor);

    String lightType;
    switch (lightShape) {
    case LightSource.ELLIPSE:
      lightType = LightSource.ELLIPSE;
      break;
    case LightSource.RECTANGLE:
      lightType = LightSource.RECTANGLE;
      break;
    default:
      lightType = LightSource.ELLIPSE;
    }
    boolean lightOn = mapObjectLightOn == null || mapObjectLightOn.isEmpty() ? true : Boolean.parseBoolean(mapObjectLightOn);
    final LightSource light = new LightSource(intensity, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha), lightType, lightOn);
    light.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    light.setLocation(mapObject.getLocation());
    light.setMapId(mapObject.getId());
    light.setName(mapObject.getName());

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(light);
    return entities;
  }
}