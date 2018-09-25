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
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.SPAWNRATE, Integer.toString(emitterData.getSpawnRate()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.SPAWNAMOUNT, Integer.toString(emitterData.getSpawnAmount()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.UPDATERATE, Integer.toString(emitterData.getUpdateRate()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.TIMETOLIVE, Integer.toString(emitterData.getEmitterTTL()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.MAXPARTICLES, Integer.toString(emitterData.getMaxParticles()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType().name());
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.COLORDEVIATION, Float.toString(emitterData.getColorDeviation()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.ALPHADEVIATION, Float.toString(emitterData.getAlphaDeviation()));
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign().name());
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign().name());

    String commaSeperatedColors = ArrayUtilities.getCommaSeparatedString(emitterData.getColors());
    newMapObject.setCustomProperty(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    newMapObject.setCustomProperty(MapObjectProperty.Emitter.PARTICLETYPE, ArrayUtilities.getCommaSeparatedString(emitterData.getColorProbabilities()));

    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINX, Float.toString(emitterData.getParticleX().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINY, Float.toString(emitterData.getParticleY().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINSTARTWIDTH, Float.toString(emitterData.getParticleWidth().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINSTARTHEIGHT, Float.toString(emitterData.getParticleHeight().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINDELTAX, Float.toString(emitterData.getDeltaX().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINDELTAY, Float.toString(emitterData.getDeltaY().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINGRAVITYX, Float.toString(emitterData.getGravityX().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINGRAVITYY, Float.toString(emitterData.getGravityY().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINDELTAWIDTH, Float.toString(emitterData.getDeltaWidth().getMinValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINDELTAHEIGHT, Float.toString(emitterData.getDeltaHeight().getMinValue()));

    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXX, Float.toString(emitterData.getParticleX().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXY, Float.toString(emitterData.getParticleY().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXSTARTWIDTH, Float.toString(emitterData.getParticleWidth().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXSTARTHEIGHT, Float.toString(emitterData.getParticleHeight().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXDELTAX, Float.toString(emitterData.getDeltaX().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXDELTAY, Float.toString(emitterData.getDeltaY().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXGRAVITYX, Float.toString(emitterData.getGravityX().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXGRAVITYY, Float.toString(emitterData.getGravityY().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXDELTAWIDTH, Float.toString(emitterData.getDeltaWidth().getMaxValue()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXDELTAHEIGHT, Float.toString(emitterData.getDeltaHeight().getMaxValue()));

    // particle
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MINTTL, Integer.toString(emitterData.getParticleMinTTL()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.MAXTTL, Integer.toString(emitterData.getParticleMaxTTL()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.COLLISIONTYPE, emitterData.getCollisionType().toString());

    newMapObject.setCustomProperty(MapObjectProperty.Particle.TEXT, emitterData.getParticleText());
    newMapObject.setCustomProperty(MapObjectProperty.Particle.SPRITE, emitterData.getSpritesheet());
    newMapObject.setCustomProperty(MapObjectProperty.Particle.ANIMATESPRITE, Boolean.toString(emitterData.isAnimateSprite()));
    newMapObject.setCustomProperty(MapObjectProperty.Particle.FADE, Boolean.toString(emitterData.isFading()));
    return newMapObject;
  }
}