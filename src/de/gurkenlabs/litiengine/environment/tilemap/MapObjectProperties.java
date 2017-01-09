package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MapObjectProperties {
  public static final String TEAM = "team";
  public static final String SPRITESHEETNAME = "spritesheetName";
  public static final String COLLISIONBOXWIDTHFACTOR = "collisionboxWidthFactor";
  public static final String COLLISIONBOXHEIGHTFACTOR = "collisionboxHeightFactor";
  public static final String COLLISION = "collision";
  public static final String INDESTRUCTIBLE = "indestructible";
  public static final String MATERIAL = "material";
  public static final String HEALTH = "health";
  public static final String EMITTERTYPE = "emitterType";
  public static final String MOBTYPE = "mobType";

  // light source
  public static final String LIGHTBRIGHTNESS = "lightBrightness";
  public static final String LIGHTCOLOR = "lightColor";
  public static final String LIGHTSHAPE = "lightShape";

  public static final String SHADOWTYPE = "shadowType";
  public static final String TRIGGERMESSAGE = "triggermessage";
  public static final String SPAWN_TYPE = "spawnType";
  public static final String CUSTOM_MAPOBJECT_TYPE = "customMapObjectType";

  // terrain
  public static final String REFLECTION = "reflection";
  public static final String FRICTION = "friction";

  // decor mob
  public static final String DECORMOB_BEHAVIOUR = "decormob-behaviour";
  public static final String DECORMOB_VELOCITY = "decormob-velocity";

  private static List<Field> availableProperties = new ArrayList<>();

  public static boolean isCustom(String name) {
    if (availableProperties.size() == 0) {
      for (Field field : MapObjectProperties.class.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          availableProperties.add(field);
        }
      }
    }

    return !availableProperties.stream().anyMatch(x -> {
      try {
        return x.get(null).equals(name);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      return false;
    });
  }
}
