package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapObjectProperty {
  private static final Logger log = Logger.getLogger(MapObjectProperty.class.getName());
  // collision entity
  public static final String COLLISION = "collision";
  public static final String COLLISION_ALGIN = "collisionAlign";
  public static final String COLLISION_VALGIN = "collisionValign";

  // collision box
  public static final String COLLISIONBOX_HEIGHT = "collisionboxHeightFactor";
  public static final String COLLISIONBOX_WIDTH = "collisionboxWidthFactor";
  public static final String COLLISIONBOX_OBSTRUCTINGLIGHTS = "isObstructingLight";

  // general entity stuff
  public static final String HEALTH = "health";
  public static final String SPRITESHEETNAME = "spritesheetName";
  public static final String TEAM = "team";

  // decor mob
  public static final String DECORMOB_BEHAVIOUR = "decormob-behaviour";
  public static final String DECORMOB_VELOCITY = "decormob-velocity";

  // props
  public static final String PROP_INDESTRUCTIBLE = "indestructible";
  public static final String PROP_MATERIAL = "material";
  public static final String PROP_OBSTACLE = "isObstacle";
  public static final String PROP_ADDSHADOW = "addShadow";
  public static final String PROP_ROTATION = "rotationSprite";
  public static final String PROP_FLIPHORIZONTALLY = "flipHorizontally";
  public static final String PROP_FLIPVERTICALLY = "flipVertically";

  // light source
  public static final String LIGHT_ALPHA = "lightBrightness";
  public static final String LIGHT_COLOR = "lightColor";
  public static final String LIGHT_INTENSITY = "lightIntensity";
  public static final String LIGHT_SHAPE = "lightShape";
  public static final String LIGHT_ACTIVE = "lightActive";

  // static shadow
  public static final String SHADOW_TYPE = "shadowType";
  public static final String SHADOW_OFFSET = "shadowOffset";

  // spawnpoint
  public static final String SPAWN_TYPE = "spawnType";
  public static final String SPAWN_DIRECTION = "spawnDirection";

  // trigger
  public static final String TRIGGER_ACTIVATION = "triggerActivation";
  public static final String TRIGGER_MESSAGE = "triggermessage";
  public static final String TRIGGER_ONETIME = "triggerOneTime";
  public static final String TRIGGER_ACTIVATORS = "triggerActivators";
  public static final String TRIGGER_TARGETS = "triggerTarget";
  public static final String TRIGGER_COOLDOWN = "triggerCooldown";

  public static final String EMITTER_COLORS = "emitterColors";
  public static final String EMITTER_COLORPROBABILITIES = "emitterColorProbabilities";
  public static final String EMITTER_SPAWNRATE = "emitterSpawnRate";
  public static final String EMITTER_SPAWNAMOUNT = "emitterSpawnAmount";
  public static final String EMITTER_UPDATEDELAY = "emitterUpdateDelay";
  public static final String EMITTER_TIMETOLIVE = "emitterTimeToLive";
  public static final String EMITTER_MAXPARTICLES = "emitterMaxParticles";
  public static final String EMITTER_PARTICLETYPE = "emitterParticleType";
  public static final String EMITTER_COLORDEVIATION = "emitterColorDeviation";
  public static final String EMITTER_ALPHADEVIATION = "emitterAlphaDeviation";

  public static final String PARTICLE_MINX = "particleMinX";
  public static final String PARTICLE_MAXX = "particleMaxX";
  public static final String PARTICLE_MINY = "particleMinY";
  public static final String PARTICLE_MAXY = "particleMaxY";
  public static final String PARTICLE_Y_RANDOM = "particleYRandom";
  public static final String PARTICLE_MINDELTAX = "particleMinDeltaX";
  public static final String PARTICLE_MAXDELTAX = "particleMaxDeltaX";
  public static final String PARTICLE_DELTAX_RANDOM = "particleDeltaXRandom";
  public static final String PARTICLE_MINDELTAY = "particleMinDeltaY";
  public static final String PARTICLE_MAXDELTAY = "particleMaxDeltaY";
  public static final String PARTICLE_DELTAY_RANDOM = "particleDeltaYRandom";
  public static final String PARTICLE_MINGRAVITYX = "particleMinGravityX";
  public static final String PARTICLE_MAXGRAVITYX = "particleMaxGravityX";
  public static final String PARTICLE_GRAVITYX_RANDOM = "particleGravityXRandom";
  public static final String PARTICLE_MINGRAVITYY = "particleMinGravityY";
  public static final String PARTICLE_MAXGRAVITYY = "particleMaxGravityY";
  public static final String PARTICLE_GRAVITYY_RANDOM = "particleGravityYRandom";
  public static final String PARTICLE_MINSTARTWIDTH = "particleMinStartWidth";
  public static final String PARTICLE_MAXSTARTWIDTH = "particleMaxStartWidth";
  public static final String PARTICLE_STARTWIDTH_RANDOM = "particleStartWidthRandom";
  public static final String PARTICLE_MINSTARTHEIGHT = "particleMinStartHeight";
  public static final String PARTICLE_MAXSTARTHEIGHT = "particleMaxStartHeight";
  public static final String PARTICLE_STARTHEIGHT_RANDOM = "particleStartHeightRandom";
  public static final String PARTICLE_MINDELTAWIDTH = "particleMinDeltaWidth";
  public static final String PARTICLE_MAXDELTAWIDTH = "particleMaxDeltaWidth";
  public static final String PARTICLE_DELTAWIDTH_RANDOM = "particleDeltaWidthRandom";
  public static final String PARTICLE_MINDELTAHEIGHT = "particleMinDeltaHeight";
  public static final String PARTICLE_MAXDELTAHEIGHT = "particleMaxDeltaHeight";
  public static final String PARTICLE_DELTAHEIGHT_RANDOM = "particleDeltaHeightRandom";
  public static final String PARTICLE_SPRITE = "particleSprite";
  public static final String PARTICLE_ANIMATESPRITE = "particleAnimateSprite";
  public static final String PARTICLE_STATICPHYSICS = "particleStaticPhysics";
  public static final String PARTICLE_TEXT = "particleText";

  private static List<Field> availableProperties = new ArrayList<>();

  private MapObjectProperty() {
  }

  public static boolean isCustom(final String name) {
    if (availableProperties.isEmpty()) {
      for (final Field field : MapObjectProperty.class.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          availableProperties.add(field);
        }
      }
    }

    return !availableProperties.stream().anyMatch(x -> {
      try {
        return x.get(null).equals(name);
      } catch (final IllegalArgumentException | IllegalAccessException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
      return false;
    });
  }
}
