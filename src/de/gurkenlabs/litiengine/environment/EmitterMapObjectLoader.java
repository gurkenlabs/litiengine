package de.gurkenlabs.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleParameter;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  /***
   * TODO 04.10.2017: refactor this implementation because the hard coded emitter
   * types are not a proper approach.
   */
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
    data.setSpawnRate(mapObject.getCustomPropertyInt(MapObjectProperty.EMITTER_SPAWNRATE));
    data.setSpawnAmount(mapObject.getCustomPropertyInt(MapObjectProperty.EMITTER_SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getCustomPropertyInt(MapObjectProperty.EMITTER_UPDATEDELAY));
    data.setEmitterTTL(mapObject.getCustomPropertyInt(MapObjectProperty.EMITTER_TIMETOLIVE));
    data.setMaxParticles(mapObject.getCustomPropertyInt(MapObjectProperty.EMITTER_MAXPARTICLES));
    data.setParticleType(mapObject.getCustomPropertyEnum(MapObjectProperty.EMITTER_PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE));
    data.setColorDeviation(mapObject.getCustomPropertyFloat(MapObjectProperty.EMITTER_COLORDEVIATION));
    data.setAlphaDeviation(mapObject.getCustomPropertyFloat(MapObjectProperty.EMITTER_ALPHADEVIATION));

    String[] colors = mapObject.getCustomProperty(MapObjectProperty.EMITTER_COLORS, "").split(",");

    List<ParticleColor> particleColors = new ArrayList<>();
    for (String color : colors) {
      ParticleColor particleColor = ParticleColor.decode(color);
      if (particleColor != null) {
        particleColors.add(particleColor);
      }
    }

    data.setColors(particleColors);
    data.setColorProbabilities(mapObject.getCustomProperty(MapObjectProperty.EMITTER_COLORPROBABILITIES, ""));
    

    // particle
    data.setParticleX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINX), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_X_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINX),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXX)));

    data.setParticleY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINY), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_Y_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINY),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXY)));

    data.setParticleWidth(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINSTARTWIDTH), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_STARTWIDTH_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINSTARTWIDTH),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXSTARTWIDTH)));

    data.setParticleHeight(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINSTARTHEIGHT), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_STARTHEIGHT_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINSTARTHEIGHT),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXSTARTHEIGHT)));

    data.setDeltaX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAX), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_DELTAX_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAX),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXDELTAX)));

    data.setDeltaY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAY), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_DELTAY_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAY),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXDELTAY)));

    data.setGravityX(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINGRAVITYX), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_GRAVITYX_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINGRAVITYX),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXGRAVITYX)));

    data.setGravityY(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINGRAVITYY), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_GRAVITYY_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINGRAVITYY),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXGRAVITYY)));

    data.setDeltaWidth(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAWIDTH), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_DELTAWIDTH_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAWIDTH),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXDELTAWIDTH)));

    data.setDeltaHeight(new ParticleParameter(mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAHEIGHT), mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_DELTAHEIGHT_RANDOM), mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MINDELTAHEIGHT),
        mapObject.getCustomPropertyFloat(MapObjectProperty.PARTICLE_MAXDELTAHEIGHT)));

    data.setApplyStaticPhysics(mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_STATICPHYSICS));

    data.setParticleText(mapObject.getCustomProperty(MapObjectProperty.PARTICLE_TEXT));
    data.setSpritesheet(mapObject.getCustomProperty(MapObjectProperty.PARTICLE_SPRITE));
    data.setAnimateSprite(mapObject.getCustomPropertyBool(MapObjectProperty.PARTICLE_ANIMATESPRITE));
    return data;
  }

}
