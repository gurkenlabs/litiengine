package de.gurkenlabs.litiengine.resources;

import java.net.URI;

import de.gurkenlabs.litiengine.video.VideoManagerFactory;

public class VideoResource extends NamedResource {
  
  private final String player;
  private final String uri;
  
  public VideoResource(String uri, String name) {
    this(VideoManagerFactory.getDefaultPlayer(), uri, name);
  }
  
  public VideoResource(URI uri, String name) {
    this(VideoManagerFactory.getDefaultPlayer(), uri, name);
  }
  
  public VideoResource(String player, String uri, String name) {
    this.player = player;
    this.uri = uri;
    setName(name);
  }
  
  public VideoResource(String player, URI uri, String name) {
    this(player, uri.toString(), name);
  }

  public String getURI() {
    return uri;
  }
  
  public String getPlayerType() {
    return player;
  }
  
}
