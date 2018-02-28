package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleParameter;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  public static List<ParticleColor> getColors(IMapObject emitter) {
    List<ParticleColor> particleColors = new ArrayList<>();
    String colorsString = emitter.getCustomProperty(MapObjectProperty.Emitter.COLORS, "");
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
  public Collection<IEntity> load(IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + EmitterMapObjectLoader.class);
    }

    EmitterData data = createEmitterData(mapObject);

    // TODO: implement origin: https://github.com/gurkenlabs/litiengine/issues/74
    // Also see Emitter.getOrigin
    CustomEmitter emitter = new CustomEmitter(data);
    loadDefaultProperties(emitter, mapObject);
    emitter.setLocation(mapObject.getLocation().getX() + mapObject.getWidth() / 2.0, mapObject.getLocation().getY() + mapObject.getHeight() / 2.0);

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(emitter);

    return entities;
  }

  public static EmitterData createEmitterData(IMapObject mapObject) {
    EmitterData data = new EmitterData();
    // emitter
    data.setWidth(mapObject.getWidth());
    data.setHeight(mapObject.getHeight());
    data.setSpawnRate(mapObject.getCustomPropertyInt(MapObjectProperty.Emitter.SPAWNRATE));
    data.setSpawnAmount(mapObject.getCustomPropertyInt(MapObjectProperty.Emitter.SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getCustomPropertyInt(MapObjectProperty.Emitter.UPDATERATE, Emitter.DEFAULT_UPDATERATE));
    data.setEmitterTTL(mapObject.getCustomPropertyInt(MapObjectProperty.Emitter.TIMETOLIVE));
    data.setMaxParticles(mapObject.getCustomPropertyInt(MapObjectProperty.Emitter.MAXPARTICLES));
    data.setParticleType(mapObject.getCustomPropertyEnum(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE));
    data.setColorDeviation(mapObject.getCustomPropertyFloat(MapObjectProperty.Emitter.COLORDEVIATION));
    data.setAlphaDeviation(mapObject.getCustomPropertyFloat(MapObjectProperty.Emitter.ALPHADEVIATION));

    data.setColors(getColors(mapObject));

    data.setColorProbabilities(mapObject.getCustomProperty(MapObjectProperty.Emitter.COLORPROBABILITIES, ""));

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINX), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINY), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleWidth(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINSTARTWIDTH), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXSTARTWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setParticleHeight(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINSTARTHEIGHT), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXSTARTHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINDELTAX), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXDELTAX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINDELTAY), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXDELTAY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINGRAVITYX), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXGRAVITYX, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setGravityY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINGRAVITYY), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXGRAVITYY, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINDELTAWIDTH), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXDELTAWIDTH, ParticleParameter.MAX_VALUE_UNDEFINED)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MINDELTAHEIGHT), mapObject.getCustomPropertyFloat(MapObjectProperty.Particle.MAXDELTAHEIGHT, ParticleParameter.MAX_VALUE_UNDEFINED)));

    data.setParticleMinTTL(mapObject.getCustomPropertyInt(MapObjectProperty.Particle.MINTTL));
    data.setParticleMaxTTL(mapObject.getCustomPropertyInt(MapObjectProperty.Particle.MAXTTL));
    data.setCollisionType(mapObject.getCustomPropertyEnum(MapObjectProperty.Particle.COLLISIONTYPE, CollisionType.class, CollisionType.NONE));

    data.setParticleText(mapObject.getCustomProperty(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getCustomProperty(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getCustomPropertyBool(MapObjectProperty.Particle.ANIMATESPRITE));
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
    
    return newMapObject;
  }
}