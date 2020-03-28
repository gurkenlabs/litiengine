package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import de.gurkenlabs.litiengine.video.VideoManager;

public class Videos extends ResourcesContainer<VideoManager>{

  Videos(){}
  
  public VideoManager load(VideoResource video) throws NoClassDefFoundError {
    return load(video, false);
  }
  
  public VideoManager load(VideoResource video, boolean play) throws NoClassDefFoundError {
    VideoManager videoManager = new VideoManager(video, play);
    this.add(video.getName(), videoManager);
    return videoManager;
  }
  
  @Override
  protected VideoManager load(URL resourceName) throws Exception, NoClassDefFoundError {
    return new VideoManager(resourceName);
  }

}
