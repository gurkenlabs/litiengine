package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;
import java.util.ArrayList;
import java.util.Collection;

public class EmitterMapObjectLoader extends MapObjectLoader {

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    EmitterData data = createEmitterData(mapObject);

    Emitter emitter = new Emitter(data);
    loadDefaultProperties(emitter, mapObject);

    entities.add(emitter);

    return entities;
  }

  public static EmitterData createEmitterData(IMapObject mapObject) {
    EmitterData data = new EmitterData();
    // emitter
    data.setWidth(mapObject.getWidth());
    data.setHeight(mapObject.getHeight());
    data.setSpawnRate(
      mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE, EmitterData.DEFAULT_SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT,
      EmitterData.DEFAULT_SPAWNAMOUNT));
    data.setUpdateRate(
      mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, EmitterData.DEFAULT_UPDATERATE));
    data.setEmitterDuration(
      mapObject.getIntValue(MapObjectProperty.Emitter.DURATION, EmitterData.DEFAULT_DURATION));
    data.setMaxParticles(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES,
      EmitterData.DEFAULT_MAXPARTICLES));
    data.setParticleType(
      mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class,
        EmitterData.DEFAULT_PARTICLE_TYPE));
    data.setColorVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORVARIANCE,
      EmitterData.DEFAULT_COLOR_VARIANCE));
    data.setAlphaVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHAVARIANCE,
      EmitterData.DEFAULT_ALPHA_VARIANCE));
    data.setOriginAlign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class,
      EmitterData.DEFAULT_ORIGIN_ALIGN));
    data.setOriginValign(
      mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class,
        EmitterData.DEFAULT_ORIGIN_VALIGN));
    data.setColors(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Emitter.COLORS,
      ColorHelper.encode(EmitterData.DEFAULT_COLOR)));

    // particle
    data.setParticleOffsetX(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_X_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_X_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_X_MIN)));
    data.setParticleOffsetY(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_Y_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_Y_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_Y_MIN)
      ));
    data.setParticleWidth(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.STARTWIDTH_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.STARTWIDTH_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.STARTWIDTH_MIN)
      ));
    data.setParticleHeight(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.STARTHEIGHT_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.STARTHEIGHT_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.STARTHEIGHT_MIN)
      ));
    data.setVelocityX(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_X_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_X_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_X_MIN)
      ));
    data.setVelocityY(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_Y_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_Y_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_Y_MIN)
      ));
    data.setAccelerationX(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_X_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_X_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_X_MIN)));
    data.setAccelerationY(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN)));
    data.setDeltaWidth(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.DELTAWIDTH_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTAWIDTH_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTAWIDTH_MIN)));
    data.setDeltaHeight(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN)));
    data.setAngle(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.ANGLE_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.ANGLE_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.ANGLE_MIN)));
    data.setDeltaAngle(
      new RangeAttribute<>(mapObject.getFloatValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN),
        mapObject.getFloatValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN)));
    data.setParticleTTL(
      new RangeAttribute<>(mapObject.getLongValue(MapObjectProperty.Particle.TTL_MAX),
        mapObject.getLongValue(MapObjectProperty.Particle.TTL_MIN),
        mapObject.getLongValue(MapObjectProperty.Particle.TTL_MIN)));

    data.setCollision(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class,
      EmitterData.DEFAULT_COLLISION));
    data.setRequiredQuality(
      mapObject.getEnumValue(MapObjectProperty.REQUIRED_QUALITY, Quality.class,
        EmitterData.DEFAULT_REQUIRED_QUALITY));

    data.setTexts(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Particle.TEXTS,
      EmitterData.DEFAULT_TEXT));
    data.setSpritesheet(mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME));
    data.setAnimateSprite(mapObject.getBoolValue(MapObjectProperty.Particle.ANIMATESPRITE));
    data.setLoopSprite(mapObject.getBoolValue(MapObjectProperty.Particle.LOOPSPRITE));
    data.setFade(mapObject.getBoolValue(MapObjectProperty.Particle.FADE));
    data.setFadeOnCollision(mapObject.getBoolValue(MapObjectProperty.Particle.FADEONCOLLISION));
    data.setOutlineOnly(mapObject.getBoolValue(MapObjectProperty.Particle.OUTLINEONLY));
    data.setAntiAliasing(mapObject.getBoolValue(MapObjectProperty.Particle.ANTIALIASING));
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
    mo.setValue(MapObjectProperty.Emitter.DURATION, emitterData.getEmitterDuration());
    mo.setValue(MapObjectProperty.Emitter.MAXPARTICLES, emitterData.getMaxParticles());
    mo.setValue(MapObjectProperty.Emitter.PARTICLETYPE, emitterData.getParticleType());
    mo.setValue(MapObjectProperty.Emitter.COLORVARIANCE, emitterData.getColorVariance());
    mo.setValue(MapObjectProperty.Emitter.ALPHAVARIANCE, emitterData.getAlphaVariance());
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, emitterData.getOriginAlign());
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, emitterData.getOriginValign());

    String commaSeperatedColors = ArrayUtilities.join(emitterData.getColors());
    mo.setValue(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    // Particle parameters, min bounds
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MIN,
      emitterData.getParticleOffsetX().getMin());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MIN,
      emitterData.getParticleOffsetY().getMin());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MIN,
      emitterData.getParticleWidth().getMin());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MIN,
      emitterData.getParticleHeight().getMin());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MIN,
      emitterData.getVelocityX().getMin());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MIN,
      emitterData.getVelocityY().getMin());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MIN,
      emitterData.getAccelerationX().getMin());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN,
      emitterData.getAccelerationY().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MIN,
      emitterData.getDeltaWidth().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN,
      emitterData.getDeltaHeight().getMin());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MIN, emitterData.getAngle().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN,
      emitterData.getDeltaAngle().getMin());
    mo.setValue(MapObjectProperty.Particle.TTL_MIN, emitterData.getParticleTTL().getMin());
    // Particle parameters, max bounds
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MAX,
      emitterData.getParticleOffsetX().getMax());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MAX,
      emitterData.getParticleOffsetY().getMax());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MAX,
      emitterData.getParticleWidth().getMax());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MAX,
      emitterData.getParticleHeight().getMax());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MAX,
      emitterData.getVelocityX().getMax());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MAX,
      emitterData.getVelocityY().getMax());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MAX,
      emitterData.getAccelerationX().getMax());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX,
      emitterData.getAccelerationY().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MAX,
      emitterData.getDeltaWidth().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX,
      emitterData.getDeltaHeight().getMax());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MAX, emitterData.getAngle().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX,
      emitterData.getDeltaAngle().getMax());
    mo.setValue(MapObjectProperty.Particle.TTL_MAX, emitterData.getParticleTTL().getMax());

    mo.setValue(MapObjectProperty.COLLISION_TYPE, emitterData.getCollision());
    mo.setValue(MapObjectProperty.REQUIRED_QUALITY, emitterData.getRequiredQuality());
    String commaSeperatedTexts = ArrayUtilities.join(emitterData.getTexts());
    mo.setValue(MapObjectProperty.Particle.TEXTS, commaSeperatedTexts);

    mo.setValue(MapObjectProperty.SPRITESHEETNAME, emitterData.getSpritesheet());
    mo.setValue(MapObjectProperty.Particle.ANIMATESPRITE, emitterData.isAnimatingSprite());
    mo.setValue(MapObjectProperty.Particle.LOOPSPRITE, emitterData.isLoopingSprite());
    mo.setValue(MapObjectProperty.Particle.FADE, emitterData.isFading());
    mo.setValue(MapObjectProperty.Particle.FADEONCOLLISION, emitterData.isFadingOnCollision());
    mo.setValue(MapObjectProperty.Particle.OUTLINEONLY, emitterData.isOutlineOnly());
    mo.setValue(MapObjectProperty.Particle.ANTIALIASING, emitterData.isAntiAliased());
  }

  public static IMapObject createMapObject(EmitterData emitterData) {
    MapObject newMapObject = new MapObject();
    updateMapObject(emitterData, newMapObject);
    return newMapObject;
  }
}
