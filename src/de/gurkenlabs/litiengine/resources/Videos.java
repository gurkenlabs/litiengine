package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import de.gurkenlabs.litiengine.video.GStreamerVideoManager;

public class Videos extends ResourcesContainer<GStreamerVideoManager>{

  Videos(){}
  
  public GStreamerVideoManager load(VideoResource video) throws NoClassDefFoundError {
    return load(video, false);
  }
  
  public GStreamerVideoManager load(VideoResource video, boolean play) throws NoClassDefFoundError {
    GStreamerVideoManager videoManager = new GStreamerVideoManager(video, play);
    this.add(video.getName(), videoManager);
    return videoManager;
  }
  
  @Override
  protected GStreamerVideoManager load(URL resourceName) throws Exception, NoClassDefFoundError {
    return new GStreamerVideoManager(resourceName);
  }

}
