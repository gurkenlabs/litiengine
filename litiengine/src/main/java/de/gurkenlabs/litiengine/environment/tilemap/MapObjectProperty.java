package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the built-in custom property names that LITIengine recognizes on Tiled {@link IMapObject} instances.
 * <p>
 * These string constants are used as keys for properties attached to map objects in {@code .tmx} files and are referenced by the various
 * {@code MapObjectLoader} implementations to configure entities at load time.
 * </p>
 */
public final class MapObjectProperty {
  private static final Logger log = Logger.getLogger(MapObjectProperty.class.getName());

  /**
   * Comma-separated list of tags assigned to an entity.
   */
  public static final String TAGS = "tags";
  /**
   * Render type used when drawing the entity.
   */
  public static final String RENDERTYPE = "renderType";
  /** Whether the entity should be rendered together with its containing layer. */
  public static final String RENDERWITHLAYER = "renderWithLayer";
  /** Minimum graphics quality required for the entity to be rendered. */
  public static final String REQUIRED_QUALITY = "requiredQuality";

  // collision entity
  /** Whether collision is enabled for the entity. */
  public static final String COLLISION = "collision";
  /** Horizontal alignment of the entity's collision box. */
  public static final String COLLISION_ALIGN = "collisionAlign";
  /** Vertical alignment of the entity's collision box. */
  public static final String COLLISION_VALIGN = "collisionValign";
  /** Type of collision used by the entity (static, dynamic, etc.). */
  public static final String COLLISION_TYPE = "collisionType";

  // collision box
  /** Height of the entity's collision box, in pixels. */
  public static final String COLLISIONBOX_HEIGHT = "collisionboxHeight";
  /** Width of the entity's collision box, in pixels. */
  public static final String COLLISIONBOX_WIDTH = "collisionboxWidth";
  /** Whether the collision box obstructs light sources. */
  public static final String COLLISIONBOX_OBSTRUCTINGLIGHTS = "isObstructingLight";

  // general entity stuff
  /** Name of the spritesheet associated with the entity. */
  public static final String SPRITESHEETNAME = "spritesheetName";
  /** Whether the entity's sprite should be scaled to fit its bounding box. */
  public static final String SCALE_SPRITE = "scaling";

  // mobile entity
  /** Acceleration value of a mobile entity, in milliseconds to reach max velocity. */
  public static final String MOVEMENT_ACCELERATION = "acceleration";
  /** Deceleration value of a mobile entity, in milliseconds to come to a stop. */
  public static final String MOVEMENT_DECELERATION = "deceleration";
  /** Maximum movement velocity of a mobile entity, in pixels per second. */
  public static final String MOVEMENT_VELOCITY = "velocity";
  /** Whether the entity should rotate towards its movement direction. */
  public static final String MOVEMENT_TURNONMOVE = "turnOnMove";

  // combat entity
  /** Hit points of a combat entity. */
  public static final String COMBAT_HITPOINTS = "hitpoints";
  /** Whether the combat entity is indestructible. */
  public static final String COMBAT_INDESTRUCTIBLE = "indestructible";
  /** Identifier of the team the combat entity belongs to. */
  public static final String COMBAT_TEAM = "team";

  // props
  /** Material of a prop, used to determine sound effects and damage interactions. */
  public static final String PROP_MATERIAL = "material";
  /** Whether the prop should cast a shadow. */
  public static final String PROP_ADDSHADOW = "addShadow";
  /** Sprite rotation of the prop. */
  public static final String PROP_ROTATION = "rotationSprite";
  /** Whether the prop's sprite is flipped horizontally. */
  public static final String PROP_FLIPHORIZONTALLY = "flipHorizontally";
  /** Whether the prop's sprite is flipped vertically. */
  public static final String PROP_FLIPVERTICALLY = "flipVertically";

  // light source
  /** Color of the emitted light, encoded as a string. */
  public static final String LIGHT_COLOR = "lightColor";
  /** Intensity of the emitted light. */
  public static final String LIGHT_INTENSITY = "lightIntensity";
  /** Shape of the light source (e.g. rectangle, ellipse). */
  public static final String LIGHT_SHAPE = "lightShape";
  /** Whether the light source is initially active. */
  public static final String LIGHT_ACTIVE = "lightActive";

  // sound source
  /** Volume modifier of the sound source. */
  public static final String SOUND_VOLUME = "soundVolume";
  /** Whether the sound source loops its playback. */
  public static final String SOUND_LOOP = "soundLoop";
  /** Name of the sound resource played by the source. */
  public static final String SOUND_NAME = "soundName";
  /** Range, in pixels, within which the sound is audible. */
  public static final String SOUND_RANGE = "soundRange";

