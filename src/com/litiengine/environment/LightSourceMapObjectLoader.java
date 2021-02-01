package com.litiengine.environment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.LightSource;

public class LightSourceMapObjectLoader extends MapObjectLoader {

  protected LightSourceMapObjectLoader() {
    super(MapObjectType.LIGHTSOURCE);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final int intensity = mapObject.getIntValue(MapObjectProperty.LIGHT_INTENSITY, LightSource.DEFAULT_INTENSITY);
    final Color color = mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR);
    final boolean active = mapObject.getBoolValue(MapObjectProperty.LIGHT_ACTIVE, true);
    final LightSource.Type lightType = mapObject.getEnumValue(MapObjectProperty.LIGHT_SHAPE, LightSource.Type.class);
    final double focusOffsetX = mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETX);
    final double focusOffsetY = mapObject.getDoubleValue(MapObjectProperty.LIGHT_FOCUSOFFSETY);
    if (color == null) {
      return entities;
    }

    final LightSource light = this.createLightSource(mapObject, intensity, color, lightType, active);
    loadDefaultProperties(light, mapObject);
    light.setFocusOffsetX(focusOffsetX);
    light.setFocusOffsetY(focusOffsetY);

    entities.add(light);
    return entities;
  }

  protected LightSource createLightSource(IMapObject mapObject, int intensity, Color color, LightSource.Type lightType, boolean active) {
    return new LightSource(intensity, color, lightType, active);
  }
}