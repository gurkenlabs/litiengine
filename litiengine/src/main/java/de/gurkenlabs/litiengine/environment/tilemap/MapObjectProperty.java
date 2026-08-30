package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Defines the built-in custom property names that LITIengine recognizes on Tiled [IMapObject] instances.
///
/// These string constants are used as keys for properties attached to map objects in `.tmx` files and are referenced by the various
/// `MapObjectLoader` implementations to configure entities at load time.
public final class MapObjectProperty {
  private static final Logger log = Logger.getLogger(MapObjectProperty.class.getName());

  /// Comma-separated list of tags assigned to an entity.
  @TmxPropertyInfo(name = "tags", description = "Comma-separated list of tags assigned to an entity.", category = "General", type = "string")
  public static final String TAGS = "tags";

  /// Render type used when drawing the entity.
  @TmxPropertyInfo(name = "renderType", description = "Render type used when drawing the entity (BACKGROUND, GROUND, SURFACE, NORMAL, OVERLAY).", category = "Graphics", type = "enum", defaultValue = "NORMAL")
  public static final String RENDERTYPE = "renderType";

  /// Whether the entity should be rendered together with its containing layer.
  @TmxPropertyInfo(name = "renderWithLayer", description = "Whether the entity should be rendered together with its containing layer.", category = "Graphics", type = "boolean", defaultValue = "false")
  public static final String RENDERWITHLAYER = "renderWithLayer";

  /// Minimum graphics quality required for the entity to be rendered.
  @TmxPropertyInfo(name = "requiredQuality", description = "Minimum graphics quality required for the entity to be rendered.", category = "Graphics", type = "enum", defaultValue = "VERYLOW")
  public static final String REQUIRED_QUALITY = "requiredQuality";

  // collision entity
  /// Whether collision is enabled for the entity.
  @TmxPropertyInfo(name = "collision", description = "Whether collision is enabled for the entity.", category = "Collision", type = "boolean", defaultValue = "false")
  public static final String COLLISION = "collision";

  /// Horizontal alignment of the entity's collision box.
  @TmxPropertyInfo(name = "collisionAlign", description = "Horizontal alignment of the entity's collision box (LEFT, CENTER, RIGHT).", category = "Collision", type = "enum", defaultValue = "CENTER")
  public static final String COLLISION_ALIGN = "collisionAlign";

  /// Vertical alignment of the entity's collision box.
  @TmxPropertyInfo(name = "collisionValign", description = "Vertical alignment of the entity's collision box (TOP, MIDDLE, DOWN).", category = "Collision", type = "enum", defaultValue = "DOWN")
  public static final String COLLISION_VALIGN = "collisionValign";

  /// Type of collision used by the entity (static, dynamic, etc.).
  @TmxPropertyInfo(name = "collisionType", description = "Type of collision used by the entity (STATIC, DYNAMIC).", category = "Collision", type = "enum", defaultValue = "STATIC")
  public static final String COLLISION_TYPE = "collisionType";

  // collision box
  /// Height of the entity's collision box, in pixels.
  @TmxPropertyInfo(name = "collisionboxHeight", description = "Height of the entity's collision box in pixels.", category = "Collision", type = "float")
  public static final String COLLISIONBOX_HEIGHT = "collisionboxHeight";

  /// Width of the entity's collision box, in pixels.
  @TmxPropertyInfo(name = "collisionboxWidth", description = "Width of the entity's collision box in pixels.", category = "Collision", type = "float")
  public static final String COLLISIONBOX_WIDTH = "collisionboxWidth";

  /// Whether the collision box obstructs light sources.
  @TmxPropertyInfo(name = "isObstructingLight", description = "Whether the collision box obstructs light sources.", category = "Collision", type = "boolean", defaultValue = "false")
  public static final String COLLISIONBOX_OBSTRUCTINGLIGHTS = "isObstructingLight";

  // general entity stuff
  /// Name of the spritesheet associated with the entity.
  @TmxPropertyInfo(name = "spritesheetName", description = "Name of the spritesheet associated with the entity.", category = "Graphics", type = "string")
  public static final String SPRITESHEETNAME = "spritesheetName";

