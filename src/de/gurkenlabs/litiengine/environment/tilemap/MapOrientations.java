package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A class containing various standard map orientations.
 */
public class MapOrientations {
  /**
   * <p>
   * An {@code IMapOrientation} for orthogonal maps, consistent with the behavior of Tiled.
   *
   * <p>
   * With this orientation, tiles are treated as rectangles with dimensions equal to the tile size, layed out in rows and columns starting with the
   * origin.
   */
  public static final IMapOrientation ORTHOGONAL = new Orthogonal();

  /**
   * <p>
   * An isometric {@code IMapOrientation}, consistent with the behavior of Tiled.
   *
   * <p>
   * With this orientation, the shapes of tiles are transformed into an isometric coordinate system via the transformation
   * {@code (x, y) -> ((x-y)/2, (x+y)/2)}. Rectangles within this coordinate system are rendered as diamond shapes. Points are also translated such
   * that every tile in a map fits in the first quadrant.
   *
   * <p>
   * This orientation does <em>not</em> transform map objects into its coordinate system. Maps with an odd-numbered tile width or height will throw
   * {@code IllegalArgumentException}s when using this orientation.
   */
  public static final IMapOrientation ISOMETRIC = new Isometric();

  /**
   * <p>
   * A staggered isometric {@code IMapOrientation}, consistent with the behavior of Tiled.
   *
   * <p>
   * This orientation is similar to the isometric orientation, but the tile coordinates are changed to be arranged in a rectangular area. The
   * locations of tiles are positioned using rectangular coordinates, with tiles with parity on the stagger axis matching the stagger index filling
   * the gaps between the other tiles.
   *
   * <p>
   * This orientation requires that a stagger axis and stagger index be set. If either are missing, an {@code IllegalArgumentException} will be
   * thrown. Like the standard isometric orientation, this orientation will also throw an {@code IllegalArgumentException} when used with a map with
   * an odd-numbered tile width or height.
   */
  public static final IMapOrientation ISOMETRIC_STAGGERED = new StaggeredIsometric();

  /**
   * <p>
   * A hexagonal {@code IMapOrientation}, consistent with the behavior of Tiled.
   *
   * <p>
   * Tiles are arranged in the same manner as the staggered isometric orientation (the staggered isometric orientation can be viewed as a special case
   * of this orientation with a hex side length of 0), with extra space between tiles (the hex side length) added on the stagger axis to allow for a
   * hexagonal shape.
   *
   * <p>
   * This orientation has the same requirements as staggered isometric, with the additional restriction that the hex side length must be an even
   * number.
   */
  public static final IMapOrientation HEXAGONAL = new Hexagonal();

  /**
   * Determines the appropriate {@code IMapOrientation} instance for the given name. If no such orientation exists, this method returns {@code null}.
   *
   * @param name
   *          The name of the orientation, as stored in the TMX file
   * @return The {@code IMapOrientation} by the given name
   */
  public static IMapOrientation forName(String name) {
    if ("orthogonal".equals(name)) {
      return ORTHOGONAL;
    } else if ("isometric".equals(name)) {
      return ISOMETRIC;
    } else if ("staggered".equals(name)) {
      return ISOMETRIC_STAGGERED;
    } else if ("hexagonal".equals(name)) {
      return HEXAGONAL;
    } else {
      return null;
    }
  }

  private abstract static class Base implements IMapOrientation {
    @Override
    public Point getLocation(Point tile, IMap map) {
      return this.getLocation(tile.x, tile.y, map);
    }

    @Override
    public Shape getShape(Point tile, IMap map) {
      return this.getShape(tile.x, tile.y, map);
    }

    @Override
    public Rectangle2D getBounds(int x, int y, IMap map) {
      return this.getShape(x, y, map).getBounds2D();
    }

    @Override
    public Rectangle2D getBounds(Point tile, IMap map) {
      return this.getBounds(tile.x, tile.y, map);
    }

    @Override
    public Shape getEnclosingTileShape(double x, double y, IMap map) {
      return this.getShape(this.getTile(x, y, map), map);
    }

