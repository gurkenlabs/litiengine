package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapObjectProperty {
  private static final Logger log = Logger.getLogger(MapObjectProperty.class.getName());
  public static final String COLLISION = "collision";
  public static final String COLLISIONALGIN = "collisionAlign";

  public static final String COLLISIONBOXHEIGHT = "collisionboxHeightFactor";
  public static final String COLLISIONBOXWIDTH = "collisionboxWidthFactor";
  public static final String COLLISIONVALGIN = "collisionValign";
  public static final String CUSTOM_MAPOBJECT_TYPE = "customMapObjectType";
  // decor mob
  public static final String DECORMOB_BEHAVIOUR = "decormob-behaviour";

  public static final String DECORMOB_VELOCITY = "decormob-velocity";
  public static final String FRICTION = "friction";
  public static final String HEALTH = "health";
  public static final String INDESTRUCTIBLE = "indestructible";

  // light source
  public static final String LIGHTALPHA = "lightBrightness";
  public static final String LIGHTCOLOR = "lightColor";
  public static final String LIGHTINTENSITY = "lightIntensity";
  public static final String LIGHTSHAPE = "lightShape";
  public static final String LIGHTACTIVE = "lightActive";

  public static final String MATERIAL = "material";
  public static final String MOBTYPE = "mobType";

  public static final String OBSTACLE = "isObstacle";
  public static final String OBSTRUCTINGLIGHTS = "isObstructingLight";

  // terrain
  public static final String REFLECTION = "reflection";
  // collision box
  public static final String SHADOWTYPE = "shadowType";
  public static final String SHADOWOFFSET = "shadowOffset";

  public static final String SPRITESHEETNAME = "spritesheetName";
  public static final String PROP_ADDSHADOW = "addShadow";

  // spawnpoint
  public static final String SPAWN_TYPE = "spawnType";
  public static final String SPAWN_DIRECTION = "spawnDirection";

  public static final String TEAM = "team";
  public static final String TRIGGERACTIVATION = "triggerActivation";

  public static final String TRIGGERACTIVATORS = "triggerActivators";
  // trigger
  public static final String TRIGGERMESSAGE = "triggermessage";

  public static final String TRIGGERONETIME = "triggerOneTime";
  public static final String TRIGGERTARGETS = "triggerTarget";

  public static final String EMITTERCOLORS = "emitterColors";
  public static final String EMITTERCOLORPROBABILITIES = "emitterColorProbabilities";
  public static final String EMITTERSPAWNRATE = "emitterSpawnRate";
  public static final String EMITTERSPAWNAMOUNT = "emitterSpawnAmount";
  public static final String EMITTERUPDATEDELAY = "emitterUpdateDelay";
  public static final String EMITTERTIMETOLIVE = "emitterTimeToLive";
  public static final String EMITTERMAXPARTICLES = "emitterMaxParticles";
  public static final String EMITTERPARTICLETYPE = "emitterParticleType";
  public static final String EMITTERCOLORDEVIATION = "emitterColorDeviation";
  public static final String EMITTERALPHADEVIATION = "emitterAlphaDeviation";

  public static final String PARTICLEDELTAX = "particleDeltaX";
  public static final String PARTICLEDELTAY = "particleDeltaY";
  public static final String PARTICLEGRAVITYX = "particleGravityX";
  public static final String PARTICLEGRAVITYY = "particleGravityY";
  public static final String PARTICLESTARTWIDTH = "particleStartWidth";
  public static final String PARTICLESTARTHEIGHT = "particleStartHeight";
  public static final String PARTICLEDELTAWIDTH = "particleDeltaWidth";
  public static final String PARTICLEDELTAHEIGHT = "particleDeltaHeight";
  public static final String PARTICLESPRITETYPE = "particleSpriteType";
  public static final String PARTICLESPRITE = "particleSprite";
  public static final String PARTICLESTATICPHYSICS = "particleStaticPhysics";
  public static final String PARTICLETEXT = "particleText";

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
