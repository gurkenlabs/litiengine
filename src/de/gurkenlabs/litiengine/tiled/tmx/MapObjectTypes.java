package de.gurkenlabs.litiengine.tiled.tmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapObjectTypes {
  public static final String EMITTER = "EMITTER";
  public static final String SPAWNPOINT = "SPAWNPOINT";
  public static final String SPAWNPOINT_CREEP = "SPAWNPOINT_CREEP";
  public static final String LANE = "LANE";
  public static final String PROP = "PROP";
  public static final String MOB = "MOB";
  public static final String DECORMOB = "DECORMOB";
  public static final String COLLISIONBOX = "COLLISIONBOX";
  public static final String LIGHTSOURCE = "LIGHTSOURCE";
  public static final String TRIGGER = "TRIGGER";
  public static final String SPAWNPOINT_PICKUP = "SPAWNPOINT_PICKUP";

  public static final List<String> ALL = new ArrayList<String>(Arrays.asList(EMITTER, SPAWNPOINT, SPAWNPOINT_CREEP, LANE, PROP, MOB, DECORMOB, COLLISIONBOX, LIGHTSOURCE, SPAWNPOINT_PICKUP));;

}
