package de.gurkenlabs.litiengine;

public class SpriteSheetInfo {
  private String path;
  private int width;
  private int height;

  public SpriteSheetInfo(String path, int width, int height) {
    super();
    this.path = path;
    this.width = width;
    this.height = height;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getWidth() {
    return this.width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