  /// Whether the entity's sprite should be scaled to fit its bounding box.
  @TmxPropertyInfo(name = "scaling", description = "Whether the entity's sprite should be scaled to fit its bounding box.", category = "Graphics", type = "boolean", defaultValue = "false")
  public static final String SCALE_SPRITE = "scaling";

  /// Project-defined implementation identifier for a built-in map object type.
  @TmxPropertyInfo(name = "implementation", description = "Project-defined implementation identifier for a built-in map object type.", category = "General", type = "string")
  public static final String IMPLEMENTATION = "implementation";

  /// Versioned JSON bindings for scripts attached to an entity.
  @TmxPropertyInfo(name = "scriptBindings", description = "Structured Java or runtime script bindings assigned to this entity.", category = "Behavior", type = "scripts")
  public static final String SCRIPT_BINDINGS = "scriptBindings";

  // mobile entity
  /// Acceleration value of a mobile entity, in milliseconds to reach max velocity.
  @TmxPropertyInfo(name = "acceleration", description = "Acceleration value of a mobile entity in milliseconds to reach max velocity.", category = "Movement", type = "int", defaultValue = "0")
  public static final String MOVEMENT_ACCELERATION = "acceleration";

  /// Deceleration value of a mobile entity, in milliseconds to come to a stop.
  @TmxPropertyInfo(name = "deceleration", description = "Deceleration value of a mobile entity in milliseconds to come to a stop.", category = "Movement", type = "int", defaultValue = "0")
  public static final String MOVEMENT_DECELERATION = "deceleration";

  /// Maximum movement velocity of a mobile entity, in pixels per second.
  @TmxPropertyInfo(name = "velocity", description = "Maximum movement velocity of a mobile entity in pixels per second.", category = "Movement", type = "float", defaultValue = "100")
  public static final String MOVEMENT_VELOCITY = "velocity";

  /// Whether the entity should rotate towards its movement direction.
  @TmxPropertyInfo(name = "turnOnMove", description = "Whether the entity should rotate towards its movement direction.", category = "Movement", type = "boolean", defaultValue = "true")
  public static final String MOVEMENT_TURNONMOVE = "turnOnMove";

  // combat entity
  /// Hit points of a combat entity.
  @TmxPropertyInfo(name = "hitpoints", description = "Maximum hit points of a combat entity.", category = "Combat", type = "int", defaultValue = "100")
  public static final String COMBAT_HITPOINTS = "hitpoints";

  /// Initial current hit points of a combat entity.
  @TmxPropertyInfo(name = "currentHitpoints", description = "Initial current hit points of a combat entity.", category = "Combat", type = "int", defaultValue = "100")
  public static final String COMBAT_CURRENT_HITPOINTS = "currentHitpoints";

  /// Whether the combat entity is indestructible.
  @TmxPropertyInfo(name = "indestructible", description = "Whether the combat entity is indestructible.", category = "Combat", type = "boolean", defaultValue = "false")
  public static final String COMBAT_INDESTRUCTIBLE = "indestructible";

  /// Identifier of the team the combat entity belongs to.
  @TmxPropertyInfo(name = "team", description = "Identifier of the team the combat entity belongs to.", category = "Combat", type = "int", defaultValue = "0")
  public static final String COMBAT_TEAM = "team";

  // props
  /// Material of a prop, used to determine sound effects and damage interactions.
  @TmxPropertyInfo(name = "material", description = "Material of a prop used to determine sound effects and damage interactions.", category = "Prop", type = "string")
  public static final String PROP_MATERIAL = "material";

  /// Whether the prop should cast a shadow.
  @TmxPropertyInfo(name = "addShadow", description = "Whether the prop should cast a shadow.", category = "Prop", type = "boolean", defaultValue = "false")
  public static final String PROP_ADDSHADOW = "addShadow";

  /// Sprite rotation of the prop.
  @TmxPropertyInfo(name = "rotationSprite", description = "Sprite rotation angle of the prop.", category = "Prop", type = "int", defaultValue = "0")
  public static final String PROP_ROTATION = "rotationSprite";

  /// Whether the prop's sprite is flipped horizontally.
  @TmxPropertyInfo(name = "flipHorizontally", description = "Whether the prop's sprite is flipped horizontally.", category = "Prop", type = "boolean", defaultValue = "false")
  public static final String PROP_FLIPHORIZONTALLY = "flipHorizontally";

