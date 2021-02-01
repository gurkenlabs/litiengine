package com.litiengine.environment;

import java.util.ArrayList;
import java.util.Collection;

import com.litiengine.configuration.Quality;
import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.environment.tilemap.xml.MapObject;
import com.litiengine.Align;
import com.litiengine.Valign;
import com.litiengine.entities.IEntity;
import com.litiengine.graphics.emitters.Emitter;
import com.litiengine.graphics.emitters.particles.ParticleType;
import com.litiengine.graphics.emitters.xml.EmitterData;
import com.litiengine.graphics.emitters.xml.ParticleParameter;
import com.litiengine.physics.Collision;
import com.litiengine.util.ArrayUtilities;
import com.litiengine.util.ColorHelper;

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
    data.setSpawnRate(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE, EmitterData.DEFAULT_SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT, EmitterData.DEFAULT_SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, EmitterData.DEFAULT_UPDATERATE));
    data.setEmitterDuration(mapObject.getIntValue(MapObjectProperty.Emitter.DURATION, EmitterData.DEFAULT_DURATION));
    data.setMaxParticles(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES, EmitterData.DEFAULT_MAXPARTICLES));
    data.setParticleType(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterData.DEFAULT_PARTICLE_TYPE));
    data.setColorVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORVARIANCE, EmitterData.DEFAULT_COLOR_VARIANCE));
    data.setAlphaVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHAVARIANCE, EmitterData.DEFAULT_ALPHA_VARIANCE));
    data.setOriginAlign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, EmitterData.DEFAULT_ORIGIN_ALIGN));
    data.setOriginValign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, EmitterData.DEFAULT_ORIGIN_VALIGN));
    data.setColors(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Emitter.COLORS, ColorHelper.encode(EmitterData.DEFAULT_COLOR)));

    // particle
    data.setParticleOffsetX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_X_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_X_MAX)));
    data.setParticleOffsetY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_Y_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.OFFSET_Y_MAX)));
    data.setParticleWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.STARTWIDTH_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.STARTWIDTH_MAX)));
    data.setParticleHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.STARTHEIGHT_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.STARTHEIGHT_MAX)));
    data.setVelocityX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_X_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_X_MAX)));
    data.setVelocityY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_Y_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.VELOCITY_Y_MAX)));
    data.setAccelerationX(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_X_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_X_MAX)));
    data.setAccelerationY(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX)));
    data.setDeltaWidth(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.DELTAWIDTH_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.DELTAWIDTH_MAX)));
    data.setDeltaHeight(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX)));
    data.setAngle(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.ANGLE_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.ANGLE_MAX)));
    data.setDeltaRotation(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX)));
    data.setParticleTTL(new ParticleParameter(mapObject.getFloatValue(MapObjectProperty.Particle.TTL_MIN), mapObject.getFloatValue(MapObjectProperty.Particle.TTL_MAX)));

    data.setCollisionType(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class, EmitterData.DEFAULT_COLLISION));
    data.setRequiredQuality(mapObject.getEnumValue(MapObjectProperty.REQUIRED_QUALITY, Quality.class, EmitterData.DEFAULT_REQUIRED_QUALITY));

    data.setTexts(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Particle.TEXTS, EmitterData.DEFAULT_TEXT));
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

    //    Particle parameters, min bounds
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MIN, emitterData.getParticleOffsetX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MIN, emitterData.getParticleOffsetY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MIN, emitterData.getParticleWidth().getMinValue());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MIN, emitterData.getParticleHeight().getMinValue());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MIN, emitterData.getVelocityX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MIN, emitterData.getVelocityY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MIN, emitterData.getAccelerationX().getMinValue());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN, emitterData.getAccelerationY().getMinValue());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MIN, emitterData.getDeltaWidth().getMinValue());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN, emitterData.getDeltaHeight().getMinValue());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MIN, emitterData.getAngle().getMinValue());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN, emitterData.getDeltaAngle().getMinValue());
    mo.setValue(MapObjectProperty.Particle.TTL_MIN, emitterData.getParticleTTL().getMinValue());
    //  Particle parameters, max bounds
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MAX, emitterData.getParticleOffsetX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MAX, emitterData.getParticleOffsetY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MAX, emitterData.getParticleWidth().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MAX, emitterData.getParticleHeight().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MAX, emitterData.getVelocityX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MAX, emitterData.getVelocityY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MAX, emitterData.getAccelerationX().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX, emitterData.getAccelerationY().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MAX, emitterData.getDeltaWidth().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX, emitterData.getDeltaHeight().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MAX, emitterData.getAngle().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX, emitterData.getDeltaAngle().getMaxValue());
    mo.setValue(MapObjectProperty.Particle.TTL_MAX, emitterData.getParticleTTL().getMaxValue());

    mo.setValue(MapObjectProperty.COLLISION_TYPE, emitterData.getCollisionType());
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