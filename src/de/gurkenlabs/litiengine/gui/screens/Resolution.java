package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Dimension;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.GameWindow;

/**
 * Represents the resolution of the game window consisting of the width and height and information about the ratio.
 * 
 * <p>
 * This class also provides access to predefined known resolutions of the different aspect ratios which can be used
 * to set the resolution of the <code>GameWindow</code>.
 * </p>
 * <ul>
 * <li>{@link Ratio4x3}</li>
 * <li>{@link Ratio5x4}</li>
 * <li>{@link Ratio16x9}</li>
 * <li>{@link Ratio16x10}</li>
 * </ul>
 *
 * @see GameWindow#setResolution(Resolution)
 */
public class Resolution {
  private static final Logger log = Logger.getLogger(Resolution.class.getName());

  private final int width;
  private final int height;
  private final Dimension dimension;
  private final Ratio ratio;

  private Resolution(int width, int height, Ratio ratio) {
    this.width = width;
    this.height = height;
    this.dimension = new Dimension(this.getWidth(), this.getHeight());
    this.ratio = ratio;
  }

  public static Resolution custom(int width, int height, String resolutionName) {
    return new Resolution(width, height, new Ratio(width, height, resolutionName));
  }

  /**
   * Gets the height of this resolution.
   * 
   * @return The height of the resolution.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the width of this resolution.
   * 
   * @return The width of the resolution.
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the dimension of this resolution consisting of it's width and height.
   * 
   * @return The dimension of the resolution.
   */
  public Dimension getDimension() {
    return this.dimension;
  }

  public String toDimensionString() {
    return this.toDimensionString("x");
  }

  public String toDimensionString(String delimiter) {
    return this.getWidth() + delimiter + this.getHeight();
  }

  @Override
  public String toString() {
    return this.toDimensionString();
  }

  /**
   * Gets the aspect ratio of this resolution.
   * 
   * @return The aspect ratio of this resolution.
   */
  public Ratio getRatio() {
    return this.ratio;
  }

  /**
   * Contains predefined <code>Resolutions</code> with an aspect ratio of 4:3.
   */
  public static class Ratio4x3 extends Ratio {
    public static final Resolution RES_1024x768 = new Resolution(1024, 768, new Ratio4x3());

    private Ratio4x3() {
      super(4, 3);
    }

    /**
     * Gets all predefined resolutions with an aspect ratio of 4:3.
     * 
     * @return All predefined resolutions with an aspect ratio of 4:3.
     */
    public static List<Resolution> getAll() {
      return getAll(Ratio4x3.class);
    }
  }

  /**
   * Contains predefined <code>Resolutions</code> with an aspect ratio of 5:4.
   */
  public static class Ratio5x4 extends Ratio {
    public static final Resolution RES_1280x1024 = new Resolution(1280, 1024, new Ratio5x4());

    private Ratio5x4() {
      super(5, 4);
    }

    /**
     * Gets all predefined resolutions with an aspect ratio of 5:4.
     * 
     * @return All predefined resolutions with an aspect ratio of 5:4.
     */
    public static List<Resolution> getAll() {
      return getAll(Ratio5x4.class);
    }
  }

  /**
   * Contains predefined <code>Resolutions</code> with an aspect ratio of 16:9.
   */
  public static class Ratio16x9 extends Ratio {
    public static final Resolution RES_1280x720 = new Resolution(1280, 720, new Ratio16x9());
    public static final Resolution RES_1360x768 = new Resolution(1360, 768, new Ratio16x9());
    public static final Resolution RES_1366x768 = new Resolution(1366, 768, new Ratio16x9());
    public static final Resolution RES_1536x864 = new Resolution(1536, 864, new Ratio16x9());
    public static final Resolution RES_1600x900 = new Resolution(1600, 900, new Ratio16x9());
    public static final Resolution RES_1920x1080 = new Resolution(1920, 1080, new Ratio16x9());
    public static final Resolution RES_2560x1440 = new Resolution(2560, 1440, new Ratio16x9());

    private Ratio16x9() {
      super(16, 9);
    }

    /**
     * Gets all predefined resolutions with an aspect ratio of 16:9.
     * 
     * @return All predefined resolutions with an aspect ratio of 16:9.
     */
    public static List<Resolution> getAll() {
      return getAll(Ratio16x9.class);
    }
  }

  /**
   * Contains predefined <code>Resolutions</code> with an aspect ratio of 16:10.
   */
  public static class Ratio16x10 extends Ratio {
    public static final Resolution RES_1280x800 = new Resolution(1280, 800, new Ratio16x10());
    public static final Resolution RES_1440x900 = new Resolution(1440, 900, new Ratio16x10());
    public static final Resolution RES_1680x1050 = new Resolution(1680, 1050, new Ratio16x10());
    public static final Resolution RES_1920x1200 = new Resolution(1920, 1200, new Ratio16x10());

    private Ratio16x10() {
      super(16, 10);
    }

    /**
     * Gets all predefined resolutions with an aspect ratio of 16:10.
     * 
     * @return All predefined resolutions with an aspect ratio of 16:10.
     */
    public static List<Resolution> getAll() {
      return getAll(Ratio16x10.class);
    }
  }

  public static class Ratio {
    private final String name;
    private final int x;
    private final int y;

    protected Ratio(int x, int y) {
      this(x, y, x + ":" + y);
    }

    protected Ratio(int x, int y, String name) {
      this.x = x;
      this.y = y;
      this.name = name;
    }

    protected static List<Resolution> getAll(Class<?> clz) {
      List<Resolution> resolutions = new ArrayList<>();

      for (final Field field : clz.getDeclaredFields()) {
        if (field.getType() == Resolution.class && Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          try {
            resolutions.add((Resolution) field.get(null));
          } catch (final IllegalArgumentException | IllegalAccessException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
          }
        }
      }

      return resolutions;
    }

    /**
     * Gets the name of this aspect ratio
     * 
     * @return The name of this aspect ratio.
     */
    public String getName() {
      return this.name;
    }

    /**
     * Gets the x-value of this aspect ratio.
     * 
     * @return The x-value of this aspect ratio.
     */
    public int getX() {
      return this.x;
    }

    /**
     * Gets the y-value of this aspect ratio.
     * 
     * @return The y-value of this aspect ratio.
     */
    public int getY() {
      return this.y;
    }

    @Override
    public String toString() {
      return this.getName();
    }
  }
}