    @Override
    public Shape getEnclosingTileShape(Point2D location, IMap map) {
      return this.getEnclosingTileShape(location.getX(), location.getY(), map);
    }

    @Override
    public Rectangle2D getEnclosingTileBounds(double x, double y, IMap map) {
      return this.getEnclosingTileShape(x, y, map).getBounds2D();
    }

    @Override
    public Rectangle2D getEnclosingTileBounds(Point2D location, IMap map) {
      return this.getEnclosingTileBounds(location.getX(), location.getY(), map);
    }

    @Override
    public Point getTile(Point2D location, IMap map) {
      return this.getTile(location.getX(), location.getY(), map);
    }

    protected static void checkTileSize(IMap map) {
      if (map.getTileWidth() == 0) {
        throw new ArithmeticException("tile width == 0");
      }
      if (map.getTileHeight() == 0) {
        throw new ArithmeticException("tile height == 0");
      }
    }
  }

  private static class Orthogonal extends Base {
    @Override
    public String getName() {
      return "orthogonal";
    }

    @Override
    public Dimension getSize(IMap map) {
      return new Dimension(map.getWidth() * map.getTileWidth(), map.getHeight() * map.getTileHeight());
    }

    @Override
    public Point getLocation(int x, int y, IMap map) {
      return new Point(x * map.getTileWidth(), (y + 1) * map.getTileHeight());
    }

    @Override
    public Shape getShape(int x, int y, IMap map) {
      Dimension tileSize = map.getTileSize();
      return new Rectangle(x * tileSize.width, y * tileSize.height, tileSize.width, tileSize.height);
    }

    @Override
    public Point getTile(double x, double y, IMap map) {
      checkTileSize(map);
      return new Point((int) Math.floor(x / map.getTileWidth()), (int) Math.floor(y / map.getTileHeight()));
    }
  }

  private static class Isometric extends Base {
    @Override
    public String getName() {
      return "isometric";
    }

    @Override
    public Dimension getSize(IMap map) {
      checkSizeParity(map);
      int side = map.getWidth() + map.getHeight();
      return new Dimension(side * map.getTileWidth() / 2, side * map.getTileHeight() / 2);
    }

    @Override
    public Point getLocation(int x, int y, IMap map) {
      checkSizeParity(map);
      return new Point((x - y + map.getHeight() - 1) * map.getTileWidth() / 2, (x + y + 2) * map.getTileHeight() / 2);
    }

    @Override
    public Shape getShape(int x, int y, IMap map) {
      Point loc = this.getLocation(x, y, map);
      Dimension tileSize = map.getTileSize();
      Polygon pg = new Polygon(new int[] { 0, tileSize.width / 2, tileSize.width, tileSize.width / 2 }, new int[] { tileSize.height / -2, -tileSize.height, tileSize.height / -2, 0 }, 4);
      pg.translate(loc.x, loc.y);
      return pg;
    }

    @Override
    public Point getTile(double x, double y, IMap map) {
      checkTileSize(map);
      checkSizeParity(map);
      // transform our coordinates to put the top-left corner at the origin and make tiles 1x1
      x /= map.getTileWidth();
      y /= map.getTileHeight();
      x -= 0.5 * map.getHeight();
      // this uses ceil(y)-1 instead of floor(y) to be consistent with getShape; see the definition of "insideness" in the Shape class
      return new Point((int) Math.floor(y + x), (int) Math.ceil(y - x) - 1);
    }

    static void checkSizeParity(IMap map) {
      if (map.getWidth() % 2 != 0) {
        throw new IllegalArgumentException("tile width is not divisible by 2");
      }
      if (map.getHeight() % 2 != 0) {
        throw new IllegalArgumentException("tile height is not divisible by 2");
      }
    }
  }

  private static class StaggeredIsometric extends Base {
    @Override
    public String getName() {
      return "staggered";
    }