  // static shadow
  /** Type of static shadow cast (e.g. rectangle, ellipse). */
  public static final String SHADOW_TYPE = "shadowType";
  /** Pixel offset applied to the static shadow. */
  public static final String SHADOW_OFFSET = "shadowOffset";

  // spawnpoint
  /** Free-form spawn information string passed to the spawned entity. */
  public static final String SPAWN_INFO = "spawnInfo";
  /** Initial facing direction of the spawned entity. */
  public static final String SPAWN_DIRECTION = "spawnDirection";
  /** Pivot point used when positioning the spawned entity. */
  public static final String SPAWN_PIVOT = "spawnPivot";
  /** Horizontal offset of the spawn pivot. */
  public static final String SPAWN_PIVOT_OFFSETX = "spawnPivotOffsetX";
  /** Vertical offset of the spawn pivot. */
  public static final String SPAWN_PIVOT_OFFSETY = "spawnPivotOffsetY";

  // trigger
  /** Activation mode of a trigger (interact, collision, etc.). */
  public static final String TRIGGER_ACTIVATION = "triggerActivation";
  /** Message dispatched when the trigger fires. */
  public static final String TRIGGER_MESSAGE = "triggermessage";
  /** Whether the trigger can fire only once. */
  public static final String TRIGGER_ONETIME = "triggerOneTime";
  /** Identifiers of entities that may activate the trigger. */
  public static final String TRIGGER_ACTIVATORS = "triggerActivators";
  /** Identifiers of entities that receive the trigger message. */
  public static final String TRIGGER_TARGETS = "triggerTarget";
  /** Cooldown, in milliseconds, between subsequent activations of the trigger. */
  public static final String TRIGGER_COOLDOWN = "triggerCooldown";

  private static final List<String> availableProperties = new ArrayList<>();

  /**
   * Property names specific to particle emitter map objects.
   */
  public static final class Emitter {
    /** Encoded list of emitter colors. */
    public static final String COLORS = "emitterColors";
    /** Encoded list of probabilities per emitter color. */
    public static final String COLORPROBABILITIES = "emitterColorProbabilities";
    /** Spawn rate in milliseconds between spawn ticks. */
    public static final String SPAWNRATE = "emitterSpawnRate";
    /** Number of particles spawned per spawn tick. */
    public static final String SPAWNAMOUNT = "emitterSpawnAmount";
    /** Update delay of the emitter, in milliseconds. */
    public static final String UPDATERATE = "emitterUpdateDelay";
    /** Total duration of the emitter, in milliseconds; {@code 0} means infinite. */
    public static final String DURATION = "emitterDuration";
    /** Maximum number of concurrently alive particles. */
    public static final String MAXPARTICLES = "emitterMaxParticles";
    /** Default particle type/shape used by the emitter. */
    public static final String PARTICLETYPE = "emitterParticleType";
    /** Per-particle color variance in the range {@code [0, 1]}. */
    public static final String COLORVARIANCE = "emitterColorVariance";
    /** Per-particle alpha variance in the range {@code [0, 1]}. */
    public static final String ALPHAVARIANCE = "emitterAlphaVariance";
    /** Horizontal alignment of the emitter origin. */
    public static final String ORIGIN_ALIGN = "emitterOriginAlign";
    /** Vertical alignment of the emitter origin. */
    public static final String ORIGIN_VALIGN = "emitterOriginValign";

    private Emitter() {
    }
  }

