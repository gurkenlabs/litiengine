package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Dimension;

public class Resolution {
  public static final String R_4X3 = "4x3";
  public static final String R_5X4 = "5x4";
  public static final String R_16X9 = "16x9";
  public static final String R_16X10 = "16x10";

  private final int width;
  private final int height;
  private final Dimension dimension;
  private final String ratio;

  private Resolution(int width, int height, String ratio) {
    this.width = width;
    this.height = height;
    this.dimension = new Dimension(this.getWidth(), this.getHeight());
    this.ratio = ratio;
  }

  public static Resolution custom(int width, int height, String resolutionName) {
    return new Resolution(width, height, resolutionName);
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }

  public Dimension getDimension() {
    return this.dimension;
  }

  public String getRatio() {
    return this.ratio;
  }

  public static class Ratio4x3 {
    public static final Resolution RES_1024x768 = new Resolution(1024, 768, R_4X3);

    private Ratio4x3() {
    }
  }

  public static class Ratio5x4 {
    public static final Resolution RES_1280x1024 = new Resolution(1280, 1024, R_5X4);

    private Ratio5x4() {
    }
  }

  public static class Ratio16x9 {
    public static final Resolution RES_1280x720 = new Resolution(1280, 720, R_16X9);
    public static final Resolution RES_1360x768 = new Resolution(1360, 768, R_16X9);
    public static final Resolution RES_1366x768 = new Resolution(1366, 768, R_16X9);
    public static final Resolution RES_1536x864 = new Resolution(1536, 864, R_16X9);
    public static final Resolution RES_1600x900 = new Resolution(1600, 900, R_16X9);
    public static final Resolution RES_1920x1080 = new Resolution(1920, 1080, R_16X9);
    public static final Resolution RES_2560x1440 = new Resolution(2560, 1440, R_16X9);

    private Ratio16x9() {
    }
  }

  public static class Ratio16x10 {
    public static final Resolution RES_1280x800 = new Resolution(1280, 800, R_16X10);
    public static final Resolution RES_1440x900 = new Resolution(1440, 900, R_16X10);
    public static final Resolution RES_1680x1050 = new Resolution(1680, 1050, R_16X10);
    public static final Resolution RES_1920x1200 = new Resolution(1920, 1200, R_16X10);

    private Ratio16x10() {
    }
  }
}