    @Override
    public Dimension getSize(IMap map) {
      this.checkValidity(map);
      Dimension mapSize = map.getSizeInTiles();
      Dimension tileSize = map.getTileSize();
      int width = mapSize.width * tileSize.width;
      int height = mapSize.height * tileSize.height;
      if (map.getStaggerAxis() == StaggerAxis.X) {
        width = (width + tileSize.width) / 2;
        if (mapSize.width > 1 || map.getStaggerIndex() == StaggerIndex.EVEN) {
          height += tileSize.height / 2;
        }
      } else {
        height = (height + tileSize.height) / 2;
        if (mapSize.height > 1 || map.getStaggerIndex() == StaggerIndex.EVEN) {
          width += tileSize.width / 2;
        }
      }
      return new Dimension(width, height);
    }

    @Override
    public Point getLocation(int x, int y, IMap map) {
      this.checkValidity(map);
      Dimension tileSize = map.getTileSize();
      int sx = x * tileSize.width;
      int sy = y * tileSize.height;
      if (map.getStaggerAxis() == StaggerAxis.X) {
        sx /= 2;
        if (x % 2 == 0 ^ map.getStaggerIndex() == StaggerIndex.ODD) {
          sy += tileSize.height / 2;
        }
      } else {
        sy /= 2;
        if (y % 2 == 0 ^ map.getStaggerIndex() == StaggerIndex.ODD) {
          sx += tileSize.width / 2;
        }
      }
      return new Point(sx, sy + tileSize.height);
    }

    @Override
    public Shape getShape(int x, int y, IMap map) {
      Point loc = this.getLocation(x, y, map);
      Dimension tileSize = map.getTileSize();
      Polygon pg = new Polygon(new int[] { 0, tileSize.width / 2, tileSize.width, tileSize.width / 2 }, new int[] { tileSize.height / -2, -tileSize.height, tileSize.height / -2, 0 }, 4);
      pg.translate(loc.x, loc.y);
      return pg;
    }

    @Override
    public Point getTile(double x, double y, IMap map) {
      checkTileSize(map);
      this.checkValidity(map);
      x /= map.getTileWidth();
      y /= map.getTileHeight();
      StaggerAxis axis = map.getStaggerAxis();
      StaggerIndex index = map.getStaggerIndex();
      // preprocess the point for the stagger properties
      if (axis == StaggerAxis.Y) {
        double temp = y;
        y = x;
        x = temp;
      }
      if (index == StaggerIndex.EVEN) {
        x += 0.5;
      }
      int cx = (int) Math.floor(x);
      int cy = (int) Math.floor(y);
      double cix = x - cx;
      double ciy = y - cy;
      Point p = new Point(cx * 2, cy);
      if (cix + ciy < 0.5) {
        p.x--;
        p.y--;
      } else if (ciy < cix - 0.5 || axis == StaggerAxis.X && ciy == cix - 0.5) {
        p.x++;
        p.y--;
      } else if (cix + ciy >= 1.5) {
        p.x++;
      } else if (ciy > cix + 0.5 || axis == StaggerAxis.Y && ciy == cix + 0.5) {
        p.x--;
      }
      // undo effects of the preprocessing on the point
      if (index == StaggerIndex.EVEN) {
        p.x--;
      }
      if (axis == StaggerAxis.Y) {
        int temp = p.y;
        p.y = p.x;
        p.x = temp;
      }
      return p;
    }

    protected void checkValidity(IMap map) {
      Isometric.checkSizeParity(map);
      if (map.getStaggerAxis() == null) {
        throw new IllegalArgumentException("no stagger axis");
      }
      if (map.getStaggerIndex() == null) {
        throw new IllegalArgumentException("no stagger index");
      }
    }
  }

  private static class Hexagonal extends StaggeredIsometric {
    @Override
    public String getName() {
      return "hexagonal";
    }

    @Override
    public Dimension getSize(IMap map) {
      Dimension d = super.getSize(map);
      if (map.getStaggerAxis() == StaggerAxis.X) {
        d.width += Math.max(map.getWidth() - 1, 0) * map.getHexSideLength() / 2;
      } else {
        d.height += Math.max(map.getHeight() - 1, 0) * map.getHexSideLength() / 2;
      }
      return d;
    }

