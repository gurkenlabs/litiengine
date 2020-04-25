package de.gurkenlabs.litiengine.resources;

import java.net.URL;

import de.gurkenlabs.litiengine.video.VideoManager;
import de.gurkenlabs.litiengine.video.VideoManagerFactory;

public class Videos extends ResourcesContainer<VideoManager>{

  Videos(){}
  
  public VideoManager load(VideoResource video) throws NoClassDefFoundError {
    return load(video, false);
  }
  
  public VideoManager load(VideoResource video, boolean play) throws NoClassDefFoundError {
    VideoManager videoManager = VideoManagerFactory.create(video);
    if(play) {
      videoManager.play();
    }
    this.add(video.getName(), videoManager);
    return videoManager;
  }
  
  @Override
  protected VideoManager load(URL resourceName) throws Exception, NoClassDefFoundError {
    return VideoManagerFactory.create(new VideoResource(resourceName.toURI(), resourceName.toString()));
  }

}