  /// Whether the prop's sprite is flipped vertically.
  @TmxPropertyInfo(name = "flipVertically", description = "Whether the prop's sprite is flipped vertically.", category = "Prop", type = "boolean", defaultValue = "false")
  public static final String PROP_FLIPVERTICALLY = "flipVertically";

  // light source
  /// Color of the emitted light, encoded as a string.
  @TmxPropertyInfo(name = "lightColor", description = "Color of the emitted light encoded as a hex string.", category = "Light", type = "color", defaultValue = "#FFFFFF")
  public static final String LIGHT_COLOR = "lightColor";

  /// Intensity of the emitted light.
  @TmxPropertyInfo(name = "lightIntensity", description = "Intensity of the emitted light (0 to 255).", category = "Light", type = "int", defaultValue = "100")
  public static final String LIGHT_INTENSITY = "lightIntensity";

  /// Shape of the light source (e.g. rectangle, ellipse).
  @TmxPropertyInfo(name = "lightShape", description = "Shape of the light source (ELLIPSE, RECTANGLE).", category = "Light", type = "enum", defaultValue = "ELLIPSE")
  public static final String LIGHT_SHAPE = "lightShape";

  /// Whether the light source is initially active.
  @TmxPropertyInfo(name = "lightActive", description = "Whether the light source is initially active.", category = "Light", type = "boolean", defaultValue = "true")
  public static final String LIGHT_ACTIVE = "lightActive";

  // sound source
  /// Volume modifier of the sound source.
  @TmxPropertyInfo(name = "soundVolume", description = "Volume modifier of the sound source (0.0 to 1.0).", category = "Sound", type = "float", defaultValue = "1.0")
  public static final String SOUND_VOLUME = "soundVolume";

  /// Whether the sound source loops its playback.
  @TmxPropertyInfo(name = "soundLoop", description = "Whether the sound source loops its playback.", category = "Sound", type = "boolean", defaultValue = "true")
  public static final String SOUND_LOOP = "soundLoop";

  /// Name of the sound resource played by the source.
  @TmxPropertyInfo(name = "soundName", description = "Name of the sound resource played by the source.", category = "Sound", type = "string")
  public static final String SOUND_NAME = "soundName";

  /// Range, in pixels, within which the sound is audible.
  @TmxPropertyInfo(name = "soundRange", description = "Range in pixels within which the sound is audible.", category = "Sound", type = "float", defaultValue = "100")
  public static final String SOUND_RANGE = "soundRange";

  // static shadow
  /// Type of static shadow cast (e.g. rectangle, ellipse).
  @TmxPropertyInfo(name = "shadowType", description = "Type of static shadow cast (NONE, RECTANGLE, ELLIPSE).", category = "Shadow", type = "enum", defaultValue = "RECTANGLE")
  public static final String SHADOW_TYPE = "shadowType";

  /// Pixel offset applied to the static shadow.
  @TmxPropertyInfo(name = "shadowOffset", description = "Pixel offset applied to the static shadow.", category = "Shadow", type = "float", defaultValue = "0")
  public static final String SHADOW_OFFSET = "shadowOffset";

  // spawnpoint
  /// Free-form spawn information string passed to the spawned entity.
  @TmxPropertyInfo(name = "spawnInfo", description = "Free-form spawn information string passed to spawned entities.", category = "Spawn", type = "string")
  public static final String SPAWN_INFO = "spawnInfo";

  /// Initial facing direction of the spawned entity.
  @TmxPropertyInfo(name = "spawnDirection", description = "Initial facing direction of the spawned entity (UP, DOWN, LEFT, RIGHT).", category = "Spawn", type = "enum", defaultValue = "DOWN")
  public static final String SPAWN_DIRECTION = "spawnDirection";

  /// Pivot point used when positioning the spawned entity.
  @TmxPropertyInfo(name = "spawnPivot", description = "Pivot point used when positioning the spawned entity.", category = "Spawn", type = "enum", defaultValue = "LOCATION")
  public static final String SPAWN_PIVOT = "spawnPivot";

