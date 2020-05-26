package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.emitters.xml.ParticleParameter;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  public static List<String> getColors(IMapObject emitter) {
    List<String> particleColors = new ArrayList<>();
    String colorsString = emitter.getStringValue(MapObjectProperty.Emitter.COLORS, ColorHelper.encode(EmitterData.DEFAULT_COLOR));
    if (colorsString != null && !colorsString.isEmpty()) {
      for (String color : colorsString.split(","))
        if (color != null) {
          particleColors.add(color);
        }
    }

    return particleColors;
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    EmitterData data = createEmitterData(mapObject);

    CustomEmitter emitter = this.createCustomEmitter(data);
    loadDefaultProperties(emitter, mapObject);

    entities.add(emitter);

    return entities;
  }

  protected CustomEmitter createCustomEmitter(EmitterData data) {
    return new CustomEmitter(data);
  }

  public static EmitterData createEmitterData(IMapObject mapObject) {
    EmitterData data = new EmitterData();
    // emitter
    data.setWidth(mapObject.getWidth());
    data.setHeight(mapObject.getHeight());
    data.setSpawnRate(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE, EmitterData.DEFAULT_SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT, EmitterData.DEFAULT_SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, EmitterData.DEFAULT_UPDATERATE));
    data.setEmitterTTL(mapObject.getIntValue(MapObjectProperty.Emitter.TIMETOLIVE, EmitterData.DEFAULT_TTL));
    data.setMaxParticles(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES, EmitterData.DEFAULT_MAXPARTICLES));
    data.setParticleType(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterData.DEFAULT_PARTICLE_TYPE));
    data.setColorDeviation(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORDEVIATION, EmitterData.DEFAULT_COLOR_DEVIATION));
    data.setAlphaDeviation(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHADEVIATION, EmitterData.DEFAULT_ALPHA_DEVIATION));
    data.setOriginAlign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, EmitterData.DEFAULT_ORIGIN_ALIGN));
    data.setOriginValign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, EmitterData.DEFAULT_ORIGIN_VALIGN));
    data.setColors(getColors(mapObject));
    data.setColorProbabilities(mapObject.getStringValue(MapObjectProperty.Emitter.COLORPROBABILITIES, EmitterData.DEFAULT_COLOR_PROBABILITIES));

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXX)));
    data.setParticleY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXY)));
    data.setParticleWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINSTARTWIDTH), mapObject.getFloatValue(MapObjectProperty.Particle.MAXSTARTWIDTH)));
    data.setParticleHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINSTARTHEIGHT), mapObject.getFloatValue(MapObjectProperty.Particle.MAXSTARTHEIGHT)));
    data.setDeltaX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAX)));
    data.setDeltaY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAY)));
    data.setGravityX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINGRAVITYX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXGRAVITYX)));
    data.setGravityY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINGRAVITYY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXGRAVITYY)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAWIDTH), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAWIDTH)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAHEIGHT), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAHEIGHT)));

    data.setParticleMinTTL(mapObject.getIntValue(MapObjectProperty.Particle.MINTTL));
    data.setParticleMaxTTL(mapObject.getIntValue(MapObjectProperty.Particle.MAXTTL));
    data.setCollisionType(mapObject.getEnumValue(MapObjectProperty.Particle.COLLISIONTYPE, Collision.class, Collision.NONE));

    data.setParticleText(mapObject.getStringValue(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getStringValue(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getBoolValue(MapObjectProperty.Particle.ANIMATESPRITE));
    data.setFade(mapObject.getBoolValue(MapObjectProperty.Particle.FADE));
    return data;
  }

  public static void updateMapObject(EmitterData emitterData, IMapObject mo) {
    mo.setType(MapObjectType.EMITTER.toString());

    // emitter
    mo.setName(emitterData.getName());
    mo.setWidth(emitterData.getWidth());
    mo.setHeight(emitterData.getHeight());
    mo.setValue(MapObjectProperty.Emitter.SPAWNRATE, emitterData.getSpawnRate());
    mo.setValue(MapObjectProperty.Emitter.SPAWNAMOUNT, emitterData.getSpawnAmount());
    mo.setValue(MapObjectProperty.Emitter.UPDATERATE, emitterData.getUpdateRate());
    mo.setValue(MapObjectProperty.Emitter.TIMETOLIVE, emitterData.getEmitterTTL());
    mo.setValue(MapObjectProperty.Emitter.MAXPARTICLES, emitterData.getMaxParticles());
    mo.setValue(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType());
    mo.setValue(MapObjectProperty.Emitter.COLORDEVIATION, emitterData.getColorDeviation());
    mo.setValue(MapObjectProperty.Emitter.ALPHADEVIATION, emitterData.getAlphaDeviation());
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign());
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign());

    String commaSeperatedColors = ArrayUtilities.join(emitterData.getColors());
    mo.setValue(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    mo.setValue(MapObjectProperty.Emitter.PARTICLETYPE, ArrayUtilities.join(emitterData.getColorProbabilities()));

    mo.setValue(MapObjectProperty.Particle.MINX, emitterData.getParticleOffsetX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINY, emitterData.getParticleOffsetY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINSTARTWIDTH, emitterData.getParticleWidth().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINSTARTHEIGHT, emitterData.getParticleHeight().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINDELTAX, emitterData.getDeltaX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINDELTAY, emitterData.getDeltaY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINGRAVITYX, emitterData.getGravityX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINGRAVITYY, emitterData.getGravityY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINDELTAWIDTH, emitterData.getDeltaWidth().getMinValue());
    mo.setValue(MapObjectProperty.Particle.MINDELTAHEIGHT, emitterData.getDeltaHeight().getMinValue());

    mo.setValue(MapObjectProperty.Particle.MAXX, emitterData.getParticleOffsetX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXY, emitterData.getParticleOffsetY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXSTARTWIDTH, emitterData.getParticleWidth().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXSTARTHEIGHT, emitterData.getParticleHeight().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXDELTAX, emitterData.getDeltaX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXDELTAY, emitterData.getDeltaY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXGRAVITYX, emitterData.getGravityX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXGRAVITYY, emitterData.getGravityY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXDELTAWIDTH, emitterData.getDeltaWidth().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.MAXDELTAHEIGHT, emitterData.getDeltaHeight().getMaxValue());

    // particle
    mo.setValue(MapObjectProperty.Particle.MINTTL, emitterData.getParticleMinTTL());
    mo.setValue(MapObjectProperty.Particle.MAXTTL, emitterData.getParticleMaxTTL());
    mo.setValue(MapObjectProperty.Particle.COLLISIONTYPE, emitterData.getCollisionType());

    mo.setValue(MapObjectProperty.Particle.TEXT, emitterData.getParticleText());
    mo.setValue(MapObjectProperty.Particle.SPRITE, emitterData.getSpritesheet());
    mo.setValue(MapObjectProperty.Particle.ANIMATESPRITE, emitterData.isAnimateSprite());
    mo.setValue(MapObjectProperty.Particle.FADE, emitterData.isFading());
  }

  public static IMapObject createMapObject(EmitterData emitterData) {
    MapObject newMapObject = new MapObject();
    updateMapObject(emitterData, newMapObject);
    return newMapObject;
  }
}