    @Override
    public Point getLocation(int x, int y, IMap map) {
      Point p = super.getLocation(x, y, map);
      if (map.getStaggerAxis() == StaggerAxis.X) {
        p.x += x * map.getHexSideLength() / 2;
      } else {
        p.y += y * map.getHexSideLength() / 2;
      }
      return p;
    }

    @Override
    public Shape getShape(int x, int y, IMap map) {
      Point loc = this.getLocation(x, y, map);
      Dimension tileSize = map.getTileSize();
      int hexSide = map.getHexSideLength();
      
      int[] xp;
      int[] yp;
      if (map.getStaggerAxis() == StaggerAxis.X) {
        int off = (tileSize.width - hexSide) / 2;
        xp = new int[] { 0, off, tileSize.width - off, tileSize.width, tileSize.width - off, off };
        yp = new int[] { tileSize.height / -2, -tileSize.height, -tileSize.height, tileSize.height / -2, 0, 0 };
      } else {
        int off = (tileSize.height - hexSide) / 2;
        xp = new int[] { tileSize.width / 2, tileSize.width, tileSize.width, tileSize.width / 2, 0, 0 };
        yp = new int[] { 0, -off, off - tileSize.height, -tileSize.height, off - tileSize.height, -off };
      }
      Polygon pg = new Polygon(xp, yp, 6);
      pg.translate(loc.x, loc.y);
      return pg;
    }

    @Override
    public Point getTile(double x, double y, IMap map) {
      this.checkValidity(map);
      int w = map.getTileWidth();
      int h = map.getTileHeight();
      StaggerAxis axis = map.getStaggerAxis();
      StaggerIndex index = map.getStaggerIndex();
      if (axis == StaggerAxis.Y) {
        double temp1 = y;
        y = x;
        x = temp1;
        int temp2 = h;
        h = w;
        w = temp2;
      }
      int hs = map.getHexSideLength();
      int wp = w + hs;
      if (wp == 0) {
        throw new ArithmeticException("tile " + (axis == StaggerAxis.Y ? "height" : "width") + " + hex side length == 0");
      }
      if (h == 0) {
        throw new ArithmeticException("tile " + (axis == StaggerAxis.Y ? "width" : "height") + " == 0");
      }
      int wm = w - hs;
      double frac = (double) wm / wp;
      if (index == StaggerIndex.EVEN) {
        x += 0.5 * wp;
      }
      x /= wp;
      y /= h;
      int cx = (int) Math.floor(x);
      int cy = (int) Math.floor(y);
      double cix = x - cx;
      double ciy = y - cy;
      Point p = new Point(cx * 2, cy);
      if (ciy < 0.5) {
        if (cix < (0.5 - ciy) * frac) {
          p.x--;
          p.y--;
        } else {
          double xt = 0.5 + ciy * frac;
          if (cix > xt || axis == StaggerAxis.X && cix == xt) {
            p.x++;
            if (cix > 1.0 + frac * (0.5 - ciy)) {
              p.x++;
            } else {
              p.y--;
            }
          }
        }
      } else {
        double xt = frac * (ciy - 0.5);
        if (cix < xt || axis == StaggerAxis.Y && cix == xt) {
          p.x--;
        } else if (cix > (double) w / wp + (0.5 - ciy) * frac) {
          p.x++;
          xt = frac * (ciy - 0.5) + 1;
          if (cix > xt || axis == StaggerAxis.X && cix == xt) {
            p.x++;
          }
        }
      }
      if (index == StaggerIndex.EVEN) {
        p.x--;
      }
      if (axis == StaggerAxis.Y) {
        int temp = p.y;
        p.y = p.x;
        p.x = temp;
      }
      return p;
    }

    @Override
    protected void checkValidity(IMap map) {
      super.checkValidity(map);
      if (map.getHexSideLength() % 2 != 0) {
        throw new IllegalArgumentException("hex side length is not divisible by 2");
      }
    }
  }

  private MapOrientations() {
    throw new UnsupportedOperationException();
  }
}
