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
    String colorsString = emitter.getString(MapObjectProperty.Emitter.COLORS, "");
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
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
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
    data.setSpawnRate(mapObject.getInt(MapObjectProperty.Emitter.SPAWNRATE));
    data.setSpawnAmount(mapObject.getInt(MapObjectProperty.Emitter.SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getInt(MapObjectProperty.Emitter.UPDATERATE, Emitter.DEFAULT_UPDATERATE));
    data.setEmitterTTL(mapObject.getInt(MapObjectProperty.Emitter.TIMETOLIVE));
    data.setMaxParticles(mapObject.getInt(MapObjectProperty.Emitter.MAXPARTICLES));
    data.setParticleType(mapObject.getEnum(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE));
    data.setColorDeviation(mapObject.getFloat(MapObjectProperty.Emitter.COLORDEVIATION));
    data.setAlphaDeviation(mapObject.getFloat(MapObjectProperty.Emitter.ALPHADEVIATION));
    data.setOriginAlign(mapObject.getEnum(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, Align.LEFT));
    data.setOriginValign(mapObject.getEnum(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, Valign.TOP));

    data.setColors(getColors(mapObject));

    data.setColorProbabilities(mapObject.getString(MapObjectProperty.Emitter.COLORPROBABILITIES, ""));

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINX), mapObject.getFloat(MapObjectProperty.Particle.MAXX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleY(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINY), mapObject.getFloat(MapObjectProperty.Particle.MAXY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleWidth(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINSTARTWIDTH), mapObject.getFloat(MapObjectProperty.Particle.MAXSTARTWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleHeight(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINSTARTHEIGHT), mapObject.getFloat(MapObjectProperty.Particle.MAXSTARTHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaX(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINDELTAX), mapObject.getFloat(MapObjectProperty.Particle.MAXDELTAX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaY(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINDELTAY), mapObject.getFloat(MapObjectProperty.Particle.MAXDELTAY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityX(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINGRAVITYX), mapObject.getFloat(MapObjectProperty.Particle.MAXGRAVITYX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityY(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINGRAVITYY), mapObject.getFloat(MapObjectProperty.Particle.MAXGRAVITYY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINDELTAWIDTH), mapObject.getFloat(MapObjectProperty.Particle.MAXDELTAWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getFloat(MapObjectProperty.Particle.MINDELTAHEIGHT), mapObject.getFloat(MapObjectProperty.Particle.MAXDELTAHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));

    data.setParticleMinTTL(mapObject.getInt(MapObjectProperty.Particle.MINTTL));
    data.setParticleMaxTTL(mapObject.getInt(MapObjectProperty.Particle.MAXTTL));
    data.setCollisionType(mapObject.getEnum(MapObjectProperty.Particle.COLLISIONTYPE, CollisionType.class, CollisionType.NONE));

    data.setParticleText(mapObject.getString(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getString(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getBool(MapObjectProperty.Particle.ANIMATESPRITE));
    data.setFade(mapObject.getBool(MapObjectProperty.Particle.FADE));
    return data;
  }

  public static IMapObject createMapObject(EmitterData emitterData) {
    MapObject newMapObject = new MapObject();
    newMapObject.setType(MapObjectType.EMITTER.toString());

    // emitter
    newMapObject.setWidth(emitterData.getWidth());
    newMapObject.setHeight(emitterData.getHeight());
    newMapObject.set(MapObjectProperty.Emitter.SPAWNRATE, emitterData.getSpawnRate());
    newMapObject.set(MapObjectProperty.Emitter.SPAWNAMOUNT, emitterData.getSpawnAmount());
    newMapObject.set(MapObjectProperty.Emitter.UPDATERATE, emitterData.getUpdateRate());
    newMapObject.set(MapObjectProperty.Emitter.TIMETOLIVE, emitterData.getEmitterTTL());
    newMapObject.set(MapObjectProperty.Emitter.MAXPARTICLES, emitterData.getMaxParticles());
    newMapObject.set(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType());
    newMapObject.set(MapObjectProperty.Emitter.COLORDEVIATION, emitterData.getColorDeviation());
    newMapObject.set(MapObjectProperty.Emitter.ALPHADEVIATION, emitterData.getAlphaDeviation());
    newMapObject.set(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign());
    newMapObject.set(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign());

    String commaSeperatedColors = ArrayUtilities.getCommaSeparatedString(emitterData.getColors());
    newMapObject.set(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    newMapObject.set(MapObjectProperty.Emitter.PARTICLETYPE, ArrayUtilities.getCommaSeparatedString(emitterData.getColorProbabilities()));

    newMapObject.set(MapObjectProperty.Particle.MINX, emitterData.getParticleX().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINY, emitterData.getParticleY().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINSTARTWIDTH, emitterData.getParticleWidth().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINSTARTHEIGHT, emitterData.getParticleHeight().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINDELTAX, emitterData.getDeltaX().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINDELTAY, emitterData.getDeltaY().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINGRAVITYX, emitterData.getGravityX().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINGRAVITYY, emitterData.getGravityY().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINDELTAWIDTH, emitterData.getDeltaWidth().getMinValue());
    newMapObject.set(MapObjectProperty.Particle.MINDELTAHEIGHT, emitterData.getDeltaHeight().getMinValue());

    newMapObject.set(MapObjectProperty.Particle.MAXX, emitterData.getParticleX().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXY, emitterData.getParticleY().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXSTARTWIDTH, emitterData.getParticleWidth().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXSTARTHEIGHT, emitterData.getParticleHeight().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXDELTAX, emitterData.getDeltaX().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXDELTAY, emitterData.getDeltaY().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXGRAVITYX, emitterData.getGravityX().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXGRAVITYY, emitterData.getGravityY().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXDELTAWIDTH, emitterData.getDeltaWidth().getMaxValue());
    newMapObject.set(MapObjectProperty.Particle.MAXDELTAHEIGHT, emitterData.getDeltaHeight().getMaxValue());

    // particle
    newMapObject.set(MapObjectProperty.Particle.MINTTL, emitterData.getParticleMinTTL());
    newMapObject.set(MapObjectProperty.Particle.MAXTTL, emitterData.getParticleMaxTTL());
    newMapObject.set(MapObjectProperty.Particle.COLLISIONTYPE, emitterData.getCollisionType());

    newMapObject.set(MapObjectProperty.Particle.TEXT, emitterData.getParticleText());
    newMapObject.set(MapObjectProperty.Particle.SPRITE, emitterData.getSpritesheet());
    newMapObject.set(MapObjectProperty.Particle.ANIMATESPRITE, emitterData.isAnimateSprite());
    newMapObject.set(MapObjectProperty.Particle.FADE, emitterData.isFading());
    return newMapObject;
  }
}