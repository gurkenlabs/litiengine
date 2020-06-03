package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapObjectProperty {
  private static final Logger log = Logger.getLogger(MapObjectProperty.class.getName());

  public static final String TAGS = "tags";
  public static final String RENDERTYPE = "renderType";
  public static final String RENDERWITHLAYER = "renderWithLayer";

  // collision entity
  public static final String COLLISION = "collision";
  public static final String COLLISION_ALIGN = "collisionAlign";
  public static final String COLLISION_VALIGN = "collisionValign";
  public static final String COLLISION_TYPE = "collisionType";

  // collision box
  public static final String COLLISIONBOX_HEIGHT = "collisionboxHeight";
  public static final String COLLISIONBOX_WIDTH = "collisionboxWidth";
  public static final String COLLISIONBOX_OBSTRUCTINGLIGHTS = "isObstructingLight";

  // general entity stuff
  public static final String SPRITESHEETNAME = "spritesheetName";
  public static final String SCALE_SPRITE = "scaling";

  // mobile entity
  public static final String MOVEMENT_ACCELERATION = "acceleration";
  public static final String MOVEMENT_DECELERATION = "deceleration";
  public static final String MOVEMENT_VELOCITY = "velocity";
  public static final String MOVEMENT_TURNONMOVE = "turnOnMove";

  // combat entity
  public static final String COMBAT_HITPOINTS = "hitpoints";
  public static final String COMBAT_INDESTRUCTIBLE = "indestructible";
  public static final String COMBAT_TEAM = "team";

  // props
  public static final String PROP_MATERIAL = "material";
  public static final String PROP_ADDSHADOW = "addShadow";
  public static final String PROP_ROTATION = "rotationSprite";
  public static final String PROP_FLIPHORIZONTALLY = "flipHorizontally";
  public static final String PROP_FLIPVERTICALLY = "flipVertically";

  // light source
  public static final String LIGHT_COLOR = "lightColor";
  public static final String LIGHT_INTENSITY = "lightIntensity";
  public static final String LIGHT_SHAPE = "lightShape";
  public static final String LIGHT_ACTIVE = "lightActive";
  public static final String LIGHT_FOCUSOFFSETX = "lightFocusOffsetX";
  public static final String LIGHT_FOCUSOFFSETY = "lightFocusOffsetY";

  // static shadow
  public static final String SHADOW_TYPE = "shadowType";
  public static final String SHADOW_OFFSET = "shadowOffset";

  // spawnpoint
  public static final String SPAWN_INFO = "spawnInfo";
  public static final String SPAWN_DIRECTION = "spawnDirection";
  public static final String SPAWN_PIVOT = "spawnPivot";
  public static final String SPAWN_PIVOT_OFFSETX = "spawnPivotOffsetX";
  public static final String SPAWN_PIVOT_OFFSETY = "spawnPivotOffsetY";

  // trigger
  public static final String TRIGGER_ACTIVATION = "triggerActivation";
  public static final String TRIGGER_MESSAGE = "triggermessage";
  public static final String TRIGGER_ONETIME = "triggerOneTime";
  public static final String TRIGGER_ACTIVATORS = "triggerActivators";
  public static final String TRIGGER_TARGETS = "triggerTarget";
  public static final String TRIGGER_COOLDOWN = "triggerCooldown";

  private static List<String> availableProperties = new ArrayList<>();

  public static final class Emitter {
    public static final String COLORS = "emitterColors";
    public static final String COLORPROBABILITIES = "emitterColorProbabilities";
    public static final String SPAWNRATE = "emitterSpawnRate";
    public static final String SPAWNAMOUNT = "emitterSpawnAmount";
    public static final String UPDATERATE = "emitterUpdateDelay";
    public static final String TIMETOLIVE = "emitterTimeToLive";
    public static final String MAXPARTICLES = "emitterMaxParticles";
    public static final String PARTICLETYPE = "emitterParticleType";
    public static final String COLORVARIANCE = "emitterColorVariance";
    public static final String ALPHAVARIANCE = "emitterAlphaVariance";
    public static final String ORIGIN_ALIGN = "emitterOriginAlign";
    public static final String ORIGIN_VALIGN = "emitterOriginValign";

    private Emitter() {
    }
  }

  public static final class Particle {
    public static final String MINX = "particleMinX";
    public static final String MAXX = "particleMaxX";
    public static final String MINY = "particleMinY";
    public static final String MAXY = "particleMaxY";
    public static final String MINDELTAX = "particleMinDeltaX";
    public static final String MAXDELTAX = "particleMaxDeltaX";
    public static final String MINDELTAY = "particleMinDeltaY";
    public static final String MAXDELTAY = "particleMaxDeltaY";
    public static final String MINGRAVITYX = "particleMinGravityX";
    public static final String MAXGRAVITYX = "particleMaxGravityX";
    public static final String MINGRAVITYY = "particleMinGravityY";
    public static final String MAXGRAVITYY = "particleMaxGravityY";
    public static final String MINSTARTWIDTH = "particleMinStartWidth";
    public static final String MAXSTARTWIDTH = "particleMaxStartWidth";
    public static final String MINSTARTHEIGHT = "particleMinStartHeight";
    public static final String MAXSTARTHEIGHT = "particleMaxStartHeight";
    public static final String MINDELTAWIDTH = "particleMinDeltaWidth";
    public static final String MAXDELTAWIDTH = "particleMaxDeltaWidth";
    public static final String MINDELTAHEIGHT = "particleMinDeltaHeight";
    public static final String MAXDELTAHEIGHT = "particleMaxDeltaHeight";
    public static final String SPRITE = "particleSprite";
    public static final String ANIMATESPRITE = "particleAnimateSprite";
    public static final String COLLISIONTYPE = "particlePhysics";
    public static final String TEXT = "particleText";
    public static final String MINTTL = "particleMinTTL";
    public static final String MAXTTL = "particleMaxTTL";
    public static final String FADE = "particleFade";

    private Particle() {
    }
  }

  private MapObjectProperty() {
  }

  public static List<String> getAvailableProperties() {
    if (availableProperties.isEmpty()) {
      addAvailableProperties(MapObjectProperty.class);
      addAvailableProperties(MapObjectProperty.Emitter.class);
      addAvailableProperties(MapObjectProperty.Particle.class);
    }

    return availableProperties;
  }

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
