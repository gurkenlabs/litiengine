package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MapObjectProperties {
  public static final String COLLISION = "collision";
  public static final String COLLISIONALGIN = "collisionAlign";

  public static final String COLLISIONBOXHEIGHT = "collisionboxHeightFactor";
  public static final String COLLISIONBOXWIDTH = "collisionboxWidthFactor";
  public static final String COLLISIONVALGIN = "collisionValign";
  public static final String CUSTOM_MAPOBJECT_TYPE = "customMapObjectType";
  // decor mob
  public static final String DECORMOB_BEHAVIOUR = "decormob-behaviour";

  public static final String DECORMOB_VELOCITY = "decormob-velocity";
  public static final String EMITTERTYPE = "emitterType";
  public static final String FRICTION = "friction";
  public static final String HEALTH = "health";
  public static final String INDESTRUCTIBLE = "indestructible";

  // light source
  public static final String LIGHTBRIGHTNESS = "lightBrightness";
  public static final String LIGHTCOLOR = "lightColor";
  public static final String LIGHTINTENSITY = "lightIntensity";
  public static final String LIGHTSHAPE = "lightShape";

  public static final String MATERIAL = "material";
  public static final String MOBTYPE = "mobType";

  public static final String OBSTACLE = "isObstacle";
  // terrain
  public static final String REFLECTION = "reflection";
  // collision box
  public static final String SHADOWTYPE = "shadowType";
  public static final String SPRITESHEETNAME = "spritesheetName";

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

  private static List<Field> availableProperties = new ArrayList<>();

  public static boolean isCustom(final String name) {
    if (availableProperties.size() == 0) {
      for (final Field field : MapObjectProperties.class.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          availableProperties.add(field);
        }
      }
    }

    return !availableProperties.stream().anyMatch(x -> {
      try {
        return x.get(null).equals(name);
      } catch (final IllegalArgumentException e) {
        e.printStackTrace();
      } catch (final IllegalAccessException e) {
        e.printStackTrace();
      }
      return false;
    });
  }
}
