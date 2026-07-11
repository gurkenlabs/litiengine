package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.Particle;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ColorHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class EmitterMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(EmitterMapObjectLoader.class.getName());

  protected EmitterMapObjectLoader() {
    super(MapObjectType.EMITTER);
  }

  @Override public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    EmitterAttributes data = createEmitterData(mapObject);

    Emitter emitter = new Emitter(data);
    loadDefaultProperties(emitter, mapObject);

    entities.add(emitter);

    return entities;
  }

  public static EmitterAttributes createEmitterData(IMapObject mapObject) {
    EmitterAttributes data = new EmitterAttributes();
    // emitter
    data.setWidth(mapObject.getWidth());
    data.setHeight(mapObject.getHeight());
    data.setSpawnRate(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE, EmitterAttributes.DEFAULT_SPAWNRATE));
    data.setSpawnAmount(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT, EmitterAttributes.DEFAULT_SPAWNAMOUNT));
    data.setUpdateRate(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, EmitterAttributes.DEFAULT_UPDATERATE));
    data.setEmitterDuration(mapObject.getIntValue(MapObjectProperty.Emitter.DURATION, EmitterAttributes.DEFAULT_DURATION));
    data.setMaxParticles(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES, EmitterAttributes.DEFAULT_MAXPARTICLES));
    data.setParticleType(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterAttributes.DEFAULT_PARTICLE_TYPE));
    data.setColorVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORVARIANCE, EmitterAttributes.DEFAULT_COLOR_VARIANCE));
    data.setAlphaVariance(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHAVARIANCE, EmitterAttributes.DEFAULT_ALPHA_VARIANCE));
    data.setOriginAlign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, EmitterAttributes.DEFAULT_ORIGIN_ALIGN));
    data.setOriginValign(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, EmitterAttributes.DEFAULT_ORIGIN_VALIGN));
    data.setColors(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Emitter.COLORS, ColorHelper.encode(EmitterAttributes.DEFAULT_COLOR)));

    // particle
    data.setParticleOffsetX(range(mapObject, MapObjectProperty.Particle.OFFSET_X_MIN, MapObjectProperty.Particle.OFFSET_X_MAX, 0));
    data.setParticleOffsetY(range(mapObject, MapObjectProperty.Particle.OFFSET_Y_MIN, MapObjectProperty.Particle.OFFSET_Y_MAX, 0));
    data.setParticleWidth(range(mapObject, MapObjectProperty.Particle.STARTWIDTH_MIN, MapObjectProperty.Particle.STARTWIDTH_MAX, 0));
    data.setParticleHeight(range(mapObject, MapObjectProperty.Particle.STARTHEIGHT_MIN, MapObjectProperty.Particle.STARTHEIGHT_MAX, 0));
    data.setVelocityX(range(mapObject, MapObjectProperty.Particle.VELOCITY_X_MIN, MapObjectProperty.Particle.VELOCITY_X_MAX, 0));
    data.setVelocityY(range(mapObject, MapObjectProperty.Particle.VELOCITY_Y_MIN, MapObjectProperty.Particle.VELOCITY_Y_MAX, 0));
    data.setAccelerationX(range(mapObject, MapObjectProperty.Particle.ACCELERATION_X_MIN, MapObjectProperty.Particle.ACCELERATION_X_MAX, 0));
    data.setAccelerationY(range(mapObject, MapObjectProperty.Particle.ACCELERATION_Y_MIN, MapObjectProperty.Particle.ACCELERATION_Y_MAX, 0));
    data.setDeltaWidth(range(mapObject, MapObjectProperty.Particle.DELTAWIDTH_MIN, MapObjectProperty.Particle.DELTAWIDTH_MAX, 0));
    data.setDeltaHeight(range(mapObject, MapObjectProperty.Particle.DELTAHEIGHT_MIN, MapObjectProperty.Particle.DELTAHEIGHT_MAX, 0));
    data.setAngle(range(mapObject, MapObjectProperty.Particle.ANGLE_MIN, MapObjectProperty.Particle.ANGLE_MAX, 0));
    data.setDeltaRotation(range(mapObject, MapObjectProperty.Particle.DELTA_ANGLE_MIN, MapObjectProperty.Particle.DELTA_ANGLE_MAX, 0));
    data.setParticleTTL(longRange(mapObject, MapObjectProperty.Particle.TTL_MIN, MapObjectProperty.Particle.TTL_MAX, 0L));
    data.setOutlineThickness(range(mapObject, Particle.OUTLINETHICKNESS_MIN, MapObjectProperty.Particle.OUTLINETHICKNESS_MAX, 1f));

    data.setCollision(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class, EmitterAttributes.DEFAULT_COLLISION));
    data.setRequiredQuality(mapObject.getEnumValue(MapObjectProperty.REQUIRED_QUALITY, Quality.class, EmitterAttributes.DEFAULT_REQUIRED_QUALITY));

    data.setTexts(mapObject.getCommaSeparatedStringValues(MapObjectProperty.Particle.TEXTS, EmitterAttributes.DEFAULT_TEXT));
    data.setSpritesheet(mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null));
    data.setAnimateSprite(mapObject.getBoolValue(MapObjectProperty.Particle.ANIMATESPRITE, false));
    data.setLoopSprite(mapObject.getBoolValue(MapObjectProperty.Particle.LOOPSPRITE, false));
    data.setFade(mapObject.getBoolValue(MapObjectProperty.Particle.FADE, false));
    data.setFadeOnCollision(mapObject.getBoolValue(MapObjectProperty.Particle.FADEONCOLLISION, false));
    data.setOutlineOnly(mapObject.getBoolValue(MapObjectProperty.Particle.OUTLINEONLY, false));
    data.setAntiAliasing(mapObject.getBoolValue(MapObjectProperty.Particle.ANTIALIASING, false));
    return data;
  }

  private static RangeAttribute<Float> range(IMapObject mapObject, String minProperty, String maxProperty, float defaultValue) {
    float min = mapObject.getFloatValue(minProperty, defaultValue);
    float max = mapObject.getFloatValue(maxProperty, defaultValue);
    if (!Float.isFinite(min) || !Float.isFinite(max)) {
      log.warning("Emitter " + mapObject.getId() + " has non-finite " + minProperty + "/" + maxProperty + "; using defaults.");
      return new RangeAttribute<>(defaultValue, defaultValue);
    }
    if (min > max) {
      log.warning("Emitter " + mapObject.getId() + " has reversed " + minProperty + "/" + maxProperty + "; swapping values.");
      return new RangeAttribute<>(max, min);
    }
    return new RangeAttribute<>(min, max);
  }

  private static RangeAttribute<Long> longRange(IMapObject mapObject, String minProperty, String maxProperty, long defaultValue) {
    long min = mapObject.getLongValue(minProperty, defaultValue);
    long max = mapObject.getLongValue(maxProperty, defaultValue);
    if (min > max) {
      log.warning("Emitter " + mapObject.getId() + " has reversed " + minProperty + "/" + maxProperty + "; swapping values.");
      return new RangeAttribute<>(max, min);
    }
    return new RangeAttribute<>(min, max);
  }

  public static void updateMapObject(EmitterAttributes emitterData, IMapObject mo) {
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

    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MAX, emitterData.getAccelerationX().getMax());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MIN, emitterData.getAccelerationX().getMin());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX, emitterData.getAccelerationY().getMax());
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN, emitterData.getAccelerationY().getMin());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MAX, emitterData.getAngle().getMax());
    mo.setValue(MapObjectProperty.Particle.ANGLE_MIN, emitterData.getAngle().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX, emitterData.getDeltaAngle().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN, emitterData.getDeltaAngle().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX, emitterData.getDeltaHeight().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN, emitterData.getDeltaHeight().getMin());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MAX, emitterData.getDeltaWidth().getMax());
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MIN, emitterData.getDeltaWidth().getMin());
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MAX, emitterData.getParticleOffsetX().getMax());
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MIN, emitterData.getParticleOffsetX().getMin());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MAX, emitterData.getParticleOffsetY().getMax());
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MIN, emitterData.getParticleOffsetY().getMin());
    mo.setValue(MapObjectProperty.Particle.OUTLINETHICKNESS_MAX, emitterData.getOutlineThickness().getMax());
    mo.setValue(MapObjectProperty.Particle.OUTLINETHICKNESS_MIN, emitterData.getOutlineThickness().getMin());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MAX, emitterData.getParticleHeight().getMax());
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MIN, emitterData.getParticleHeight().getMin());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MAX, emitterData.getParticleWidth().getMax());
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MIN, emitterData.getParticleWidth().getMin());
    mo.setValue(MapObjectProperty.Particle.TTL_MAX, emitterData.getParticleTTL().getMax());
    mo.setValue(MapObjectProperty.Particle.TTL_MIN, emitterData.getParticleTTL().getMin());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MAX, emitterData.getVelocityX().getMax());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MIN, emitterData.getVelocityX().getMin());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MAX, emitterData.getVelocityY().getMax());
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MIN, emitterData.getVelocityY().getMin());

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

  public static IMapObject createMapObject(EmitterAttributes emitterData) {
    MapObject newMapObject = new MapObject();
    updateMapObject(emitterData, newMapObject);
    return newMapObject;
  }
}
