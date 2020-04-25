package de.gurkenlabs.litiengine.video;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import de.gurkenlabs.litiengine.resources.VideoResource;

public final class VideoManagerFactory {

  private VideoManagerFactory() {
    throw new UnsupportedOperationException();
  }
  
  private static final HashMap<String, Class<? extends VideoManager>> PLAYERS = new HashMap<String, Class<? extends VideoManager>>();
  private static String DEFAULT_VIDEO_PLAYER;
  
  public static void registerPlayerType(String name, Class<? extends VideoManager> clazz) {
    if(PLAYERS.containsKey(name)) {
      throw new IllegalStateException("Video player with name " + name + " already registered!");
    }
    PLAYERS.put(name, clazz);
    if(DEFAULT_VIDEO_PLAYER == null) {
      setDefaultPlayer(name);
    }
  }
  
  public static void setDefaultPlayer(String name) {
    if(PLAYERS.containsKey(name)) {
      DEFAULT_VIDEO_PLAYER = name;
    }
    else {
      throw new IllegalArgumentException("Player " + name + " is not registered");
    }
  }
  
  public static String getDefaultPlayer() {
    return DEFAULT_VIDEO_PLAYER;
  }
  
  public static VideoManager create(VideoResource videoResource) {
    try {
      Constructor<? extends VideoManager> c = PLAYERS.get(videoResource.getPlayerType()).getConstructor(VideoResource.class);
      c.setAccessible(true);
      return c.newInstance(videoResource);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new Error(e);
    }
  }
  
}
