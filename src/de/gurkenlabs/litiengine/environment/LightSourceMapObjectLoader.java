package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.util.ArrayList;
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
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.LIGHTSOURCE) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + LightSourceMapObjectLoader.class);
    }

    final int alpha = mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_ALPHA);
    final int intensity = mapObject.getCustomPropertyInt(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY);
    final Color color = mapObject.getCustomPropertyColor(MapObjectProperty.LIGHT_COLOR);
    final boolean active = mapObject.getCustomPropertyBool(MapObjectProperty.LIGHT_ACTIVE, true);
    final String lightShape = mapObject.getCustomProperty(MapObjectProperty.LIGHT_SHAPE);
    Collection<IEntity> entities = new ArrayList<>();
    if (color == null || lightShape == null) {
      return entities;
    }

    String lightType;
    switch (lightShape) {
    case LightSource.RECTANGLE:
      lightType = LightSource.RECTANGLE;
      break;
    case LightSource.ELLIPSE:
    default:
      lightType = LightSource.ELLIPSE;
      break;
    }

    final LightSource light = this.createLightSource(mapObject, intensity, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha), lightType, active);
    loadDefaultProperties(light, mapObject);

    entities.add(light);
    return entities;
  }

  protected LightSource createLightSource(IMapObject mapObject, int intensity, Color color, String lightType, boolean active) {
    return new LightSource(intensity, color, lightType, active);
  }
}