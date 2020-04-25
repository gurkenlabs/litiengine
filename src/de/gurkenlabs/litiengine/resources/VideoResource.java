package de.gurkenlabs.litiengine.resources;

import java.net.URI;

public class VideoResource extends NamedResource {
  
  private final String uri;
  
  public VideoResource(String uri, String name) {
    this.uri = uri;
    setName(name);
  }
  
  public VideoResource(URI uri, String name) {
    this(uri.toString(), name);
  }

  public String getURI() {
    return uri;
  }
  
}
