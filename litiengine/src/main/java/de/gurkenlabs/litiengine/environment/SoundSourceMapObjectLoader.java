package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.SoundSource;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

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
    sound.setVolume(mapObject.getFloatValue(MapObjectProperty.SOUND_VOLUME, 0));
    sound.setLoop(mapObject.getBoolValue(MapObjectProperty.SOUND_LOOP, false));
    sound.setRange(mapObject.getIntValue(MapObjectProperty.SOUND_RANGE, 0));

    return sound;
  }
}
