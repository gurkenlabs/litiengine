package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.util.AlphanumComparator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.List;

/// Engine-facing representation of a tile map and its layer hierarchy.
///
/// Dimensions returned by [#getWidth()] and [#getHeight()] are measured in tiles; use
/// [#getSizeInPixels()] or [#getBounds()] for map-coordinate dimensions. Maps loaded from TMX are
/// represented by [de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap].
///
/// @see de.gurkenlabs.litiengine.resources.Maps
/// @see de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap
public interface IMap extends ILayerList, Comparable<IMap> {

  /// Gets the tilesets.
  ///
  /// @return the tilesets
  List<ITileset> getTilesets();

  /// Gets the tileset entry referenced by the supplied global tile id.
  ///
  /// @param gid the global tile id
  /// @return the matching tileset entry, or `null` if no tileset contains the id
  ITilesetEntry getTilesetEntry(int gid);

  /// Gets the orientation.
  ///
  /// @return the orientation
  IMapOrientation getOrientation();

  /// Gets the source URL the map was loaded from.
  ///
  /// @return the source URL
  URL getPath();

  /// Gets the order in which orthogonal tile coordinates are rendered.
  ///
  /// @return the render order declared by the map
  RenderOrder getRenderOrder();

  /// Gets the size in pixels.
  ///
  /// @return the size in pixels
  Dimension getSizeInPixels();

  /// Gets the map width in tiles.
  ///
  /// @return the width in tiles
  int getWidth();

  /// Gets the map height in tiles.
  ///
  /// @return the height in tiles
  int getHeight();

  /// Gets the width and height in tiles.
  ///
  /// @return the map size in tiles
  Dimension getSizeInTiles();

  /// Gets the bounding rectangle of the map in pixels.
  ///
  /// @return the bounding rectangle
  Rectangle2D getBounds();

  /// Gets the tile size.
  ///
  /// @return the tile size
  Dimension getTileSize();

  /// Gets the coordinates of the parallax origin in pixels.
  ///
  /// @return The parallax origin.
  Point2D getParallaxOrigin();

  /// Gets the horizontal tile size.
  ///
  /// @return the horizontal tile size
  int getTileWidth();

  /// Gets the vertical tile size.
  ///
  /// @return the vertical tile size
  int getTileHeight();

  /// Gets the straight edges' length for hexagonal maps.
  ///
  /// @return the hex side length
  int getHexSideLength();

  /// Gets the axis along which hexagonal or staggered rows are offset.
  ///
  /// @return the staggering axis
  StaggerAxis getStaggerAxis();

  /// Gets whether even or odd indexes are staggered.
  ///
  /// @return the staggering index
  StaggerIndex getStaggerIndex();

  /// Gets the version.
  ///
  /// @return the version
  double getVersion();

  /// Gets the Tiled editor version string the map was saved with.
  ///
  /// @return the Tiled version string
  String getTiledVersion();

  /// Gets the display name of this map.
  ///
  /// @return the map name
  String getName();

  /// Sets the name.
  ///
  /// @param name the new name
  void setName(String name);

  /// Gets the next available object id, used to assign unique ids to newly created map objects.
  ///
  /// @return the next object id
  int getNextObjectId();

  /// Gets the next available layer id, used to assign unique ids to newly created layers.
  ///
  /// @return the next layer id
  int getNextLayerId();

  /// Gets the background color of the map.
  ///
  /// @return the background color
  Color getBackgroundColor();

  /// Returns whether this is an infinite map (i.e. composed of chunks rather than a fixed-size grid).
  ///
  /// @return `true` if the map is infinite
  boolean isInfinite();

  @Override
  default int compareTo(IMap map) {
    return AlphanumComparator.compareTo(this.getName(), map.getName());
  }
}