  /**
   * Property names specific to particle configuration on emitter map objects.
   */
  public static final class Particle {
    /** Minimum horizontal spawn offset, in pixels. */
    public static final String OFFSET_X_MIN = "particleMinOffsetX";
    /** Maximum horizontal spawn offset, in pixels. */
    public static final String OFFSET_X_MAX = "particleMaxOffsetX";
    /** Minimum vertical spawn offset, in pixels. */
    public static final String OFFSET_Y_MIN = "particleMinOffsetY";
    /** Maximum vertical spawn offset, in pixels. */
    public static final String OFFSET_Y_MAX = "particleMaxOffsetY";
    /** Minimum horizontal initial velocity. */
    public static final String VELOCITY_X_MIN = "particleMinVelocityX";
    /** Maximum horizontal initial velocity. */
    public static final String VELOCITY_X_MAX = "particleMaxVelocityX";
    /** Minimum vertical initial velocity. */
    public static final String VELOCITY_Y_MIN = "particleMinVelocityY";
    /** Maximum vertical initial velocity. */
    public static final String VELOCITY_Y_MAX = "particleMaxVelocityY";
    /** Minimum horizontal acceleration. */
    public static final String ACCELERATION_X_MIN = "particleMinAccelerationX";
    /** Maximum horizontal acceleration. */
    public static final String ACCELERATION_X_MAX = "particleMaxAccelerationX";
    /** Minimum vertical acceleration. */
    public static final String ACCELERATION_Y_MIN = "particleMinAccelerationY";
    /** Maximum vertical acceleration. */
    public static final String ACCELERATION_Y_MAX = "particleMaxAccelerationY";
    /** Minimum initial particle width, in pixels. */
    public static final String STARTWIDTH_MIN = "particleMinStartWidth";
    /** Maximum initial particle width, in pixels. */
    public static final String STARTWIDTH_MAX = "particleMaxStartWidth";
    /** Minimum initial particle height, in pixels. */
    public static final String STARTHEIGHT_MIN = "particleMinStartHeight";
    /** Maximum initial particle height, in pixels. */
    public static final String STARTHEIGHT_MAX = "particleMaxStartHeight";
    /** Minimum per-update width delta. */
    public static final String DELTAWIDTH_MIN = "particleMinDeltaWidth";
    /** Maximum per-update width delta. */
    public static final String DELTAWIDTH_MAX = "particleMaxDeltaWidth";
    /** Minimum per-update height delta. */
    public static final String DELTAHEIGHT_MIN = "particleMinDeltaHeight";
    /** Maximum per-update height delta. */
    public static final String DELTAHEIGHT_MAX = "particleMaxDeltaHeight";
    /** Minimum initial angle, in degrees. */
    public static final String ANGLE_MIN = "particleMinAngle";
    /** Maximum initial angle, in degrees. */
    public static final String ANGLE_MAX = "particleMaxAngle";
    /** Minimum per-update angle delta, in degrees. */
    public static final String DELTA_ANGLE_MIN = "particleMinDeltaAngle";
    /** Maximum per-update angle delta, in degrees. */
    public static final String DELTA_ANGLE_MAX = "particleMaxDeltaAngle";
    /** Minimum particle time-to-live, in milliseconds. */
    public static final String TTL_MIN = "particleMinTTL";
    /** Maximum particle time-to-live, in milliseconds. */
    public static final String TTL_MAX = "particleMaxTTL";
    /** Whether sprite-based particles are animated. */
    public static final String ANIMATESPRITE = "particleAnimateSprite";
    /** Whether sprite-based particle animations are looped. */
    public static final String LOOPSPRITE = "particleLoopSprite";
    /** Encoded list of texts used by text-rendering particles. */
    public static final String TEXTS = "particleTexts";
    /** Whether particles fade out over their lifetime. */
    public static final String FADE = "particleFade";
    /** Whether particles fade upon collision. */
    public static final String FADEONCOLLISION = "particleFadeOnCollision";
    /** Whether shape-based particles are rendered as outlines only. */
    public static final String OUTLINEONLY = "particleOutlineOnly";
    /** Minimum outline thickness for outline-rendered particles. */
    public static final String OUTLINETHICKNESS_MIN = "particleMinOutlineThickness";
    /** Maximum outline thickness for outline-rendered particles. */
    public static final String OUTLINETHICKNESS_MAX = "particleMaxOutlineThickness";
    /** Whether particles are rendered with anti-aliasing. */
    public static final String ANTIALIASING = "particleAntiAliasing";

    private Particle() {

    }
  }

  private MapObjectProperty() {
  }

  /**
   * Returns the lazily-computed list of all built-in property names defined by this class and its nested {@link Emitter} and {@link Particle} classes.
   *
   * @return the list of available built-in property names
   */
  public static List<String> getAvailableProperties() {
    if (availableProperties.isEmpty()) {
      addAvailableProperties(MapObjectProperty.class);
      addAvailableProperties(MapObjectProperty.Emitter.class);
      addAvailableProperties(MapObjectProperty.Particle.class);
    }

    return availableProperties;
  }

  /**
   * Determines whether the given property name is a custom (user-defined) property, i.e. not part of the built-in property names.
   *
   * @param name the property name to test
   * @return {@code true} if the name is not part of the built-in properties; {@code false} otherwise
   */
  public static boolean isCustom(final String name) {
    return getAvailableProperties().stream().noneMatch(x -> x.equalsIgnoreCase(name));
  }

  private static void addAvailableProperties(Class<?> clz) {
    for (final Field field : clz.getDeclaredFields()) {
      if (field.getType() == String.class && Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
        try {
          availableProperties.add((String) field.get(null));
        } catch (final IllegalArgumentException | IllegalAccessException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }
  }
}
