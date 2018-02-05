package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleParameter;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;

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

    EmitterData data = this.createEmitterData(mapObject);
    CustomEmitter emitter = new CustomEmitter(mapObject.getLocation().getX(), mapObject.getLocation().getY(), data);

    emitter.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    emitter.setLocation(mapObject.getLocation());
    emitter.setMapId(mapObject.getId());
    emitter.setName(mapObject.getName());

    Collection<IEntity> entities = super.load(mapObject);
    entities.add(emitter);

    return entities;
  }

  private EmitterData createEmitterData(IMapObject mapObject) {
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
    data.setApplyStaticPhysics(mapObject.getCustomPropertyBool(MapObjectProperty.Particle.STATICPHYSICS));

    data.setParticleText(mapObject.getCustomProperty(MapObjectProperty.Particle.TEXT));
    data.setSpritesheet(mapObject.getCustomProperty(MapObjectProperty.Particle.SPRITE));
    data.setAnimateSprite(mapObject.getCustomPropertyBool(MapObjectProperty.Particle.ANIMATESPRITE));
    return data;
  }

}