  /// Horizontal offset of the spawn pivot.
  @TmxPropertyInfo(name = "spawnPivotOffsetX", description = "Horizontal offset of the spawn pivot.", category = "Spawn", type = "float", defaultValue = "0")
  public static final String SPAWN_PIVOT_OFFSETX = "spawnPivotOffsetX";

  /// Vertical offset of the spawn pivot.
  @TmxPropertyInfo(name = "spawnPivotOffsetY", description = "Vertical offset of the spawn pivot.", category = "Spawn", type = "float", defaultValue = "0")
  public static final String SPAWN_PIVOT_OFFSETY = "spawnPivotOffsetY";

  // trigger
  /// Activation mode of a trigger (interact, collision, etc.).
  @TmxPropertyInfo(name = "triggerActivation", description = "Activation mode of a trigger (INTERACT, COLLISION).", category = "Trigger", type = "enum", defaultValue = "INTERACT")
  public static final String TRIGGER_ACTIVATION = "triggerActivation";

  /// Message dispatched when the trigger fires.
  @TmxPropertyInfo(name = "triggermessage", description = "Message dispatched when the trigger fires.", category = "Trigger", type = "string")
  public static final String TRIGGER_MESSAGE = "triggermessage";

  /// Whether the trigger can fire only once.
  @TmxPropertyInfo(name = "triggerOneTime", description = "Whether the trigger can fire only once.", category = "Trigger", type = "boolean", defaultValue = "false")
  public static final String TRIGGER_ONETIME = "triggerOneTime";

  /// Identifiers of entities that may activate the trigger.
  @TmxPropertyInfo(name = "triggerActivators", description = "Identifiers or tags of entities that may activate the trigger.", category = "Trigger", type = "string")
  public static final String TRIGGER_ACTIVATORS = "triggerActivators";

  /// Identifiers of entities that receive the trigger message.
  @TmxPropertyInfo(name = "triggerTarget", description = "Identifiers or tags of entities that receive the trigger message.", category = "Trigger", type = "string")
  public static final String TRIGGER_TARGETS = "triggerTarget";

  /// Cooldown, in milliseconds, between subsequent activations of the trigger.
  @TmxPropertyInfo(name = "triggerCooldown", description = "Cooldown in milliseconds between subsequent activations.", category = "Trigger", type = "int", defaultValue = "0")
  public static final String TRIGGER_COOLDOWN = "triggerCooldown";

  private static final List<String> availableProperties = new ArrayList<>();

  /// Property names specific to particle emitter map objects.
  public static final class Emitter {
    /// Encoded list of emitter colors.
    @TmxPropertyInfo(name = "emitterColors", description = "Comma-separated or encoded list of emitter colors.", category = "Emitter", type = "string")
    public static final String COLORS = "emitterColors";

    /// Encoded list of probabilities per emitter color.
    @TmxPropertyInfo(name = "emitterColorProbabilities", description = "Probabilities per emitter color.", category = "Emitter", type = "string")
    public static final String COLORPROBABILITIES = "emitterColorProbabilities";

    /// Spawn rate in milliseconds between spawn ticks.
    @TmxPropertyInfo(name = "emitterSpawnRate", description = "Spawn rate in milliseconds between spawn ticks.", category = "Emitter", type = "int", defaultValue = "100")
    public static final String SPAWNRATE = "emitterSpawnRate";

    /// Number of particles spawned per spawn tick.
    @TmxPropertyInfo(name = "emitterSpawnAmount", description = "Number of particles spawned per spawn tick.", category = "Emitter", type = "int", defaultValue = "1")
    public static final String SPAWNAMOUNT = "emitterSpawnAmount";

    /// Update delay of the emitter, in milliseconds.
    @TmxPropertyInfo(name = "emitterUpdateDelay", description = "Update delay of the emitter in milliseconds.", category = "Emitter", type = "int", defaultValue = "10")
    public static final String UPDATERATE = "emitterUpdateDelay";

    /// Total duration of the emitter, in milliseconds; `0` means infinite.
    @TmxPropertyInfo(name = "emitterDuration", description = "Total duration of the emitter in milliseconds (0 = infinite).", category = "Emitter", type = "int", defaultValue = "0")
    public static final String DURATION = "emitterDuration";

