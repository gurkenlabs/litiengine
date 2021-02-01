package com.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.SoundSource;

public class SoundSourceMapObjectLoader extends MapObjectLoader {

  protected SoundSourceMapObjectLoader() {
    super(MapObjectType.SOUNDSOURCE);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final SoundSource sound = this.createSoundSource(mapObject);
    loadDefaultProperties(sound, mapObject);

    entities.add(sound);

    return entities;
  }

  protected SoundSource createSoundSource(IMapObject mapObject) {
    SoundSource sound = new SoundSource();
    sound.setSound(mapObject.getStringValue(MapObjectProperty.SOUND_NAME));
    sound.setVolume(mapObject.getFloatValue(MapObjectProperty.SOUND_VOLUME));
    sound.setLoop(mapObject.getBoolValue(MapObjectProperty.SOUND_LOOP));
    sound.setRange(mapObject.getIntValue(MapObjectProperty.SOUND_RANGE));

    return sound;
  }
}
