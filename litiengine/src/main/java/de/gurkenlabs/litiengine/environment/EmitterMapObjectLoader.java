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
import java.util.List;
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
    mo.setValue(MapObjectProperty.Emitter.PARTICLETYPE,
      emitterData.getParticleType() != null ? emitterData.getParticleType() : EmitterAttributes.DEFAULT_PARTICLE_TYPE);
    mo.setValue(MapObjectProperty.Emitter.COLORVARIANCE, emitterData.getColorVariance());
    mo.setValue(MapObjectProperty.Emitter.ALPHAVARIANCE, emitterData.getAlphaVariance());
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_ALIGN,
      emitterData.getOriginAlign() != null ? emitterData.getOriginAlign() : EmitterAttributes.DEFAULT_ORIGIN_ALIGN);
    mo.setValue(MapObjectProperty.Emitter.ORIGIN_VALIGN,
      emitterData.getOriginValign() != null ? emitterData.getOriginValign() : EmitterAttributes.DEFAULT_ORIGIN_VALIGN);

    List<String> colors = emitterData.getColors();
    String commaSeperatedColors = ArrayUtilities.join(colors != null && !colors.isEmpty()
      ? colors
      : List.of(ColorHelper.encode(EmitterAttributes.DEFAULT_COLOR)));
    mo.setValue(MapObjectProperty.Emitter.COLORS, commaSeperatedColors);

    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MAX,
      rangeMax(emitterData.getAccelerationX(), EmitterAttributes.DEFAULT_MAX_ACCELERATION_X));
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_X_MIN,
      rangeMin(emitterData.getAccelerationX(), EmitterAttributes.DEFAULT_MIN_ACCELERATION_X));
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MAX,
      rangeMax(emitterData.getAccelerationY(), EmitterAttributes.DEFAULT_MAX_ACCELERATION_Y));
    mo.setValue(MapObjectProperty.Particle.ACCELERATION_Y_MIN,
      rangeMin(emitterData.getAccelerationY(), EmitterAttributes.DEFAULT_MIN_ACCELERATION_Y));
    mo.setValue(MapObjectProperty.Particle.ANGLE_MAX,
      rangeMax(emitterData.getAngle(), EmitterAttributes.DEFAULT_MAX_ROTATION));
    mo.setValue(MapObjectProperty.Particle.ANGLE_MIN,
      rangeMin(emitterData.getAngle(), EmitterAttributes.DEFAULT_MIN_ANGLE));
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MAX,
      rangeMax(emitterData.getDeltaAngle(), EmitterAttributes.DEFAULT_MAX_DELTA_ANGLE));
    mo.setValue(MapObjectProperty.Particle.DELTA_ANGLE_MIN,
      rangeMin(emitterData.getDeltaAngle(), EmitterAttributes.DEFAULT_MIN_DELTA_ANGLE));
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MAX,
      rangeMax(emitterData.getDeltaHeight(), EmitterAttributes.DEFAULT_MAX_DELTA_HEIGHT));
    mo.setValue(MapObjectProperty.Particle.DELTAHEIGHT_MIN,
      rangeMin(emitterData.getDeltaHeight(), EmitterAttributes.DEFAULT_MIN_DELTA_HEIGHT));
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MAX,
      rangeMax(emitterData.getDeltaWidth(), EmitterAttributes.DEFAULT_MAX_DELTA_WIDTH));
    mo.setValue(MapObjectProperty.Particle.DELTAWIDTH_MIN,
      rangeMin(emitterData.getDeltaWidth(), EmitterAttributes.DEFAULT_MIN_DELTA_WIDTH));
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MAX,
      rangeMax(emitterData.getParticleOffsetX(), EmitterAttributes.DEFAULT_MAX_OFFSET_X));
    mo.setValue(MapObjectProperty.Particle.OFFSET_X_MIN,
      rangeMin(emitterData.getParticleOffsetX(), EmitterAttributes.DEFAULT_MIN_OFFSET_X));
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MAX,
      rangeMax(emitterData.getParticleOffsetY(), EmitterAttributes.DEFAULT_MAX_OFFSET_Y));
    mo.setValue(MapObjectProperty.Particle.OFFSET_Y_MIN,
      rangeMin(emitterData.getParticleOffsetY(), EmitterAttributes.DEFAULT_MIN_OFFSET_Y));
    mo.setValue(MapObjectProperty.Particle.OUTLINETHICKNESS_MAX,
      rangeMax(emitterData.getOutlineThickness(), EmitterAttributes.DEFAULT_MAX_OUTLINETHICKNESS));
    mo.setValue(MapObjectProperty.Particle.OUTLINETHICKNESS_MIN,
      rangeMin(emitterData.getOutlineThickness(), EmitterAttributes.DEFAULT_MIN_OUTLINETHICKNESS));
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MAX,
      rangeMax(emitterData.getParticleHeight(), EmitterAttributes.DEFAULT_MAX_HEIGHT));
    mo.setValue(MapObjectProperty.Particle.STARTHEIGHT_MIN,
      rangeMin(emitterData.getParticleHeight(), EmitterAttributes.DEFAULT_MIN_HEIGHT));
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MAX,
      rangeMax(emitterData.getParticleWidth(), EmitterAttributes.DEFAULT_MAX_WIDTH));
    mo.setValue(MapObjectProperty.Particle.STARTWIDTH_MIN,
      rangeMin(emitterData.getParticleWidth(), EmitterAttributes.DEFAULT_MIN_WIDTH));
    mo.setValue(MapObjectProperty.Particle.TTL_MAX,
      rangeMax(emitterData.getParticleTTL(), (long) EmitterAttributes.DEFAULT_MAX_PARTICLE_TTL));
    mo.setValue(MapObjectProperty.Particle.TTL_MIN,
      rangeMin(emitterData.getParticleTTL(), (long) EmitterAttributes.DEFAULT_MIN_PARTICLE_TTL));
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MAX,
      rangeMax(emitterData.getVelocityX(), EmitterAttributes.DEFAULT_MAX_VELOCITY_X));
    mo.setValue(MapObjectProperty.Particle.VELOCITY_X_MIN,
      rangeMin(emitterData.getVelocityX(), EmitterAttributes.DEFAULT_MIN_VELOCITY_X));
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MAX,
      rangeMax(emitterData.getVelocityY(), EmitterAttributes.DEFAULT_MAX_VELOCITY_Y));
    mo.setValue(MapObjectProperty.Particle.VELOCITY_Y_MIN,
      rangeMin(emitterData.getVelocityY(), EmitterAttributes.DEFAULT_MIN_VELOCITY_Y));

    mo.setValue(MapObjectProperty.COLLISION_TYPE,
      emitterData.getCollision() != null ? emitterData.getCollision() : EmitterAttributes.DEFAULT_COLLISION);
    mo.setValue(MapObjectProperty.REQUIRED_QUALITY,
      emitterData.getRequiredQuality() != null ? emitterData.getRequiredQuality() : EmitterAttributes.DEFAULT_REQUIRED_QUALITY);
    List<String> texts = emitterData.getTexts();
    String commaSeperatedTexts = ArrayUtilities.join(texts != null && !texts.isEmpty()
      ? texts
      : List.of(EmitterAttributes.DEFAULT_TEXT));
    mo.setValue(MapObjectProperty.Particle.TEXTS, commaSeperatedTexts);

    mo.setValue(MapObjectProperty.SPRITESHEETNAME,
      emitterData.getSpritesheet() != null ? emitterData.getSpritesheet() : EmitterAttributes.DEFAULT_SPRITESHEET);
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

  private static <T extends Number & Comparable<T>> T rangeMin(RangeAttribute<T> range, T defaultValue) {
    return range != null && range.getMin() != null ? range.getMin() : defaultValue;
  }

  private static <T extends Number & Comparable<T>> T rangeMax(RangeAttribute<T> range, T defaultValue) {
    return range != null && range.getMax() != null ? range.getMax() : defaultValue;
  }
}