    /// Maximum number of concurrently alive particles.
    @TmxPropertyInfo(name = "emitterMaxParticles", description = "Maximum number of concurrently alive particles.", category = "Emitter", type = "int", defaultValue = "100")
    public static final String MAXPARTICLES = "emitterMaxParticles";

    /// Default particle type/shape used by the emitter.
    @TmxPropertyInfo(name = "emitterParticleType", description = "Default particle type or shape used by the emitter (RECTANGLE, CIRCLE, TEXT, SPRITE).", category = "Emitter", type = "enum", defaultValue = "RECTANGLE")
    public static final String PARTICLETYPE = "emitterParticleType";

    /// Per-particle color variance in the range `[0, 1]`.
    @TmxPropertyInfo(name = "emitterColorVariance", description = "Per-particle color variance ratio [0, 1].", category = "Emitter", type = "float", defaultValue = "0")
    public static final String COLORVARIANCE = "emitterColorVariance";

    /// Per-particle alpha variance in the range `[0, 1]`.
    @TmxPropertyInfo(name = "emitterAlphaVariance", description = "Per-particle alpha variance ratio [0, 1].", category = "Emitter", type = "float", defaultValue = "0")
    public static final String ALPHAVARIANCE = "emitterAlphaVariance";

    /// Horizontal alignment of the emitter origin.
    @TmxPropertyInfo(name = "emitterOriginAlign", description = "Horizontal alignment of the emitter origin.", category = "Emitter", type = "enum", defaultValue = "CENTER")
    public static final String ORIGIN_ALIGN = "emitterOriginAlign";

    /// Vertical alignment of the emitter origin.
    @TmxPropertyInfo(name = "emitterOriginValign", description = "Vertical alignment of the emitter origin.", category = "Emitter", type = "enum", defaultValue = "MIDDLE")
    public static final String ORIGIN_VALIGN = "emitterOriginValign";

    private Emitter() {
    }
  }

  /// Property names specific to particle configuration on emitter map objects.
  public static final class Particle {
    /// Minimum horizontal spawn offset, in pixels.
    @TmxPropertyInfo(name = "particleMinOffsetX", description = "Minimum horizontal spawn offset in pixels.", category = "Particle", type = "float", defaultValue = "0")
    public static final String OFFSET_X_MIN = "particleMinOffsetX";

    /// Maximum horizontal spawn offset, in pixels.
    @TmxPropertyInfo(name = "particleMaxOffsetX", description = "Maximum horizontal spawn offset in pixels.", category = "Particle", type = "float", defaultValue = "0")
    public static final String OFFSET_X_MAX = "particleMaxOffsetX";

    /// Minimum vertical spawn offset, in pixels.
    @TmxPropertyInfo(name = "particleMinOffsetY", description = "Minimum vertical spawn offset in pixels.", category = "Particle", type = "float", defaultValue = "0")
    public static final String OFFSET_Y_MIN = "particleMinOffsetY";

    /// Maximum vertical spawn offset, in pixels.
    @TmxPropertyInfo(name = "particleMaxOffsetY", description = "Maximum vertical spawn offset in pixels.", category = "Particle", type = "float", defaultValue = "0")
    public static final String OFFSET_Y_MAX = "particleMaxOffsetY";

    /// Minimum horizontal initial velocity.
    @TmxPropertyInfo(name = "particleMinVelocityX", description = "Minimum horizontal initial velocity.", category = "Particle", type = "float", defaultValue = "0")
    public static final String VELOCITY_X_MIN = "particleMinVelocityX";

    /// Maximum horizontal initial velocity.
    @TmxPropertyInfo(name = "particleMaxVelocityX", description = "Maximum horizontal initial velocity.", category = "Particle", type = "float", defaultValue = "0")
    public static final String VELOCITY_X_MAX = "particleMaxVelocityX";

    /// Minimum vertical initial velocity.
    @TmxPropertyInfo(name = "particleMinVelocityY", description = "Minimum vertical initial velocity.", category = "Particle", type = "float", defaultValue = "0")
    public static final String VELOCITY_Y_MIN = "particleMinVelocityY";

    /// Maximum vertical initial velocity.
    @TmxPropertyInfo(name = "particleMaxVelocityY", description = "Maximum vertical initial velocity.", category = "Particle", type = "float", defaultValue = "0")
    public static final String VELOCITY_Y_MAX = "particleMaxVelocityY";

