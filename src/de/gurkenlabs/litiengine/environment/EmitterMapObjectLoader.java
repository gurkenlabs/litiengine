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
    String colorsString = emitter.getStringProperty(MapObjectProperty.Emitter.COLORS, "");
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
    data.setSpawnRate(mapObject.getIntProperty(MapObjectProperty.Emitter.SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntProperty(MapObjectProperty.Emitter.SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getIntProperty(MapObjectProperty.Emitter.UPDATERATE, Emitter.DEFAULT_UPDATERATE));
    data.setEmitterTTL(mapObject.getIntProperty(MapObjectProperty.Emitter.TIMETOLIVE));
    data.setMaxParticles(mapObject.getIntProperty(MapObjectProperty.Emitter.MAXPARTICLES));
    data.setParticleType(mapObject.getEnumProperty(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE));
    data.setColorDeviation(mapObject.getFloatProperty(MapObjectProperty.Emitter.COLORDEVIATION));
    data.setAlphaDeviation(mapObject.getFloatProperty(MapObjectProperty.Emitter.ALPHADEVIATION));
    data.setOriginAlign(mapObject.getEnumProperty(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, Align.LEFT));
    data.setOriginValign(mapObject.getEnumProperty(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, Valign.TOP));

    data.setColors(getColors(mapObject));

    data.setColorProbabilities(mapObject.getStringProperty(MapObjectProperty.Emitter.COLORPROBABILITIES, ""));

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINX), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleY(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINY), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleWidth(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINSTARTWIDTH), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXSTARTWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleHeight(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINSTARTHEIGHT), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXSTARTHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaX(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINDELTAX), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXDELTAX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaY(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINDELTAY), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXDELTAY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityX(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINGRAVITYX), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXGRAVITYX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityY(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINGRAVITYY), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXGRAVITYY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINDELTAWIDTH), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXDELTAWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getFloatProperty(MapObjectProperty.Particle.MINDELTAHEIGHT), mapObject.getFloatProperty(MapObjectProperty.Particle.MAXDELTAHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));

    data.setParticleMinTTL(mapObject.getIntProperty(MapObjectProperty.Particle.MINTTL));
    data.setParticleMaxTTL(mapObject.getIntProperty(MapObjectProperty.Particle.MAXTTL));
    data.setCollisionType(mapObject.getEnumProperty(MapObjectProperty.Particle.COLLISIONTYPE, CollisionType.class, CollisionType.NONE));

    data.setParticleText(mapObject.getStringProperty(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getStringProperty(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getBoolProperty(MapObjectProperty.Particle.ANIMATESPRITE));
    data.setFade(mapObject.getBoolProperty(MapObjectProperty.Particle.FADE));
    return data;
  }

  public static IMapObject createMapObject(EmitterData emitterData) {
    MapObject newMapObject = new MapObject();
    newMapObject.setType(MapObjectType.EMITTER.toString());

    // emitter
    newMapObject.setWidth(emitterData.getWidth());
    newMapObject.setHeight(emitterData.getHeight());
    newMapObject.setProperty(MapObjectProperty.Emitter.SPAWNRATE, emitterData.getSpawnRate());
    newMapObject.setProperty(MapObjectProperty.Emitter.SPAWNAMOUNT, emitterData.getSpawnAmount());
    newMapObject.setProperty(MapObjectProperty.Emitter.UPDATERATE, emitterData.getUpdateRate());
    newMapObject.setProperty(MapObjectProperty.Emitter.TIMETOLIVE, emitterData.getEmitterTTL());
    newMapObject.setProperty(MapObjectProperty.Emitter.MAXPARTICLES, emitterData.getMaxParticles());
    newMapObject.setProperty(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType());
    newMapObject.setProperty(MapObjectProperty.Emitter.COLORDEVIATION, emitterData.getColorDeviation());
    newMapObject.setProperty(MapObjectProperty.Emitter.ALPHADEVIATION, emitterData.getAlphaDeviation());
    newMapObject.setProperty(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign());
    newMapObject.setProperty(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign());

    String commaSeperatedColors = ArrayUtilities.join(emitterData.getColors());
    newMapObject.setProperty(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    newMapObject.setProperty(MapObjectProperty.Emitter.PARTICLETYPE, ArrayUtilities.join(emitterData.getColorProbabilities()));

    newMapObject.setProperty(MapObjectProperty.Particle.MINX, emitterData.getParticleX().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINY, emitterData.getParticleY().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINSTARTWIDTH, emitterData.getParticleWidth().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINSTARTHEIGHT, emitterData.getParticleHeight().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINDELTAX, emitterData.getDeltaX().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINDELTAY, emitterData.getDeltaY().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINGRAVITYX, emitterData.getGravityX().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINGRAVITYY, emitterData.getGravityY().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINDELTAWIDTH, emitterData.getDeltaWidth().getMinValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MINDELTAHEIGHT, emitterData.getDeltaHeight().getMinValue());

    newMapObject.setProperty(MapObjectProperty.Particle.MAXX, emitterData.getParticleX().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXY, emitterData.getParticleY().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXSTARTWIDTH, emitterData.getParticleWidth().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXSTARTHEIGHT, emitterData.getParticleHeight().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXDELTAX, emitterData.getDeltaX().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXDELTAY, emitterData.getDeltaY().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXGRAVITYX, emitterData.getGravityX().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXGRAVITYY, emitterData.getGravityY().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXDELTAWIDTH, emitterData.getDeltaWidth().getMaxValue());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXDELTAHEIGHT, emitterData.getDeltaHeight().getMaxValue());

    // particle
    newMapObject.setProperty(MapObjectProperty.Particle.MINTTL, emitterData.getParticleMinTTL());
    newMapObject.setProperty(MapObjectProperty.Particle.MAXTTL, emitterData.getParticleMaxTTL());
    newMapObject.setProperty(MapObjectProperty.Particle.COLLISIONTYPE, emitterData.getCollisionType());

    newMapObject.setProperty(MapObjectProperty.Particle.TEXT, emitterData.getParticleText());
    newMapObject.setProperty(MapObjectProperty.Particle.SPRITE, emitterData.getSpritesheet());
    newMapObject.setProperty(MapObjectProperty.Particle.ANIMATESPRITE, emitterData.isAnimateSprite());
    newMapObject.setProperty(MapObjectProperty.Particle.FADE, emitterData.isFading());
    return newMapObject;
  }
}