package de.gurkenlabs.litiengine.environment.tilemap;

public enum RenderOrder {
  RIGHT_DOWN("right-down", false, false),
  RIGHT_UP("right-up", false, true),
  LEFT_DOWN("left-down", true, false),
  LEFT_UP("left-up", true, true);

  public final String name;
  public final boolean rtl;
  public final boolean btt;

  public static RenderOrder forName(String name) {
    if ("right-down".equals(name)) {
      return RIGHT_DOWN;
    } else if ("right-up".equals(name)) {
      return RIGHT_UP;
    } else if ("left-down".equals(name)) {
      return LEFT_DOWN;
    } else if ("left-up".equals(name)) {
      return LEFT_UP;
    } else {
      return null;
    }
  }

  private RenderOrder(String name, boolean rtl, boolean btt) {
    this.name = name;
    this.rtl = rtl;
    this.btt = btt;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