    /// Minimum horizontal acceleration.
    @TmxPropertyInfo(name = "particleMinAccelerationX", description = "Minimum horizontal acceleration.", category = "Particle", type = "float", defaultValue = "0")
    public static final String ACCELERATION_X_MIN = "particleMinAccelerationX";

    /// Maximum horizontal acceleration.
    @TmxPropertyInfo(name = "particleMaxAccelerationX", description = "Maximum horizontal acceleration.", category = "Particle", type = "float", defaultValue = "0")
    public static final String ACCELERATION_X_MAX = "particleMaxAccelerationX";

    /// Minimum vertical acceleration.
    @TmxPropertyInfo(name = "particleMinAccelerationY", description = "Minimum vertical acceleration.", category = "Particle", type = "float", defaultValue = "0")
    public static final String ACCELERATION_Y_MIN = "particleMinAccelerationY";

    /// Maximum vertical acceleration.
    @TmxPropertyInfo(name = "particleMaxAccelerationY", description = "Maximum vertical acceleration.", category = "Particle", type = "float", defaultValue = "0")
    public static final String ACCELERATION_Y_MAX = "particleMaxAccelerationY";

    /// Minimum initial particle width, in pixels.
    @TmxPropertyInfo(name = "particleMinStartWidth", description = "Minimum initial particle width in pixels.", category = "Particle", type = "float", defaultValue = "4")
    public static final String STARTWIDTH_MIN = "particleMinStartWidth";

    /// Maximum initial particle width, in pixels.
    @TmxPropertyInfo(name = "particleMaxStartWidth", description = "Maximum initial particle width in pixels.", category = "Particle", type = "float", defaultValue = "4")
    public static final String STARTWIDTH_MAX = "particleMaxStartWidth";

    /// Minimum initial particle height, in pixels.
    @TmxPropertyInfo(name = "particleMinStartHeight", description = "Minimum initial particle height in pixels.", category = "Particle", type = "float", defaultValue = "4")
    public static final String STARTHEIGHT_MIN = "particleMinStartHeight";

    /// Maximum initial particle height, in pixels.
    @TmxPropertyInfo(name = "particleMaxStartHeight", description = "Maximum initial particle height in pixels.", category = "Particle", type = "float", defaultValue = "4")
    public static final String STARTHEIGHT_MAX = "particleMaxStartHeight";

