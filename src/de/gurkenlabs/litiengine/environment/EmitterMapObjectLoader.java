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
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.emitters.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.emitters.xml.ParticleParameter;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  public static List<ParticleColor> getColors(IMapObject emitter) {
    List<ParticleColor> particleColors = new ArrayList<>();
    String colorsString = emitter.getStringValue(MapObjectProperty.Emitter.COLORS, "");
    if (colorsString != null && !colorsString.isEmpty()) {
      String[] colors = colorsString.split(",");
      for (String color : colors) {
        ParticleColor particleColor = ParticleColor.decode(color);
        if (particleColor != null) {
          particleColors.add(particleColor);
        }
      }
    }

    return particleColors;
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + EmitterMapObjectLoader.class);
    }

    EmitterData data = createEmitterData(mapObject);

    CustomEmitter emitter = this.createCustomEmitter(data);
    loadDefaultProperties(emitter, mapObject);

    Collection<IEntity> entities = new ArrayList<>();
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
    data.setSpawnRate(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, Emitter.DEFAULT_UPDATERATE));
    data.setEmitterTTL(mapObject.getIntValue(MapObjectProperty.Emitter.TIMETOLIVE));
    data.setMaxParticles(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
    data.setParticleType(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE));
    data.setColorDeviation(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORDEVIATION));
    data.setAlphaDeviation(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHADEVIATION));
    data.setOriginAlign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, Align.LEFT));
    data.setOriginValign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, Valign.TOP));

    data.setColors(getColors(mapObject));

    data.setColorProbabilities(mapObject.getStringValue(MapObjectProperty.Emitter.COLORPROBABILITIES, ""));

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINSTARTWIDTH), mapObject.getFloatValue(MapObjectProperty.Particle.MAXSTARTWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINSTARTHEIGHT), mapObject.getFloatValue(MapObjectProperty.Particle.MAXSTARTHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINGRAVITYX), mapObject.getFloatValue(MapObjectProperty.Particle.MAXGRAVITYX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINGRAVITYY), mapObject.getFloatValue(MapObjectProperty.Particle.MAXGRAVITYY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAWIDTH), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.MINDELTAHEIGHT), mapObject.getFloatValue(MapObjectProperty.Particle.MAXDELTAHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));

    data.setParticleMinTTL(mapObject.getIntValue(MapObjectProperty.Particle.MINTTL));
    data.setParticleMaxTTL(mapObject.getIntValue(MapObjectProperty.Particle.MAXTTL));
    data.setCollisionType(mapObject.getEnumValue(MapObjectProperty.Particle.COLLISIONTYPE, CollisionType.class, CollisionType.NONE));

    data.setParticleText(mapObject.getStringValue(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getStringValue(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getBoolValue(MapObjectProperty.Particle.ANIMATESPRITE));
    data.setFade(mapObject.getBoolValue(MapObjectProperty.Particle.FADE));
    return data;
  }

  @SuppressWarnings("deprecation")
  public static IMapObject createMapObject(EmitterData emitterData) {
    MapObject newMapObject = new MapObject();
    newMapObject.setType(MapObjectType.EMITTER.toString());

    // emitter
    newMapObject.setWidth(emitterData.getWidth());
    newMapObject.setHeight(emitterData.getHeight());
    newMapObject.setValue(MapObjectProperty.Emitter.SPAWNRATE, emitterData.getSpawnRate());
    newMapObject.setValue(MapObjectProperty.Emitter.SPAWNAMOUNT, emitterData.getSpawnAmount());
    newMapObject.setValue(MapObjectProperty.Emitter.UPDATERATE, emitterData.getUpdateRate());
    newMapObject.setValue(MapObjectProperty.Emitter.TIMETOLIVE, emitterData.getEmitterTTL());
    newMapObject.setValue(MapObjectProperty.Emitter.MAXPARTICLES, emitterData.getMaxParticles());
    newMapObject.setValue(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType());
    newMapObject.setValue(MapObjectProperty.Emitter.COLORDEVIATION, emitterData.getColorDeviation());
    newMapObject.setValue(MapObjectProperty.Emitter.ALPHADEVIATION, emitterData.getAlphaDeviation());
    newMapObject.setValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign());
    newMapObject.setValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign());

    String commaSeperatedColors = ArrayUtilities.join(emitterData.getColors());
    newMapObject.setValue(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    newMapObject.setValue(MapObjectProperty.Emitter.PARTICLETYPE, ArrayUtilities.join(emitterData.getColorProbabilities()));

    newMapObject.setValue(MapObjectProperty.Particle.MINX, emitterData.getParticleX().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINY, emitterData.getParticleY().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINSTARTWIDTH, emitterData.getParticleWidth().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINSTARTHEIGHT, emitterData.getParticleHeight().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINDELTAX, emitterData.getDeltaX().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINDELTAY, emitterData.getDeltaY().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINGRAVITYX, emitterData.getGravityX().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINGRAVITYY, emitterData.getGravityY().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINDELTAWIDTH, emitterData.getDeltaWidth().getMinValue());
    newMapObject.setValue(MapObjectProperty.Particle.MINDELTAHEIGHT, emitterData.getDeltaHeight().getMinValue());

    newMapObject.setValue(MapObjectProperty.Particle.MAXX, emitterData.getParticleX().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXY, emitterData.getParticleY().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXSTARTWIDTH, emitterData.getParticleWidth().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXSTARTHEIGHT, emitterData.getParticleHeight().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXDELTAX, emitterData.getDeltaX().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXDELTAY, emitterData.getDeltaY().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXGRAVITYX, emitterData.getGravityX().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXGRAVITYY, emitterData.getGravityY().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXDELTAWIDTH, emitterData.getDeltaWidth().getMaxValue());
    newMapObject.setValue(MapObjectProperty.Particle.MAXDELTAHEIGHT, emitterData.getDeltaHeight().getMaxValue());

    // particle
    newMapObject.setValue(MapObjectProperty.Particle.MINTTL, emitterData.getParticleMinTTL());
    newMapObject.setValue(MapObjectProperty.Particle.MAXTTL, emitterData.getParticleMaxTTL());
    newMapObject.setValue(MapObjectProperty.Particle.COLLISIONTYPE, emitterData.getCollisionType());

    newMapObject.setValue(MapObjectProperty.Particle.TEXT, emitterData.getParticleText());
    newMapObject.setValue(MapObjectProperty.Particle.SPRITE, emitterData.getSpritesheet());
    newMapObject.setValue(MapObjectProperty.Particle.ANIMATESPRITE, emitterData.isAnimateSprite());
    newMapObject.setValue(MapObjectProperty.Particle.FADE, emitterData.isFading());
    return newMapObject;
  }
}