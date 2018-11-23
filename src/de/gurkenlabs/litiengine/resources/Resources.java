package de.gurkenlabs.litiengine.resources;

public final class Resources {
  private static Fonts fonts;
  private static Sounds sounds;
  private static Maps maps;
  private static Strings strings;
  private static Images images;
  private static Spritesheets spritesheets;

  static {
    fonts = new Fonts();
    sounds = new Sounds();
    maps = new Maps();
    strings = new Strings();
    images = new Images();
    spritesheets = new Spritesheets();
  }

  private Resources() {
  }

  public static Fonts fonts() {
    return fonts;
  }

  public static Sounds sounds() {
    return sounds;
  }

  public static Maps maps() {
    return maps;
  }

  public static Strings strings() {
    return strings;
  }

  public static Images images() {
    return images;
  }

  public static Spritesheets spritesheets() {
    return spritesheets;
  }
}