    /// Minimum per-update width delta.
    @TmxPropertyInfo(name = "particleMinDeltaWidth", description = "Minimum per-update width delta.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTAWIDTH_MIN = "particleMinDeltaWidth";

    /// Maximum per-update width delta.
    @TmxPropertyInfo(name = "particleMaxDeltaWidth", description = "Maximum per-update width delta.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTAWIDTH_MAX = "particleMaxDeltaWidth";

    /// Minimum per-update height delta.
    @TmxPropertyInfo(name = "particleMinDeltaHeight", description = "Minimum per-update height delta.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTAHEIGHT_MIN = "particleMinDeltaHeight";

    /// Maximum per-update height delta.
    @TmxPropertyInfo(name = "particleMaxDeltaHeight", description = "Maximum per-update height delta.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTAHEIGHT_MAX = "particleMaxDeltaHeight";

    /// Minimum initial angle, in degrees.
    @TmxPropertyInfo(name = "particleMinAngle", description = "Minimum initial angle in degrees.", category = "Particle", type = "float", defaultValue = "0")
    public static final String ANGLE_MIN = "particleMinAngle";

    /// Maximum initial angle, in degrees.
    @TmxPropertyInfo(name = "particleMaxAngle", description = "Maximum initial angle in degrees.", category = "Particle", type = "float", defaultValue = "360")
    public static final String ANGLE_MAX = "particleMaxAngle";

    /// Minimum per-update angle delta, in degrees.
    @TmxPropertyInfo(name = "particleMinDeltaAngle", description = "Minimum per-update angle delta in degrees.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTA_ANGLE_MIN = "particleMinDeltaAngle";

    /// Maximum per-update angle delta, in degrees.
    @TmxPropertyInfo(name = "particleMaxDeltaAngle", description = "Maximum per-update angle delta in degrees.", category = "Particle", type = "float", defaultValue = "0")
    public static final String DELTA_ANGLE_MAX = "particleMaxDeltaAngle";

    /// Minimum particle time-to-live, in milliseconds.
    @TmxPropertyInfo(name = "particleMinTTL", description = "Minimum particle time-to-live in milliseconds.", category = "Particle", type = "int", defaultValue = "1000")
    public static final String TTL_MIN = "particleMinTTL";

    /// Maximum particle time-to-live, in milliseconds.
    @TmxPropertyInfo(name = "particleMaxTTL", description = "Maximum particle time-to-live in milliseconds.", category = "Particle", type = "int", defaultValue = "1000")
    public static final String TTL_MAX = "particleMaxTTL";

    /// Whether sprite-based particles are animated.
    @TmxPropertyInfo(name = "particleAnimateSprite", description = "Whether sprite-based particles are animated.", category = "Particle", type = "boolean", defaultValue = "false")
    public static final String ANIMATESPRITE = "particleAnimateSprite";

    /// Whether sprite-based particle animations are looped.
    @TmxPropertyInfo(name = "particleLoopSprite", description = "Whether sprite-based particle animations are looped.", category = "Particle", type = "boolean", defaultValue = "false")
    public static final String LOOPSPRITE = "particleLoopSprite";

    /// Encoded list of texts used by text-rendering particles.
    @TmxPropertyInfo(name = "particleTexts", description = "Comma-separated list of texts used by text-rendering particles.", category = "Particle", type = "string")
    public static final String TEXTS = "particleTexts";

    /// Whether particles fade out over their lifetime.
    @TmxPropertyInfo(name = "particleFade", description = "Whether particles fade out over their lifetime.", category = "Particle", type = "boolean", defaultValue = "true")
    public static final String FADE = "particleFade";

    /// Whether particles fade upon collision.
    @TmxPropertyInfo(name = "particleFadeOnCollision", description = "Whether particles fade upon collision.", category = "Particle", type = "boolean", defaultValue = "false")
    public static final String FADEONCOLLISION = "particleFadeOnCollision";

    /// Whether shape-based particles are rendered as outlines only.
    @TmxPropertyInfo(name = "particleOutlineOnly", description = "Whether shape-based particles are rendered as outlines only.", category = "Particle", type = "boolean", defaultValue = "false")
    public static final String OUTLINEONLY = "particleOutlineOnly";

    /// Minimum outline thickness for outline-rendered particles.
    @TmxPropertyInfo(name = "particleMinOutlineThickness", description = "Minimum outline thickness for outline-rendered particles.", category = "Particle", type = "float", defaultValue = "1")
    public static final String OUTLINETHICKNESS_MIN = "particleMinOutlineThickness";

    /// Maximum outline thickness for outline-rendered particles.
    @TmxPropertyInfo(name = "particleMaxOutlineThickness", description = "Maximum outline thickness for outline-rendered particles.", category = "Particle", type = "float", defaultValue = "1")
    public static final String OUTLINETHICKNESS_MAX = "particleMaxOutlineThickness";

    /// Whether particles are rendered with anti-aliasing.
    @TmxPropertyInfo(name = "particleAntiAliasing", description = "Whether particles are rendered with anti-aliasing.", category = "Particle", type = "boolean", defaultValue = "false")
    public static final String ANTIALIASING = "particleAntiAliasing";

    private Particle() {

    }
  }

  private MapObjectProperty() {
  }

  /// Returns the lazily-computed list of all built-in property names defined by this class and its nested [Emitter] and [Particle] classes.
  ///
  /// @return the list of available built-in property names
  public static List<String> getAvailableProperties() {
    if (availableProperties.isEmpty()) {
      addAvailableProperties(MapObjectProperty.class);
      addAvailableProperties(MapObjectProperty.Emitter.class);
      addAvailableProperties(MapObjectProperty.Particle.class);
    }

    return availableProperties;
  }

  /// Determines whether the given property name is a custom (user-defined) property, i.e. not part of the built-in property names.
  ///
  /// @param name the property name to test
  /// @return `true` if the name is not part of the built-in properties; `false` otherwise
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
